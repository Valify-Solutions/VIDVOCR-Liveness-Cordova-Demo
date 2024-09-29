package cordova.plugin.vidvliveness;

import android.app.Activity;
import android.graphics.Color;
import me.vidv.vidvlivenesssdk.sdk.CapturedActions;
import me.vidv.vidvlivenesssdk.sdk.VIDVLivenessConfig;
import me.vidv.vidvlivenesssdk.sdk.VIDVLivenessResult;
import me.vidv.vidvlivenesssdk.sdk.VIDVLivenessException;
import me.vidv.vidvlivenesssdk.sdk.VIDVLivenessListener;
import me.vidv.vidvlivenesssdk.sdk.VIDVLivenessResponse;
import me.vidv.vidvlivenesssdk.sdk.BuilderError;
import me.vidv.vidvlivenesssdk.sdk.ServiceFailure;
import me.vidv.vidvlivenesssdk.sdk.Success;
import me.vidv.vidvlivenesssdk.sdk.UserExit;
import android.util.Log;
import com.google.gson.Gson;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cordova_plugin_vidvliveness.VIDVLivenessPlugin;

public class VIDVLivenessActivity extends Activity {

    private CallbackContext callbackContext;
    @Override
    protected void onStart() {
        super.onStart();
     callbackContext = VIDVLivenessPlugin.callbackContext;
        startService();

    }

    private void startService() {
         VIDVLivenessConfig.Builder livenessConfig;
        livenessConfig = new VIDVLivenessConfig.Builder()
                .setBaseUrl(getIntent().getExtras().getString("base_url"))
                .setAccessToken(getIntent().getExtras().getString("access_token"))
                .setBundleKey(getIntent().getExtras().getString("bundle_key"))
                .setLanguage(getIntent().getExtras().getString("language"));

       if (getIntent().hasExtra("enable_smile")) {
                if (!getIntent().getExtras().getBoolean("enable_smile"))
                     livenessConfig.withoutSmile();
            }
        if (getIntent().hasExtra("enable_look_left")) {
                if (!getIntent().getExtras().getBoolean("enable_look_left"))
                     livenessConfig.withoutLookLeft();
            }
        if (getIntent().hasExtra("enable_look_right")) {
                if (!getIntent().getExtras().getBoolean("enable_look_right"))
                     livenessConfig.withoutLookRight();
            }
        if (getIntent().hasExtra("enable_close_eyes")) {
                if (!getIntent().getExtras().getBoolean("enable_close_eyes"))
                     livenessConfig.withoutCloseEyes();
            }

        
        if (getIntent().hasExtra("liveness_number_of_instructions")) {
                livenessConfig
                        .setNumberOfInstructions(getIntent().getExtras().getInt("liveness_number_of_instructions"));
            }
        if (getIntent().hasExtra("liveness_number_of_failed_trials")) {
                livenessConfig
                        .setFailTrials(getIntent().getExtras().getInt("liveness_number_of_failed_trials"));
            }
        if (getIntent().hasExtra("liveness_time_per_action")) {
                livenessConfig.setInstructionTimer(getIntent().getExtras().getInt("liveness_time_per_action"));
            }
        if (getIntent().hasExtra("enable_voiceover")) {
                if(!getIntent().getExtras().getBoolean("enable_voiceover"))
                     livenessConfig.withoutVoiceOver();
            }
        if (getIntent().hasExtra("facematch_ocr_transactionId")) {
                livenessConfig
                        .setFrontTransactionId(getIntent().getExtras().getString("facematch_ocr_transactionId"));
            }
        else if (getIntent().hasExtra("facematch_image")) {
                livenessConfig
                        .setFaceMatchImage((byte[]) getIntent().getExtras().get("facematch_image"));
            }

        if (getIntent().hasExtra("show_error_message")) {
                livenessConfig.showErrorDialogs(getIntent().getExtras().getBoolean("show_error_message"));
            }
      
        if (getIntent().hasExtra("primary_color") && !getIntent().getExtras().getString("primary_color").equals("")) {
                livenessConfig.setPrimaryColor(Color.parseColor(getIntent().getExtras().getString("primary_color")));
            }

        if (getIntent().hasExtra("headers")) {
            livenessConfig.setHeaders((HashMap<String, String>) getIntent().getExtras().get("headers"));
        }

            livenessConfig.start(this, new VIDVLivenessListener() {

                @Override
                public void onLivenessResult(VIDVLivenessResponse livenessResponse) {
                    if(livenessResponse instanceof Success){

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("state", "SUCCESS");
                            jsonObject.put("livenessResult", ((Success) livenessResponse).vidvLivenessResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String jsonInString = new Gson().toJson(jsonObject);

                        PluginResult resultado = new PluginResult(PluginResult.Status.OK, jsonInString);

                        resultado.setKeepCallback(true);
                        callbackContext.sendPluginResult(resultado);

                        finish();// Exit of this activity !
                    }else if (livenessResponse instanceof BuilderError){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("state", "ERROR");
                            jsonObject.put("errorCode", ((BuilderError) livenessResponse).errorCode);
                            jsonObject.put("errorMessage", ((BuilderError) livenessResponse).errorMessage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String jsonInString = new Gson().toJson(jsonObject);

                        PluginResult resultado = new PluginResult(PluginResult.Status.ERROR, jsonInString);

                        resultado.setKeepCallback(true);
                        callbackContext.sendPluginResult(resultado);

                        finish();// Exit of this activity !
                    }else if (livenessResponse instanceof ServiceFailure){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("state", "FAILURE");
                            jsonObject.put("livenessResult", ((ServiceFailure) livenessResponse).vidvLivenessResult);
                            jsonObject.put("errorCode", ((ServiceFailure) livenessResponse).errorCode);
                            jsonObject.put("errorMessage", ((ServiceFailure) livenessResponse).errorMessage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String jsonInString = new Gson().toJson(jsonObject);

                        PluginResult resultado = new PluginResult(PluginResult.Status.ERROR, jsonInString);

                        resultado.setKeepCallback(true);
                        callbackContext.sendPluginResult(resultado);

                        finish();// Exit of this activity !
                    }else if (livenessResponse instanceof UserExit){
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("state", "EXIT");
                            jsonObject.put("livenessResult", ((UserExit) livenessResponse).vidvLivenessResult);
                            jsonObject.put("step", ((UserExit) livenessResponse).step);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String jsonInString = new Gson().toJson(jsonObject);

                        PluginResult resultado = new PluginResult(PluginResult.Status.ERROR, jsonInString);

                        resultado.setKeepCallback(true);
                        callbackContext.sendPluginResult(resultado);

                        finish();// Exit of this activity !
                    }else if (livenessResponse instanceof CapturedActions){
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("state", "CAPTURED_IMAGES");
                        jsonObject.put("capturedImage", ((CapturedActions) livenessResponse).detectedFace);
                      
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jsonInString = new Gson().toJson(jsonObject);

                    PluginResult resultado = new PluginResult(PluginResult.Status.OK , jsonInString);

                    resultado.setKeepCallback(true);
                    callbackContext.sendPluginResult(resultado);
                }
                }

            });

         

    }
}