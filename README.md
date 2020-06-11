# react-native-ble-advertiser [![npm version](https://img.shields.io/npm/v/react-native-ble-advertiser.svg?style=flat)](https://www.npmjs.com/package/react-native-ble-advertiser) [![npm downloads](https://img.shields.io/npm/dm/react-native-ble-advertiser.svg?style=flat)](https://www.npmjs.com/package/react-native-ble-advertiser) [![GitHub issues](https://img.shields.io/github/issues/vitorpamplona/react-native-ble-advertiser.svg?style=flat)](https://github.com/vitorpamplona/react-native-ble-advertiser/issues)

Bluetooth Advertiser & Scanner for React Native. This is in a very early development focused in contact tracing applications. Please use with caution.

## Supported Platforms
- ReactNative 0.60+
- Android 21+
- iOS 10+
- Bluetooth API 5.0+

## Features / TO-DO List

- [x] Android Advertiser (v0.0.2)
- [x] Android Scanner (v0.0.6)
- [x] Android BLE Status Events (v0.0.8)
- [x] iOS Advertiser (v0.0.10)
- [x] iOS Scanner (v0.0.11)
- [ ] iOS BLE Status Events 
- [ ] Android Background Service (Use [react-native-background-fetch](https://www.npmjs.com/package/react-native-background-fetch))
- [ ] iOS Background Service (Use [react-native-background-fetch](https://www.npmjs.com/package/react-native-background-fetch))

## Installation

```bash
npm install react-native-ble-advertiser --save
```

or

```bash
yarn add react-native-ble-advertiser
```

### Setting up the Android Project

In the AndroidManifest.xml file, add the Bluetooth permissions

```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

### Setting up the iOS Project

On your plist file, add the following keys: 
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>...</string>
<key>NSBluetoothAlwaysUsageDescription</key>
<string>...</string>
<key>UIBackgroundModes</key>
<array>
    <string>bluetooth-central</string>
    <string>bluetooth-peripheral</string>
</array>
<key>UIRequiredDeviceCapabilities</key>
<array>
    <string>bluetooth-le</string>
</array>
```

## Usage

### Advertiser

Import the module

```js
import BLEAdvertiser from 'react-native-ble-advertiser'
```

Define your company ID and broadcast your service UUID with additional manufactoring data: 

```js
BLEAdvertiser.setCompanyId(0x00); // Your Company's Code
BLEAdvertiser.broadcast([UUID], [ManufacturerData], {}) // The service UUID and additional manufacturer data. 
    .then(success => console.log('Broadcasting Sucessful', success))
    .catch(error => console.log('Broadcasting Error', error));
```

Available Advertising Options: 
```js
{
    advertiseMode: BLEAdvertiser.<
        ADVERTISE_MODE_LOW_POWER, 
        ADVERTISE_MODE_BALANCED, 
        ADVERTISE_MODE_LOW_LATENCY, 
        ADVERTISE_MODE_LOW_POWER>,
    txPowerLevel: BLEAdvertiser.<
        ADVERTISE_TX_POWER_LOW, 
        ADVERTISE_TX_POWER_HIGH, 
        ADVERTISE_TX_POWER_LOW, 
        ADVERTISE_TX_POWER_MEDIUM, 
        ADVERTISE_TX_POWER_ULTRA_LOW>,
    connectable: <false,true>, 
    includeDeviceName: <false,true>, 
    includeTxPowerLevel: <false,true>
}
```

Stop broadcasting

```js
BLEAdvertiser.stopBroadcast()
    .then(success => console.log("Stop Broadcast Successful", success))
    .catch(error => console.log("Stop Broadcast Error", error));
```

### Scanner

Import the modules

```js
import BLEAdvertiser from 'react-native-ble-advertiser'
import { NativeEventEmitter, NativeModules } from 'react-native';
```

Register a listener to collect the devices through ReactNative events. 

```js
const eventEmitter = new NativeEventEmitter(NativeModules.BLEAdvertiser);
eventEmitter.addListener('onDeviceFound', (deviceData) => {
    console.log(deviceData);
});
```

Scan by Service UUID 

```js
BLEAdvertiser.setCompanyId(0x00); // Your Company's Code
BLEAdvertiser.scanByService([UUID], {}) // service UUID and options
    .then(success => console.log("Scan Successful", success))
    .catch(error => console.log("Scan Error", error)); 
```

Scan by your company ID and additional data (Android only). 

```js
BLEAdvertiser.setCompanyId(0x00); // Your Company's Code
BLEAdvertiser.scan([ManufacturerData], {}) // manufacturer data and options
    .then(success => console.log("Scan Successful", success))
    .catch(error => console.log("Scan Error", error)); 
```

Available Scanning Options: 
```js
{
    scanMode: BLEAdvertiser.<
        SCAN_MODE_BALANCED, 
        SCAN_MODE_LOW_LATENCY, 
        SCAN_MODE_LOW_POWER, 
        SCAN_MODE_OPPORTUNISTIC>,
    matchMode: BLEAdvertiser.<
        MATCH_MODE_AGGRESSIVE, 
        MATCH_MODE_STICKY>,
    numberOfMatches: BLEAdvertiser.<
        MATCH_NUM_FEW_ADVERTISEMENT,
        MATCH_NUM_MAX_ADVERTISEMENT, 
        MATCH_NUM_ONE_ADVERTISEMENT>,
    reportDelay: <int>
}
```

Stop scanning
```js
BLEAdvertiser.stopScan()
    .then(success => console.log("Stop Scan Successful", success))
    .catch(error => console.log("Stop Scan Error", error));
```

### Bluetooth Status

```js
const eventEmitter = new NativeEventEmitter(NativeModules.BLEAdvertiser);
onBTStatusChange = eventEmitter.addListener('onBTStatusChange', (enabled) => {
    console.log("Bluetooth status: ", enabled);
});
```

## Developing

1. Fork the repo to your GitHub user. 

2. Clone to your computer.

```bash
git clone https://github.com/vitorpamplona/react-native-ble-advertiser.git
```

3. Use the script to repack the lib into the example folder and start the app in a connected device (avoid emulators).

```bash
./repack.sh <android,ios> <devicename>
```

4. If you only change the Example files, you can re-run the example with the usual

```bash
npx react-native <run-android, run-ios --device>
```

Pull requests are welcome :) 

### Manual execution

1. Build the library with npm pack

```bash
npm pack
```

2. Install your build on the example app.

```bash
cd example
npm i $NPMFILE
```

3. Update pods 

```bash
cd ios
pod install
cd ..
```

4. Run the example

```bash
npx react-native run-android
```