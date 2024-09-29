cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "cordova-plugin-vidvliveness.VIDVLivenessPlugin",
      "file": "plugins/cordova-plugin-vidvliveness/www/VIDVLivenessPlugin.js",
      "pluginId": "cordova-plugin-vidvliveness",
      "clobbers": [
        "VIDVLivenessPlugin"
      ]
    },
    {
      "id": "cordova-plugin-vidvocr.VIDVOCRPlugin",
      "file": "plugins/cordova-plugin-vidvocr/www/VIDVOCRPlugin.js",
      "pluginId": "cordova-plugin-vidvocr",
      "clobbers": [
        "VIDVOCRPlugin"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-vidvliveness": "1.0.1",
    "cordova-plugin-vidvocr": "2.1.7"
  };
});