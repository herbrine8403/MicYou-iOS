#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MicYouBridge : NSObject
- (instancetype)init;
- (NSString *)prepareAudio;
- (NSString *)startCapture;
- (NSString *)stopCapture;
@end

NS_ASSUME_NONNULL_END
