#import "MicYouAudioBridge.h"
#import <AVFoundation/AVFoundation.h>

@interface MicYouAudioBridge ()
@property (nonatomic, strong) AVAudioEngine *audioEngine;
@property (nonatomic, strong) AVAudioSession *audioSession;
@property (nonatomic, copy) void (^frameCallback)(NSData *pcmData, int sampleRate, int channels);
@property (nonatomic, assign) BOOL capturing;
@property (nonatomic, assign) int sampleRate;
@property (nonatomic, assign) int channels;
@end

@implementation MicYouAudioBridge

- (instancetype)init {
    self = [super init];
    if (self) {
        _audioEngine = [[AVAudioEngine alloc] init];
        _audioSession = [AVAudioSession sharedInstance];
        _capturing = NO;
        _sampleRate = 48000;
        _channels = 1;
    }
    return self;
}

- (void)setFrameCallback:(void (^)(NSData *pcmData, int sampleRate, int channels))callback {
    self.frameCallback = callback;
}

- (NSString *)prepareAudio {
    NSError *error = nil;

    // Configure audio session for recording
    BOOL success = [self.audioSession setCategory:AVAudioSessionCategoryPlayAndRecord
                                             mode:AVAudioSessionModeDefault
                                          options:AVAudioSessionCategoryOptionDefaultToSpeaker |
                                                  AVAudioSessionCategoryOptionAllowBluetooth |
                                                  AVAudioSessionCategoryOptionAllowBluetoothA2DP
                                            error:&error];
    if (!success) {
        return [NSString stringWithFormat:@"Failed to set category: %@", error.localizedDescription];
    }

    success = [self.audioSession setActive:YES error:&error];
    if (!success) {
        return [NSString stringWithFormat:@"Failed to activate session: %@", error.localizedDescription];
    }

    // Get preferred sample rate
    self.sampleRate = (int)self.audioSession.sampleRate;
    if (self.sampleRate <= 0) {
        self.sampleRate = 48000;
    }

    // Configure input node format
    AVAudioInputNode *inputNode = self.audioEngine.inputNode;
    AVAudioFormat *inputFormat = [inputNode outputFormatForBus:0];
    self.channels = (int)inputFormat.channelCount;

    // Install tap on input node
    AVAudioFormat *recordingFormat = [[AVAudioFormat alloc] initWithCommonFormat:AVAudioPCMFormatInt16
                                                                       sampleRate:self.sampleRate
                                                                         channels:(AVAudioChannelCount)self.channels
                                                                      interleaved:YES];

    if (!recordingFormat) {
        return @"Failed to create recording format";
    }

    __weak typeof(self) weakSelf = self;
    [inputNode installTapOnBus:0
                    bufferSize:480  // 10ms at 48kHz mono
                        format:recordingFormat
                         block:^(AVAudioPCMBuffer *buffer, AVAudioTime *when) {
        [weakSelf processAudioBuffer:buffer];
    }];

    return [NSString stringWithFormat:@"Audio prepared: %dHz, %dch", self.sampleRate, self.channels];
}

- (NSString *)startCapture {
    if (self.capturing) {
        return @"Already capturing";
    }

    NSError *error = nil;
    BOOL success = [self.audioEngine startAndReturnError:&error];
    if (!success) {
        return [NSString stringWithFormat:@"Failed to start engine: %@", error.localizedDescription];
    }

    self.capturing = YES;
    return [NSString stringWithFormat:@"Capture started: %dHz, %dch", self.sampleRate, self.channels];
}

- (NSString *)stopCapture {
    if (!self.capturing) {
        return @"Not capturing";
    }

    [self.audioEngine.inputNode removeTapOnBus:0];
    [self.audioEngine stop];

    NSError *error = nil;
    [self.audioSession setActive:NO withOptions:AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation error:&error];

    self.capturing = NO;
    return @"Capture stopped";
}

- (BOOL)isCapturing {
    return self.capturing;
}

#pragma mark - Private

- (void)processAudioBuffer:(AVAudioPCMBuffer *)buffer {
    if (!self.frameCallback) return;

    int frameLength = (int)buffer.frameLength;
    if (frameLength == 0) return;

    // Convert PCM buffer to NSData
    int16_t *int16Data = buffer.int16ChannelData[0];
    int byteLength = frameLength * self.channels * sizeof(int16_t);
    NSData *pcmData = [NSData dataWithBytes:int16Data length:byteLength];

    self.frameCallback(pcmData, self.sampleRate, self.channels);
}

@end
