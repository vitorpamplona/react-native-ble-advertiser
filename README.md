# react-native-ble-advertiser
Bluetooth Advertiser for React Native. 


````

AndroidBLEAdvertiserModule.setCompanyId(<0x00>); // Your Company's Code
AndroidBLEAdvertiserModule.broadcastPacket(<UUID>, []) // Your UUID and additional data. 
.then((sucess) => {
    console.log("Sucessful", sucess);
}).catch(error => console.log(error));
``à