#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * MicYouAudioBridgeOC - Objective-C bridge for iOS audio capture.
 *
 * This class wraps AVAudioEngine to capture microphone input and
 * forward PCM frames to the Kotlin side via a callback block.
 */
@interface MicYouAudioBridgeOC : NSObject

- (instancetype)init;

/**
 * Set the callback block that receives PCM audio frames.
 * @param callback Block called on each audio frame with NSData containing PCM16LE samples,
 *                 sample rate in Hz, and channel count.
 */
- (void)setFrameCallback:(void (^)(NSData *pcmData, int sampleRate, int channels))callback;

/**
 * Prepare the audio session. Must be called before startCapture.
 * Configures AVAudioSession for recording.
 * @return Status message.
 */
- (NSString *)prepareAudio;

/**
 * Start capturing audio from the microphone.
 * @return Status message.
 */
- (NSString *)startCapture;

/**
 * Stop capturing audio.
 * @return Status message.
 */
- (NSString *)stopCapture;

@end

NS_ASSUME_NONNULL_END
