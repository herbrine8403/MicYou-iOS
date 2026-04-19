#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MicYouAudioBridge : NSObject
- (instancetype)init;
- (void)startWithHost:(NSString *)host port:(NSInteger)port;
- (void)stop;
@end

NS_ASSUME_NONNULL_END
