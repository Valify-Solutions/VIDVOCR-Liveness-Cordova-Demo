package cordova.plugin.vidvocr;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import com.google.gson.Gson;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.vidv.vidvocrsdk.sdk.BuilderError;
import me.vidv.vidvocrsdk.sdk.CapturedImages;
import me.vidv.vidvocrsdk.sdk.ServiceFailure;
import me.vidv.vidvocrsdk.sdk.Success;
import me.vidv.vidvocrsdk.sdk.UserExit;
import me.vidv.vidvocrsdk.sdk.VIDVOCRConfig;
import me.vidv.vidvocrsdk.sdk.VIDVOCRListener;
import me.vidv.vidvocrsdk.sdk.VIDVOCRResponse;
import me.vidv.vidvocrsdk.viewmodel.VIDVError;
import me.vidv.vidvocrsdk.viewmodel.VIDVEvent;
import me.vidv.vidvocrsdk.viewmodel.VIDVLogListener;

public class VIDVOCRActivity extends Activity {

    private CallbackContext callbackContext;
    private boolean servicesStarted = false; 
    private boolean enableLogging = false;
    private List<JSONObject> events = new ArrayList<>();
    private List<JSONObject> errors = new ArrayList<>();
    private List<CapturedImages> capturedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackContext = cordova_plugin_vidvocr.VIDVOCRPlugin.callbackContext;
        if (!servicesStarted) {
            startService();
            servicesStarted = true;
        }
    }

    private void startService() {
        VIDVOCRConfig.Builder builder = new VIDVOCRConfig.Builder();
        builder.setBaseUrl(getIntent().getExtras().getString("base_url"))
                .setAccessToken(getIntent().getExtras().getString("access_token"))
                .setBundleKey(getIntent().getExtras().getString("bundle_key"))
                .setLanguage(getIntent().getExtras().getString("language"));

        if (getIntent().hasExtra("document_verification")) {
            builder.setDocumentVerification(getIntent().getExtras().getBoolean("document_verification"));
        }
        if (getIntent().hasExtra("data_validation")) {
            builder.setDataValidation(getIntent().getExtras().getBoolean("data_validation"));
        }
        if (getIntent().hasExtra("return_data_validation_error")) {
            builder.setReturnValidationError(getIntent().getExtras().getBoolean("return_data_validation_error"));
        }
        if (getIntent().hasExtra("review_data")) {
            builder.setReviewData(getIntent().getExtras().getBoolean("review_data"));
        }
        if (getIntent().hasExtra("capture_only_mode")) {
            builder.setCaptureOnlyMode(getIntent().getExtras().getBoolean("capture_only_mode"));
        }
        if (getIntent().hasExtra("manual_capture_mode")) {
            builder.setManualCaptureMode(getIntent().getExtras().getBoolean("manual_capture_mode"));
        }
        if (getIntent().hasExtra("preview_captured_image")) {
            builder.setPreviewCapturedImage(getIntent().getExtras().getBoolean("preview_captured_image"));
        }
        if (getIntent().hasExtra("headers")) {
            builder.setHeaders((HashMap<String, String>) getIntent().getExtras().get("headers"));
        }
        if (getIntent().hasExtra("primary_color") && !getIntent().getExtras().getString("primary_color").isEmpty()) {
            builder.setPrimaryColor(Color.parseColor(getIntent().getExtras().getString("primary_color")));
        }

        if (getIntent().hasExtra("enable_logging")) {
            enableLogging = getIntent().getExtras().getBoolean("enable_logging");
            if (enableLogging) {
                builder.setLogsListener(new VIDVLogListener() {
                    @Override
                    public void onLog(VIDVEvent log) {
                        logEvent(log);
                    }

                    @Override
                    public void onLog(VIDVError log) {
                        logError(log);
                    }
                });
            }
        }

        builder.start(VIDVOCRActivity.this, new VIDVOCRListener() {
            @Override
            public void onOCRResult(VIDVOCRResponse response) {
                handleResponse(response);
            }
        });
    }

    private void logEvent(VIDVEvent log) {
        if (enableLogging) {
            try {
                JSONObject jsonObject = new JSONObject(new Gson().toJson(log));
                events.add(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void logError(VIDVError log) {
        if (enableLogging) {
            try {
                JSONObject jsonObject = new JSONObject(new Gson().toJson(log));
                errors.add(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleResponse(VIDVOCRResponse response) {
        JSONObject jsonObject = new JSONObject();
        if (enableLogging) {
            try {
                jsonObject.put("events", new Gson().toJson(events));
                jsonObject.put("errors", new Gson().toJson(errors));
            }catch (JSONException exception){
                exception.printStackTrace();
            }
        }
            if (response instanceof Success) {
                try {
                    jsonObject.put("state", "SUCCESS");
                    jsonObject.put("capturedImages", new Gson().toJson(capturedImages));
                    jsonObject.put("ocrResult", new Gson().toJson(((Success) response).getData()));
                }catch (JSONException exception){
                    exception.printStackTrace();
                }
                String jsonInString = jsonObject.toString();
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonInString);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
                finish(); // Exit this activity
            } else if (response instanceof BuilderError) {
                try {
                    jsonObject.put("state", "ERROR");
                    jsonObject.put("errorCode", ((BuilderError) response).getCode());
                    jsonObject.put("errorMessage", ((BuilderError) response).getMessage());
                }catch (JSONException exception){
                    exception.printStackTrace();
                }
                String jsonInString = jsonObject.toString();
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonInString);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
                finish(); // Exit this activity
            } else if (response instanceof ServiceFailure) {
                try {
                    jsonObject.put("state", "FAILURE");
                    jsonObject.put("errorCode", ((ServiceFailure) response).getCode());
                    jsonObject.put("errorMessage", ((ServiceFailure) response).getMessage());
                    jsonObject.put("ocrResult", new Gson().toJson(((ServiceFailure) response).getData()));
                }catch (JSONException exception){
                    exception.printStackTrace();
                }
                String jsonInString = jsonObject.toString();
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonInString);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
                finish(); // Exit this activity
            } else if (response instanceof UserExit) {
                try {
                    jsonObject.put("state", "EXIT");
                    jsonObject.put("step", ((UserExit) response).getStep());
                    jsonObject.put("ocrResult", new Gson().toJson(((UserExit) response).getData()));
                }catch (JSONException exception){
                    exception.printStackTrace();
                }
                String jsonInString = jsonObject.toString();
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonInString);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
                finish(); // Exit this activity
            } else if(response instanceof CapturedImages){
                capturedImages.add((CapturedImages) response);
            }

    }


}