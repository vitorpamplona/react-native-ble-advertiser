# react-native-ble-advertiser [![npm version](https://img.shields.io/npm/v/react-native-ble-advertiser.svg?style=flat)](https://www.npmjs.com/package/react-native-ble-advertiser) [![npm downloads](https://img.shields.io/npm/dm/react-native-ble-advertiser.svg?style=flat)](https://www.npmjs.com/package/react-native-ble-advertiser) [![GitHub issues](https://img.shields.io/github/issues/vitorpamplona/react-native-ble-advertiser.svg?style=flat)](https://github.com/vitorpamplona/react-native-ble-advertiser/issues)

Bluetooth Advertiser for React Native. 

## Requirements
RN 0.60+

## Supported Platforms
- Android (API 23+)

## Install

1. ```npm install react-native-ble-advertiser --save```

##### Android

1. In build.gradle of app module make sure that min SDK version is at least 23:
```groovy
android {
    ...
    defaultConfig {
        minSdkVersion 23
        ...       
```

2. In AndroidManifest.xml, add Bluetooth permissions and update <uses-sdk/>:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    ...
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION"/>
````

## Example

Import the module

```js
import AndroidBLEAdvertiserModule from 'react-native-ble-advertiser'
```

Define your company ID and broadcast your UUID with additional data. 

```js
AndroidBLEAdvertiserModule.setCompanyId(<0x00>); // Your Company's Code
AndroidBLEAdvertiserModule.broadcastPacket(<UUID>, []) // Your UUID and additional data. 
.then((sucess) => {
    console.log("Sucessful", sucess);
}).catch(error => console.log(error));
```
