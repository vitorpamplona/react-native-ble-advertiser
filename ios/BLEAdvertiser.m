#import "BLEAdvertiser.h"

@implementation BLEAdvertiser

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE(BLEAdvertiser)

RCT_EXPORT_METHOD(setCompanyId: (nonnull NSNumber *)companyId){
    RCTLogInfo(@"setCompanyId function called %@", companyId);
}

RCT_EXPORT_METHOD(broadcast: (NSString *)uid payload:(NSArray *)payload 
    resolve: (RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){

    RCTLogInfo(@"Broadcast function called %@ at %@", uid, payload);
    resolve(@"Yay!");
}

RCT_EXPORT_METHOD(stopBroadcast: 
    resolve: (RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){
    RCTLogInfo(@"stopBroadcast function called");
    resolve(@"Yay!");
}

RCT_EXPORT_METHOD(scan: payload:(NSArray *)payload  
    resolve: (RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){
    RCTLogInfo(@"stopBroadcast function called");
    resolve(@"Yay!");
}

RCT_EXPORT_METHOD(stopScan: 
    resolve: (RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject){
    RCTLogInfo(@"stopScan function called");
    resolve(@"Yay!");
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


/*
export function setCompanyId(companyId: number): void;
export function broadcast(uid: String, payload: number[]): Promise<string>;
export function stopBroadcast(): Promise<string>;
export function scan(payload: number[], options?: ScanOptions): Promise<string>;
export function stopScan(): Promise<string>;
export function enableAdapter(): void;
export function disableAdapter(): void;
export function getAdapterState(): Promise<string>;
export function isActive(): Promise<boolean>;
*/
/*
string (NSString)
number (NSInteger, float, double, CGFloat, NSNumber)
boolean (BOOL, NSNumber)
array (NSArray) of any types from this list
object (NSDictionary) with string keys and values of any type from this list
function (RCTResponseSenderBlock)
*/

@end
  