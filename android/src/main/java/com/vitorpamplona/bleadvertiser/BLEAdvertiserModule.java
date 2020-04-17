package com.vitorpamplona.bleavertiser;

import com.facebook.react.uimanager.*;
import com.facebook.react.bridge.*;
import com.facebook.systrace.Systrace;
import com.facebook.systrace.SystraceMessage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.os.Build;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.lang.Thread;
import java.lang.Object;
import java.util.Hashtable;
import java.util.Set;

public class BLEAdvertiserModule extends ReactContextBaseJavaModule {

    public static final String TAG = "BleAdvertiserXX0";
    private BluetoothAdapter mBluetoothAdapter;
    
    private static Hashtable<String, BluetoothLeAdvertiser> mAdvertiserList;
    private static Hashtable<String, AdvertiseCallback> mAdvertiserCallbackList;
    private static BluetoothLeScanner mScanner;
    private static ScanCallback mScannerCallback;
    private int companyId;
    private Boolean mObservedState;

    //Constructor
    public BLEAdvertiserModule(ReactApplicationContext reactContext) {
        super(reactContext);

        mAdvertiserList = new Hashtable<String, BluetoothLeAdvertiser>();
        mAdvertiserCallbackList = new Hashtable<String, AdvertiseCallback>();

        BluetoothManager bluetoothManager = (BluetoothManager) reactContext.getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        } 

        if (mBluetoothAdapter != null) {
            mObservedState = mBluetoothAdapter.isEnabled();
        }

