var exec = require('cordova/exec');

var VIDVLivenessPlugin = function() {};

VIDVLivenessPlugin.prototype.startLiveness= function(object,faceMatchImage,headers,success,error) {
    cordova.exec(success, error, "VIDVLivenessPlugin","startLiveness", [object,faceMatchImage,headers]);
};

startLiveness = function(object,faceMatchImage,headers,success,error) {
    
       cordova.exec(success, error, "VIDVLivenessPlugin","startLiveness", [object,faceMatchImage,headers]);
     };

var vidvLivenessPlugin = new VIDVLivenessPlugin();
module.exports = vidvLivenessPlugin;
