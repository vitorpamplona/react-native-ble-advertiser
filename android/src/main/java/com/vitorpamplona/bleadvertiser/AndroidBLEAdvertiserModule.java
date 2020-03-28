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
import java.lang.Thread;
import java.lang.Object;
import java.util.Hashtable;
import java.util.Set;

public class AndroidBLEAdvertiserModule extends ReactContextBaseJavaModule {

    public static final String TAG = "AndroidBleAdvertiserXX0";
    private BluetoothAdapter mBluetoothAdapter;
    
    private static Hashtable<String, BluetoothLeAdvertiser> mAdvertiserList;
    private static Hashtable<String, AdvertiseCallback> mAdvertiserCallbackList;
    private static BluetoothLeScanner mScanner;
    private static ScanCallback mScannerCallback;
    private int companyId;
    private Boolean mObservedState;

    //Constructor
    public AndroidBLEAdvertiserModule(ReactApplicationContext reactContext) {
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
        return "AndroidBLEAdvertiserModule";
    }

    @ReactMethod
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @ReactMethod
    public void broadcast(String uid, ReadableArray payload, Promise promise) {
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
        }

        BluetoothLeAdvertiser tempAdvertiser;
        AdvertiseCallback tempCallback;

        if (mAdvertiserList.containsKey(uid)) {
            tempAdvertiser = mAdvertiserList.remove(uid);
            tempCallback = mAdvertiserCallbackList.remove(uid);

            tempAdvertiser.stopAdvertising(tempCallback);
        } else {
            tempAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            tempCallback = new AndroidBLEAdvertiserModule.SimpleAdvertiseCallback(promise);
        }
         
        if (tempAdvertiser == null) {
            Log.w("BLEAdvertiserModule", "Advertiser Not Available unavailable");
            promise.reject("Advertiser unavailable on this device");
            return;
        }
        
        AdvertiseSettings settings = buildAdvertiseSettings();
        AdvertiseData data = buildAdvertiseData(ParcelUuid.fromString(uid), toByteArray(payload));

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

        if (mObservedState != null && !mObservedState) {
            Log.w("BLEAdvertiserModule", "Bluetooth disabled");
            promise.reject("Bluetooth disabled");
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
        filters.add(new ScanFilter.Builder().setManufacturerData(companyId, toByteArray(manufacturerPayload)).build());
        
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

        if (options != null) {
            if (options.hasKey("scanMode")) {
                scanSettingsBuilder.setScanMode(options.getInt("scanMode"));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (options.hasKey("numberOfMatches")) {
                    scanSettingsBuilder.setNumOfMatches(options.getInt("numberOfMatches"));
                }
                if (options.hasKey("matchMode")) {
                    scanSettingsBuilder.setMatchMode(options.getInt("matchMode"));
                }
            }

            if (options.hasKey("reportDelay")) {
                scanSettingsBuilder.setReportDelay(options.getInt("reportDelay"));
            }
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

        Log.d(TAG, "Here" + String.valueOf(mBluetoothAdapter.getState()));
        promise.resolve(String.valueOf(mBluetoothAdapter.getState()));
    }
    

    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setConnectable(true);
        return settingsBuilder.build();
    }

    private AdvertiseData buildAdvertiseData(ParcelUuid uuid, byte[] payload) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.setIncludeDeviceName(false);
        dataBuilder.setIncludeTxPowerLevel(true);
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
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                                    BluetoothAdapter.ERROR);
                
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

                // Only send enabled when ready. Turning on and OFF are equal as disabled. 
                if (BluetoothAdapter.STATE_ON) {
                    sendEvent("onBTStatusChange", true);
                } else {
                    sendEvent("onBTStatusChange", false);
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
