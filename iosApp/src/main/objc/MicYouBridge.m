#import "MicYouBridge.h"

@implementation MicYouBridge

- (instancetype)init {
    self = [super init];
    return self;
}

- (NSString *)prepareAudio {
    return @"Audio prepared";
}

- (NSString *)startCapture {
    return @"Capture started";
}

- (NSString *)stopCapture {
    return @"Capture stopped";
}

@end
