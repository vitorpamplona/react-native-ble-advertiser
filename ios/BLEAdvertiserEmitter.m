#import "BLEAdvertiserEmitter.h"

@implementation ModuleWithEmitter
  
- (NSArray<NSString *> *)supportedEvents {
    return @[@"onDeviceFound"];
}

@end