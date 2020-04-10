#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTLog.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEAdvertiser : RCTEventEmitter <RCTBridgeModule, CBCentralManagerDelegate, CBPeripheralManagerDelegate, CBPeripheralDelegate> {
    CBCentralManager *centralManager;
    CBPeripheralManager *peripheralManager;
}

@end
  
