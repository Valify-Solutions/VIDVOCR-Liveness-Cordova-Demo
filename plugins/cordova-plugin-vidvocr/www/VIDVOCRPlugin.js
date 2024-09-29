var exec = require('cordova/exec');

var VIDVOCRPlugin = function() {};
// for android
VIDVOCRPlugin.prototype.startOCR= function(object,headers,success,error) {
    cordova.exec(success, error, "VIDVOCRPlugin","startOCR", [object,headers]);
};
// for iOS
startOCR = function(object,headers,success,error) {
       cordova.exec(success, error, "VIDVOCRPlugin","startOCR", [object,headers]);
     };

var vidvOCRPlugin = new VIDVOCRPlugin();
module.exports = vidvOCRPlugin;
