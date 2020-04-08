#import <React/RCTBridgeModule.h>
#import <React/RCTLog.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEAdvertiser : NSObject <RCTBridgeModule, CBCentralManagerDelegate, CBPeripheralManagerDelegate, CBPeripheralDelegate> {
    CBCentralManager *centralManager;
    CBPeripheralManager *peripheralManager;
}

@end
  
