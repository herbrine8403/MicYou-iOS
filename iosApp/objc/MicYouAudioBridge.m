#import "MicYouAudioBridge.h"

@implementation MicYouAudioBridge

- (instancetype)init {
    self = [super init];
    if (self) {
    }
    return self;
}

- (void)startWithHost:(NSString *)host port:(NSInteger)port {
    NSLog(@"MicYouAudioBridge start host=%@ port=%ld", host, (long)port);
}

- (void)stop {
    NSLog(@"MicYouAudioBridge stop");
}

@end
