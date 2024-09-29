package cordova_plugin_vidvocr;

import android.content.Intent;

import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class VIDVOCRPlugin extends CordovaPlugin {
    public static CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startOCR")) {
            this.startOCR(args, callbackContext);
            return true;
        }
        return false;
    }

    private void startOCR(JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        JSONObject jsonObject = args.getJSONObject(0);
        Intent intent = new Intent("cordova.plugin.vidvocr.VIDVOCRActivity");
        intent.putExtra("base_url", jsonObject.getString("base_url"));
        intent.putExtra("access_token", jsonObject.getString("access_token"));
        intent.putExtra("bundle_key", jsonObject.getString("bundle_key"));
        intent.putExtra("language", jsonObject.getString("language"));

        if (jsonObject.has("document_verification"))
            intent.putExtra("document_verification", jsonObject.getBoolean("document_verification"));
        if (jsonObject.has("data_validation"))
            intent.putExtra("data_validation", jsonObject.getBoolean("data_validation"));
        if (jsonObject.has("return_data_validation_error"))
            intent.putExtra("return_data_validation_error", jsonObject.getBoolean("return_data_validation_error"));
        if (jsonObject.has("review_data"))
            intent.putExtra("review_data", jsonObject.getBoolean("review_data"));
        if (jsonObject.has("capture_only_mode"))
            intent.putExtra("capture_only_mode", jsonObject.getBoolean("capture_only_mode"));
        if (jsonObject.has("manual_capture_mode"))
            intent.putExtra("manual_capture_mode", jsonObject.getBoolean("manual_capture_mode"));
        if (jsonObject.has("preview_captured_image"))
            intent.putExtra("preview_captured_image", jsonObject.getBoolean("preview_captured_image"));
        if (!args.isNull(1)) {
            intent.putExtra("headers", new Gson().fromJson(String.valueOf(args.getJSONObject(1)), HashMap.class));
        }
//        if (jsonObject.has("ssl_certificate"))
//            intent.putExtra("ssl_certificate", jsonObject.get("ssl_certificate"));
        if (jsonObject.has("primary_color"))
            intent.putExtra("primary_color", jsonObject.getString("primary_color"));
      if (jsonObject.has("enable_logging"))
            intent.putExtra("enable_logging", jsonObject.getBoolean("enable_logging"));
        cordova.startActivityForResult(this, intent, 1);
    }
}
