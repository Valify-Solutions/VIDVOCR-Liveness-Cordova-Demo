/********* VIDVOCR.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "YourProductModuleName-Swift.h" // change YourProductModuleName with your product module name that you can find in build settings


@interface VIDVOCRPlugin : CDVPlugin {
    // Member variables go here.
}


- (void)startOCR:(CDVInvokedUrlCommand*)command;
@end


@implementation VIDVOCRPlugin

// calling the swift code from obejctive c
- (void)startOCR:(CDVInvokedUrlCommand*)command
{
    __block CDVPluginResult* pluginResult = nil;
    NSArray *arr = command.arguments;
    VIDVOCRInitializer *instance = [VIDVOCRInitializer new];
    [instance startOCRWithSender:self.viewController argArr:arr completion:^(BOOL success, NSString * _Nonnull response) {
        if (success) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:response];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:response];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            
        }
    }];
    
}
@end
