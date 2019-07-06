package com.emndeniz.rtc.call3d.pushservice;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.emndeniz.rtc.call3d.volley.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FireBaseNotificationSender {

    private final String LOG_TAG = "FBNotificationSender";
    private final  String FIREBASE_SERVER_KEY = "key=AAAAFX7OHOE:APA91bGFMC3N37WR55RuG05GyLFJNOMEsNWtuK2VJd9UbeG3ZDNs8-gcBcPoCE4b74ERzvQ0gNXgWwzgW-iqtKeRcOpfxoq2fbiEUzYbtoTwh-HUgLR2CwL7iUPye3ezR8DzZvJAD8Cr";
    //private final  String FIREBASE_SERVER_KEY = "key=AIzaSyBQtA9WsDIoMiN4zR4eXhxjk1Avvjrm244";

    private final  String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private final  String CONTENT_TYPE = "application/json";

    //public static final  String TOPIC_NAME = "call3DTopic";

    private static FireBaseNotificationSender  mInstance;
    private  FireBaseNotificationSender(){

    }

    public static FireBaseNotificationSender newInstance(){
        if(mInstance == null){
            mInstance = new FireBaseNotificationSender();
        }
        return mInstance;
    }

    public void sendCallNotification(String userTopic, String title, String sdp, Context context){

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", title);
            notifcationBody.put("message", sdp);

            notification.put("to", "/topics/" + userTopic);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "sendCallNotification: " + e.getMessage() );
        }
        sendNotification(notification,context);
    }

    public void sendCandidate(String userTopic, String title, JSONObject candidates, Context context){

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", title);
            notifcationBody.put("message", candidates.toString());

            notification.put("to", "/topics/" + userTopic);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "sendCandidate: " + e.getMessage() );
        }
        sendNotification(notification,context);
    }


    private void sendNotification(JSONObject notification, Context context) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(LOG_TAG, "onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", FIREBASE_SERVER_KEY);
                params.put("Content-Type", CONTENT_TYPE);
                return params;
            }
        };

        Log.d(LOG_TAG,"sendNotification: " + jsonObjectRequest.toString());
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }


}
