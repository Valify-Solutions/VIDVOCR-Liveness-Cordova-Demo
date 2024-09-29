package cordova_plugin_vidvliveness;

import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;


/**
 * This class echoes a string called from JavaScript.
 */
public class VIDVLivenessPlugin extends CordovaPlugin {
    public static  CallbackContext callbackContext;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startLiveness")) {
            this.startLiveness(args, callbackContext);
            return true;
        }
        return false;
    }

    private void startLiveness(JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        JSONObject jsonObject = args.getJSONObject(0);
        Intent intent = new Intent("cordova.plugin.vidvliveness.VIDVLivenessActivity");
        intent.putExtra("base_url", jsonObject.get("base_url").toString());
        intent.putExtra("access_token", jsonObject.getString("access_token"));
        intent.putExtra("bundle_key", jsonObject.getString("bundle_key"));
        intent.putExtra("language", jsonObject.getString("language"));

        if (jsonObject.has("enable_smile"))
            intent.putExtra("enable_smile", jsonObject.getBoolean("enable_smile"));
        if (jsonObject.has("enable_look_left"))
            intent.putExtra("enable_look_left", jsonObject.getBoolean("enable_look_left"));
        if (jsonObject.has("enable_look_right"))
            intent.putExtra("enable_look_right", jsonObject.getBoolean("enable_look_right"));
        if (jsonObject.has("enable_close_eyes"))
            intent.putExtra("enable_close_eyes", jsonObject.getBoolean("enable_close_eyes"));
        if (jsonObject.has("liveness_number_of_failed_trials"))
            intent.putExtra("liveness_number_of_failed_trials", jsonObject.getInt("liveness_number_of_failed_trials"));
        if (jsonObject.has("liveness_number_of_instructions"))
            intent.putExtra("liveness_number_of_instructions", jsonObject.getInt("liveness_number_of_instructions"));
        if (jsonObject.has("liveness_time_per_action"))
            intent.putExtra("liveness_time_per_action", jsonObject.getInt("liveness_time_per_action"));
        if (jsonObject.has("facematch_ocr_transactionId"))
            intent.putExtra("facematch_ocr_transactionId", jsonObject.getString("facematch_ocr_transactionId"));


        if (jsonObject.has("primary_color"))
            intent.putExtra("primary_color", jsonObject.getString("primary_color"));
        if (jsonObject.has("enable_voiceover"))
            intent.putExtra("enable_voiceover", jsonObject.getBoolean("enable_voiceover"));
        if (jsonObject.has("show_error_message"))
            intent.putExtra("show_error_message", jsonObject.getBoolean("show_error_message"));
       
        if (!args.isNull(1)) {
            intent.putExtra("facematch_image", (byte[]) args.get(1));
        }
        if (!args.isNull(2)) {
         
            intent.putExtra("headers",  new Gson().fromJson(String.valueOf(args.getJSONObject(2)), HashMap.class));
        }

        cordova.startActivityForResult(this, intent, 1);
    }
}