        this.companyId = 0x0000;

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        reactContext.registerReceiver(mReceiver, filter);
    }
    
    @Override
    public String getName() {
        return "BLEAdvertiser";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("ADVERTISE_MODE_BALANCED",        AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        constants.put("ADVERTISE_MODE_LOW_LATENCY",     AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        constants.put("ADVERTISE_MODE_LOW_POWER",       AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        constants.put("ADVERTISE_TX_POWER_HIGH",        AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        constants.put("ADVERTISE_TX_POWER_LOW",         AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
        constants.put("ADVERTISE_TX_POWER_MEDIUM",      AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        constants.put("ADVERTISE_TX_POWER_ULTRA_LOW",   AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW);

        constants.put("SCAN_MODE_BALANCED",             ScanSettings.SCAN_MODE_BALANCED);
        constants.put("SCAN_MODE_LOW_LATENCY",          ScanSettings.SCAN_MODE_LOW_LATENCY);
        constants.put("SCAN_MODE_LOW_POWER",            ScanSettings.SCAN_MODE_LOW_POWER);
        constants.put("SCAN_MODE_OPPORTUNISTIC",        ScanSettings.SCAN_MODE_OPPORTUNISTIC);
        constants.put("MATCH_MODE_AGGRESSIVE",          ScanSettings.MATCH_MODE_AGGRESSIVE);
        constants.put("MATCH_MODE_STICKY",              ScanSettings.MATCH_MODE_STICKY);
        constants.put("MATCH_NUM_FEW_ADVERTISEMENT",    ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT);
        constants.put("MATCH_NUM_MAX_ADVERTISEMENT",    ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
        constants.put("MATCH_NUM_ONE_ADVERTISEMENT",    ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT);

        return constants;
    }

    @ReactMethod
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @ReactMethod
    public void broadcast(String uid, ReadableArray payload, ReadableMap options, Promise promise) {
        if (mBluetoothAdapter == null) {
            Log.w("BLEAdvertiserModule", "Device does not support Bluetooth. Adapter is Null");
            promise.reject("Device does not support Bluetooth. Adapter is Null");
            return;
        } 
        
        if (companyId == 0x0000) {
            Log.w("BLEAdvertiserModule", "Invalid company id");
            promise.reject("Invalid company id");
            return;
        } 
        
        if (mBluetoothAdapter == null) {
            Log.w("BLEAdvertiserModule", "mBluetoothAdapter unavailable");
            promise.reject("mBluetoothAdapter unavailable");
            return;
        } 

        if (mObservedState != null && !mObservedState) {
            Log.w("BLEAdvertiserModule", "Bluetooth disabled");
            promise.reject("Bluetooth disabled");
            return;
        }

        BluetoothLeAdvertiser tempAdvertiser;
        AdvertiseCallback tempCallback;

        if (mAdvertiserList.containsKey(uid)) {
            tempAdvertiser = mAdvertiserList.remove(uid);
            tempCallback = mAdvertiserCallbackList.remove(uid);

            tempAdvertiser.stopAdvertising(tempCallback);
        } else {
            tempAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            tempCallback = new BLEAdvertiserModule.SimpleAdvertiseCallback(promise);
        }
         
        if (tempAdvertiser == null) {
            Log.w("BLEAdvertiserModule", "Advertiser Not Available unavailable");
            promise.reject("Advertiser unavailable on this device");
            return;
        }
        
        AdvertiseSettings settings = buildAdvertiseSettings(options);
        AdvertiseData data = buildAdvertiseData(ParcelUuid.fromString(uid), toByteArray(payload), options);

        tempAdvertiser.startAdvertising(settings, data, tempCallback);

        mAdvertiserList.put(uid, tempAdvertiser);
        mAdvertiserCallbackList.put(uid, tempCallback);
    }

    private byte[] toByteArray(ReadableArray payload) {
        byte[] temp = new byte[payload.size()];
        for (int i = 0; i < payload.size(); i++) {
            temp[i] = (byte)payload.getInt(i);
        }
        return temp;
    }

   @ReactMethod
    public void stopBroadcast(final Promise promise) {
        Log.w("BLEAdvertiserModule", "Stop Broadcast call");

        if (mBluetoothAdapter == null) {
            Log.w("BLEAdvertiserModule", "mBluetoothAdapter unavailable");
            promise.reject("mBluetoothAdapter unavailable");
            return;
        } 

        if (mObservedState != null && !mObservedState) {
            Log.w("BLEAdvertiserModule", "Bluetooth disabled");
            promise.reject("Bluetooth disabled");
            return;
        }

        WritableArray promiseArray=Arguments.createArray();

        Set<String> keys = mAdvertiserList.keySet();
        for (String key : keys) {
            BluetoothLeAdvertiser tempAdvertiser = mAdvertiserList.remove(key);
            AdvertiseCallback tempCallback = mAdvertiserCallbackList.remove(key);
            if (tempAdvertiser != null) {
                tempAdvertiser.stopAdvertising(tempCallback);
                promiseArray.pushString(key);
            }
        }

        promise.resolve(promiseArray);
    }

    @ReactMethod
	public void scan(ReadableArray manufacturerPayload, ReadableMap options, Promise promise) {
        if (mBluetoothAdapter == null) {
            promise.reject("Device does not support Bluetooth. Adapter is Null");
            return;
        }

        if (mObservedState != null && !mObservedState) {
            Log.w("BLEAdvertiserModule", "Bluetooth disabled");
            promise.reject("Bluetooth disabled");
            return;
        }

        if (mScannerCallback == null) {
            // Cannot change. 
            mScannerCallback = new SimpleScanCallback();
        } 
        
        if (mScanner == null) {
            mScanner = mBluetoothAdapter.getBluetoothLeScanner();
        } else {
            // was running. Needs to stop first. 
            mScanner.stopScan(mScannerCallback);
        }

        if (mScanner == null) {
            Log.w("BLEAdvertiserModule", "Scanner Not Available unavailable");
            promise.reject("Scanner unavailable on this device");
            return;
        } 

        ScanSettings scanSettings = buildScanSettings(options);
    
        List<ScanFilter> filters = new ArrayList<>();
        //filters.add(new ScanFilter.Builder().setManufacturerData(companyId, toByteArray(manufacturerPayload)).build());
        
        mScanner.startScan(filters, scanSettings, mScannerCallback);
        promise.resolve("Scanner started");
    }

    @ReactMethod
	public void stopScan(Promise promise) {
        if (mBluetoothAdapter == null) {
            promise.reject("Device does not support Bluetooth. Adapter is Null");
            return;
        } 

        if (mObservedState != null && !mObservedState) {
            Log.w("BLEAdvertiserModule", "Bluetooth disabled");
            promise.reject("Bluetooth disabled");
            return;
        }

        if (mScanner != null) {
            mScanner.stopScan(mScannerCallback);
            mScanner = null;
            promise.resolve("Scanner stopped");
        } else {
            promise.resolve("Scanner not started");
        }
    }

    private ScanSettings buildScanSettings(ReadableMap options) {
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();

        if (options != null && options.hasKey("scanMode")) {
            scanSettingsBuilder.setScanMode(options.getInt("scanMode"));
        } 

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (options != null && options.hasKey("numberOfMatches")) {
                scanSettingsBuilder.setNumOfMatches(options.getInt("numberOfMatches"));
            }
            if (options != null && options.hasKey("matchMode")) {
                scanSettingsBuilder.setMatchMode(options.getInt("matchMode"));
            }
        }

        if (options != null && options.hasKey("reportDelay")) {
            scanSettingsBuilder.setReportDelay(options.getInt("reportDelay"));
        }

        return scanSettingsBuilder.build();
    }

    private class SimpleScanCallback extends ScanCallback {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
            Log.w("BLEAdvertiserModule", "Scanned: " + result.toString());

            WritableMap params = Arguments.createMap();
            WritableArray paramsUUID = Arguments.createArray();

            if (result.getScanRecord().getServiceUuids()!=null) {
                for (ParcelUuid uuid : result.getScanRecord().getServiceUuids()) {
                    paramsUUID.pushString(uuid.toString());
                }
            }

            params.putArray("serviceUuids", paramsUUID);
            params.putInt("rssi", result.getRssi());

            if (result.getScanRecord() != null) {
                params.putInt("txPower", result.getScanRecord().getTxPowerLevel());
                params.putString("deviceName", result.getScanRecord().getDeviceName());
                params.putInt("advFlags", result.getScanRecord().getAdvertiseFlags());
            }
            
            if (result.getDevice() != null) {
                params.putString("deviceAddress", result.getDevice().getAddress());
            }

            sendEvent("onDeviceFound", params);
		}

		@Override
		public void onBatchScanResults(final List<ScanResult> results) {

		}

		@Override
		public void onScanFailed(final int errorCode) {
            /*
           switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    promise.reject("Fails to start scan as BLE scan with the same settings is already started by the app."); break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    promise.reject("Fails to start scan as app cannot be registered."); break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    promise.reject("Fails to start power optimized scan as this feature is not supported."); break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    promise.reject("Fails to start scan due an internal error"); break;
                default: 
                    promise.reject("Scan failed: " + errorCode);
            }
            promise.reject("Scan failed: Should not be here. ");*/
		}
	};

    @ReactMethod
    public void enableAdapter() {
        if (mBluetoothAdapter == null) {
            return;
        }

        if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON && mBluetoothAdapter.getState() != BluetoothAdapter.STATE_TURNING_ON) {
            mBluetoothAdapter.enable();
        }
    }

    @ReactMethod
    public void disableAdapter() {
        if (mBluetoothAdapter == null) {
            return;
        }

        if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_OFF && mBluetoothAdapter.getState() != BluetoothAdapter.STATE_TURNING_OFF) {
            mBluetoothAdapter.disable();
        }
    }

    @ReactMethod
    public void getAdapterState(Promise promise) {
        if (mBluetoothAdapter == null) {
            Log.w("BLEAdvertiserModule", "Device does not support Bluetooth. Adapter is Null");
            promise.reject("Device does not support Bluetooth. Adapter is Null");
            return;
        }

        Log.d(TAG, "GetAdapter State" + String.valueOf(mBluetoothAdapter.getState()));

        switch (mBluetoothAdapter.getState()) {
            case BluetoothAdapter.STATE_OFF:
                promise.resolve("STATE_OFF"); break;
            case BluetoothAdapter.STATE_TURNING_ON:
                promise.resolve("STATE_TURNING_ON"); break;
            case BluetoothAdapter.STATE_ON:
                promise.resolve("STATE_ON"); break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                promise.resolve("STATE_TURNING_OFF"); break;
        }

        promise.resolve(String.valueOf(mBluetoothAdapter.getState()));
    }

    @ReactMethod
    public void isActive(Promise promise) {
        if (mBluetoothAdapter == null) {
            Log.w("BLEAdvertiserModule", "Device does not support Bluetooth. Adapter is Null");
            promise.resolve(false);
            return;
        }

        Log.d(TAG, "GetAdapter State" + String.valueOf(mBluetoothAdapter.getState()));
        promise.resolve(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON); 
    }

    private AdvertiseSettings buildAdvertiseSettings(ReadableMap options) {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();

        if (options != null && options.hasKey("advertiseMode")) {
            settingsBuilder.setAdvertiseMode(options.getInt("advertiseMode"));
        }

        if (options != null && options.hasKey("txPowerLevel")) {
            settingsBuilder.setTxPowerLevel(options.getInt("txPowerLevel"));
        }

        if (options != null && options.hasKey("connectable")) {
            settingsBuilder.setConnectable(options.getBoolean("connectable"));
        }

        return settingsBuilder.build();
    }

    private AdvertiseData buildAdvertiseData(ParcelUuid uuid, byte[] payload, ReadableMap options) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();

        if (options != null && options.hasKey("includeDeviceName")) 
            dataBuilder.setIncludeDeviceName(options.getBoolean("includeDeviceName"));
        
         if (options != null && options.hasKey("includeTxPowerLevel")) 
            dataBuilder.setIncludeTxPowerLevel(options.getBoolean("includeTxPowerLevel"));
        
        dataBuilder.addManufacturerData(companyId, payload);
        dataBuilder.addServiceUuid(uuid);
        return dataBuilder.build();
    }

    private class SimpleAdvertiseCallback extends AdvertiseCallback {
        Promise promise;

        public SimpleAdvertiseCallback () {
        }

        public SimpleAdvertiseCallback (Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.i(TAG, "Advertising failed with code "+ errorCode);

            if (promise == null) return;

            switch (errorCode) {
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    promise.reject("This feature is not supported on this platform."); break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    promise.reject("Failed to start advertising because no advertising instance is available."); break;
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    promise.reject("Failed to start advertising as the advertising is already started."); break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    promise.reject("Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes."); break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    promise.reject("Operation failed due to an internal error."); break;
            }
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(TAG, "Advertising successful");

            if (promise == null) return;
            promise.resolve(settingsInEffect.toString());
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                final int prevState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR);
                
                Log.d(TAG, String.valueOf(state));
                switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    mObservedState = false;
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    mObservedState = false;
                    break;
                case BluetoothAdapter.STATE_ON:
                    mObservedState = true;
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    mObservedState = true;
                    break;
                }

                // Only send enabled when fully ready. Turning on and Turning OFF are seen as disabled. 
                if (state == BluetoothAdapter.STATE_ON && prevState != BluetoothAdapter.STATE_ON) {
                    WritableMap params = Arguments.createMap();
                    params.putBoolean("enabled", true);
                    sendEvent("onBTStatusChange", params);
                } else if (state != BluetoothAdapter.STATE_ON && prevState == BluetoothAdapter.STATE_ON ) {
                    WritableMap params = Arguments.createMap();
                    params.putBoolean("enabled", false);
                    sendEvent("onBTStatusChange", params);
                }
            }
        }
    };

    private void sendEvent(String eventName, WritableMap params) {
        getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    // @Override
    // public void onCreate() {
    //     super.onCreate();
    //     // Register for broadcasts on BluetoothAdapter state change
    //     IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    //     registerReceiver(mReceiver, filter);
    // }

    // @Override
    // public void onDestroy() {
    //     super.onDestroy();
    //     unregisterReceiver(mReceiver);
    // }
}
