# react-native-ble-advertiser [![npm version](https://img.shields.io/npm/v/react-native-ble-advertiser.svg?style=flat)](https://www.npmjs.com/package/react-native-ble-advertiser) [![npm downloads](https://img.shields.io/npm/dm/react-native-ble-advertiser.svg?style=flat)](https://www.npmjs.com/package/react-native-ble-advertiser) [![GitHub issues](https://img.shields.io/github/issues/vitorpamplona/react-native-ble-advertiser.svg?style=flat)](https://github.com/vitorpamplona/react-native-ble-advertiser/issues)

Bluetooth Advertiser & Scanner for React Native. This is in a very early development focused in contact tracing applications. Please use with caution.

## Requirements
RN 0.60+

## Supported Platforms
- Android (API 21+)

## Features / TO-DO List

- [x] Android Advertiser (v0.0.2)
- [x] Android Scanner (v0.0.6)
- [ ] Android Background Service
- [ ] iOS Advertiser (v0.0.11)
- [x] iOS Scanner
- [ ] iOS Background Service

## Install

```bash
npm install react-native-ble-advertiser --save
```

### Setting up the Android Project

1. In build.gradle of app module make sure that min SDK version is at least 23:
```groovy
android {
    ...
    defaultConfig {
        minSdkVersion 21
        ...       
```

2. In AndroidManifest.xml, add Bluetooth permissions and update <uses-sdk/>:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    ...
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

### Setting up the iOS Project

On the plist file, include: 
```xml
	<key>NSLocationWhenInUseUsageDescription</key>
	<string>We log your location to allow comparisons with other user's locations in a privacy-preserving way.</string>
	<key>NSBluetoothAlwaysUsageDescription</key>
	<string>We broadcast and scan for bluetooth signals in a way to track all phones nearby you in a privacy-preserving way.</string>
	<key>UIBackgroundModes</key>
	<array>
		<string>bluetooth-central</string>
		<string>bluetooth-peripheral</string>
        ...
	</array>
	<key>UIRequiredDeviceCapabilities</key>
	<array>
		<string>bluetooth-le</string>
        ....
	</array>
```

## Example

### Advertiser

Import the module

```js
import BLEAdvertiser from 'react-native-ble-advertiser'
```

Define your company ID and broadcast your UUID with additional data. Start: 

```js
BLEAdvertiser.setCompanyId(0x00); // Your Company's Code
BLEAdvertiser.broadcast(UUID, [ManufacturerData]) // The UUID you would like to advertise and additional manufacturer data. 
    .then(success => console.log('Broadcasting Sucessful', success))
    .catch(error => console.log('Broadcasting Error', error));
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

Define your company ID and additional data (Scanner fitlers inbound based on these). 

```js
BLEAdvertiser.setCompanyId(0x00); // Your Company's Code
BLEAdvertiser.scan([ManufacturerData], {}) // manufacturer data and options
    .then(success => console.log("Scan Successful", success))
    .catch(error => console.log("Scan Error", error)); 
```

Collect devices through ReactNative events. 

```js
const eventEmitter = new NativeEventEmitter(NativeModules.BLEAdvertiser);
eventEmitter.addListener('onDeviceFound', (event) => {
    console.log(event) // "device data"
});
```

Stop scannig. 

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