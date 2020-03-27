# react-native-ble-advertiser [![npm version](https://img.shields.io/npm/v/react-native-ble-advertiser.svg?style=flat)](https://www.npmjs.com/package/react-native-ble-advertiser) [![npm downloads](https://img.shields.io/npm/dm/react-native-ble-advertiser.svg?style=flat)](https://www.npmjs.com/package/react-native-ble-advertiser) [![GitHub issues](https://img.shields.io/github/issues/vitorpamplona/react-native-ble-advertiser.svg?style=flat)](https://github.com/vitorpamplona/react-native-ble-advertiser/issues)

Bluetooth Advertiser for React Native. 

## Requirements
RN 0.60+

## Supported Platforms
- Android (API 21+)

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
````

## Example

### Advertiser

Import the module

```js
import AndroidBLEAdvertiserModule from 'react-native-ble-advertiser'
```

Define your company ID and broadcast your UUID with additional data. Start: 

```js
AndroidBLEAdvertiserModule.setCompanyId(0x00); // Your Company's Code
AndroidBLEAdvertiserModule.broadcast(UUID, [ManufacturerData]) // Your UUID and additional manufacturer data. 
.then((success) => {
    console.log("Successful", success);
}).catch(error => console.log(error));
```

Stop broadcasting

```js
AndroidBLEAdvertiserModule.stopBroadcast()
.then((success) => {
    console.log("Stop Broadcast Successful", success);
}).catch(error => {
    console.log("Stop Broadcast Error", error); 
});
```

### Scanner

Import the modules

```js
import AndroidBLEAdvertiserModule from 'react-native-ble-advertiser'
import { NativeEventEmitter, NativeModules } from 'react-native';
```

Define your company ID and additional data (Scanner fitlers inbound based on these). 

```js
AndroidBLEAdvertiserModule.setCompanyId(0x00); // Your Company's Code
AndroidBLEAdvertiserModule.scan([ManufacturerData], {}) // manufacturer data and options
.then((success) => {
    console.log("Scan Successful", success);
}).catch(error => {
    console.log("Scan Error", error); 
});
```

Collect devices through ReactNative events. 

```js
const eventEmitter = new NativeEventEmitter(NativeModules.AndroidBLEAdvertiserModule);
eventEmitter.addListener('onDeviceFound', (event) => {
    console.log(event) // "device data"
});
```

Stop scannig. 

```js
AndroidBLEAdvertiserModule.stopScan()
.then((sucess) => {
    console.log(this.state.uuid, "Stop Scan Successful", sucess);
}).catch(error => {
    console.log(this.state.uuid, "Stop Scan Error", error); 
});
```