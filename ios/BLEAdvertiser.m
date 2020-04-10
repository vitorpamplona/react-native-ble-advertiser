#import "BLEAdvertiser.h"
@import CoreBluetooth;

@implementation BLEAdvertiser

#define REGION_ID @"com.privatekit.ibeacon"

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(BLEAdvertiser)

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onDeviceFound"];
}

RCT_EXPORT_METHOD(setCompanyId: (nonnull NSNumber *)companyId){
    RCTLogInfo(@"setCompanyId function called %@", companyId);
    self->centralManager = [[CBCentralManager alloc] initWithDelegate:self queue: nil options: nil];
    self->peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil options:nil];
}

RCT_EXPORT_METHOD(broadcast: (NSString *)uid payload:(NSArray *)payload 
    resolve: (RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){

    RCTLogInfo(@"Broadcast function called %@ at %@", uid, payload);
    // Beacon Version. 
    //NSUUID *proximityUUID = [[NSUUID alloc] initWithUUIDString:uid];
    //CLBeaconRegion *beaconRegion = [[CLBeaconRegion alloc] initWithProximityUUID:proximityUUID major:1 minor:1 identifier:REGION_ID];
    //NSDictionary *advertisingData = [beaconRegion peripheralDataWithMeasuredPower:nil];
    
    NSDictionary *advertisingData = @{CBAdvertisementDataLocalNameKey : @"PrivateKit", CBAdvertisementDataServiceUUIDsKey : @[[CBUUID UUIDWithString:uid]]};

    [peripheralManager startAdvertising:advertisingData];

    resolve(@"Yay!");
}

RCT_EXPORT_METHOD(stopBroadcast:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){
    RCTLogInfo(@"stopBroadcast function called");

    [peripheralManager stopAdvertising];

    resolve(@"Yay!");
}

RCT_EXPORT_METHOD(scan: (NSArray *)payload options:(NSDictionary *)options 
    resolve: (RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){
    RCTLogInfo(@"stopBroadcast function called");
    
    [centralManager scanForPeripheralsWithServices:nil options:@{CBCentralManagerScanOptionAllowDuplicatesKey:[NSNumber numberWithBool:YES]}];
    
    resolve(@"Yay! Central Manager Created");
}

RCT_EXPORT_METHOD(stopScan:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){
    RCTLogInfo(@"stopScan function called");
    resolve(@"Yay!");

    [centralManager stopScan];
}

RCT_EXPORT_METHOD(enableAdapter){
    RCTLogInfo(@"enableAdapter function called");
}

RCT_EXPORT_METHOD(disableAdapter){
    RCTLogInfo(@"disableAdapter function called");
}

RCT_EXPORT_METHOD(getAdapterState:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){
    
    RCTLogInfo(@"enableAdapter function called");
    resolve(@"Yay!");
}

RCT_EXPORT_METHOD(isActive: 
    resolve: (RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){
    RCTLogInfo(@"isActive function called");
    resolve(@"Yay!");
}


-(void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData RSSI:(NSNumber *)RSSI {
    NSString *peripheralName = [peripheral name];
    RCTLogInfo(@"Found: %@", peripheralName);
    RCTLogInfo(@"Found: %@", [peripheral services]);

    NSArray *keys = [advertisementData allKeys];
    for (int i = 0; i < [keys count]; ++i) {
        id key = [keys objectAtIndex: i];
        NSString *keyName = (NSString *) key;
        NSObject *value = [advertisementData objectForKey: key];
        if ([value isKindOfClass: [NSArray class]]) {
            printf("   key: %s\n", [keyName cStringUsingEncoding: NSUTF8StringEncoding]);
            NSArray *values = (NSArray *) value;
            for (int j = 0; j < [values count]; ++j) {
                NSObject *aValue = [values objectAtIndex: j];
                printf("       %s\n", [[aValue description] cStringUsingEncoding: NSUTF8StringEncoding]);
                printf("       is NSData: %d\n", [aValue isKindOfClass: [NSData class]]);
            }
        } else {
            const char *valueString = [[value description] cStringUsingEncoding: NSUTF8StringEncoding];
            printf("   key: %s, value: %s\n", [keyName cStringUsingEncoding: NSUTF8StringEncoding], valueString);
        }
    }

    NSMutableDictionary *params =  [[NSMutableDictionary alloc] initWithCapacity:1];      
    NSMutableArray *paramsUUID = [[NSMutableArray alloc] init];

    NSObject *kCBAdvDataServiceUUIDs = [advertisementData objectForKey: @"kCBAdvDataServiceUUIDs"];
    if ([kCBAdvDataServiceUUIDs isKindOfClass:[NSArray class]]) {
        NSArray *uuids = (NSArray *) kCBAdvDataServiceUUIDs;
        for (int j = 0; j < [uuids count]; ++j) {
            NSObject *aValue = [uuids objectAtIndex: j];
            [paramsUUID addObject:[aValue description]];
        }
    }

    RSSI = RSSI && RSSI.intValue < 127 ? RSSI : nil;

    params[@"serviceUuids"] = paramsUUID;
    params[@"rssi"] = RSSI;
    params[@"deviceName"] = [peripheral name];
    params[@"deviceAddress"] = [peripheral identifier];
    params[@"txPower"] = [advertisementData objectForKey: @"kCBAdvDataTxPowerLevel"];
    
    [self sendEventWithName:@"onDeviceFound" body:params];
}

-(void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    NSLog(@"Check BT status");
    switch (central.state) {
        case CBCentralManagerStatePoweredOff:
            NSLog(@"CoreBluetooth BLE hardware is powered off");
            break;
        case CBCentralManagerStatePoweredOn:
            NSLog(@"CoreBluetooth BLE hardware is powered on and ready");
            break;
        case CBCentralManagerStateResetting:
            NSLog(@"CoreBluetooth BLE hardware is resetting");
            break;
        case CBCentralManagerStateUnauthorized:
            NSLog(@"CoreBluetooth BLE state is unauthorized");
            break;
        case CBCentralManagerStateUnknown:
            NSLog(@"CoreBluetooth BLE state is unknown");
            break;
        case CBCentralManagerStateUnsupported:
            NSLog(@"CoreBluetooth BLE hardware is unsupported on this platform");
            break;
        default:
            break;
    }
}

#pragma mark - CBPeripheralManagerDelegate
- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral
{
    switch (peripheral.state) {
        case CBPeripheralManagerStatePoweredOn:
            NSLog(@"%ld, CBPeripheralManagerStatePoweredOn", peripheral.state);
            break;
        case CBPeripheralManagerStatePoweredOff:
            NSLog(@"%ld, CBPeripheralManagerStatePoweredOff", peripheral.state);
            break;
        case CBPeripheralManagerStateResetting:
            NSLog(@"%ld, CBPeripheralManagerStateResetting", peripheral.state);
            break;
        case CBPeripheralManagerStateUnauthorized:
            NSLog(@"%ld, CBPeripheralManagerStateUnauthorized", peripheral.state);
            break;
        case CBPeripheralManagerStateUnsupported:
            NSLog(@"%ld, CBPeripheralManagerStateUnsupported", peripheral.state);
            break;
        case CBPeripheralManagerStateUnknown:
            NSLog(@"%ld, CBPeripheralManagerStateUnknown", peripheral.state);
            break;
        default:
            break;
    }
}


@end
  
