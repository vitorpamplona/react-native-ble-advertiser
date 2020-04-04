#import "BLEAdvertiserEmitter.h"

@implementation BLEAdvertiserEmitter
  
RCT_EXPORT_MODULE(BLEAdvertiserEmitter)

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onDeviceFound"];
}

@end