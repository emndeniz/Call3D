package com.emndeniz.rtc.call3d.pushservice;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.emndeniz.rtc.call3d.utils.NotificationKeys;
import com.emndeniz.rtc.call3d.utils.Utils;
import com.emndeniz.rtc.call3d.volley.Request;
import com.emndeniz.rtc.call3d.volley.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;

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

    public void sendCallStartNotification(String remoteUserName, String sdp, Context context){
        sendCallNotification(remoteUserName,sdp,NotificationKeys.CALL_START_OFFER,context);
    }

    public void sendCallAnswerNotification(String remoteUserName, String sdp, Context context){
        sendCallNotification(remoteUserName,sdp,NotificationKeys.CALL_START_ANSWER,context);
    }


    private void sendCallNotification(String remoteUserName, String sdp, String notificationType,Context context){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put(NotificationKeys.NOTIFICATION_TITLE, NotificationKeys.CALL_NOTIFICATION); //?????????
            if(notificationType.equals(NotificationKeys.CALL_START_OFFER))
                notificationBody.put(NotificationKeys.NOTIFICATION_MESSAGE, Request.createCallStartRequestJson(remoteUserName,sdp));
            else
                notificationBody.put(NotificationKeys.NOTIFICATION_MESSAGE, Request.createCallAnswerRequestJson(remoteUserName,sdp));

            notification.put(NotificationKeys.TO, "/topics/" + Utils.getFireBaseUserTopicFormat(remoteUserName));
            notification.put(NotificationKeys.NOTIFICATION_DATA, notificationBody);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "sendCallNotification: " + e.getMessage() );
        }
        sendNotification(notification,context);
    }

    public void sendCandidate(String remoteUserName, IceCandidate iceCandidate, Context context){

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put(NotificationKeys.NOTIFICATION_TITLE, NotificationKeys.CALL_NOTIFICATION); //?????????
            notificationBody.put(NotificationKeys.NOTIFICATION_MESSAGE, Request.createIceCandidateRequestJson(remoteUserName,iceCandidate));

            notification.put(NotificationKeys.TO, "/topics/" + Utils.getFireBaseUserTopicFormat(remoteUserName));
            notification.put(NotificationKeys.NOTIFICATION_DATA, notificationBody);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "sendCandidate: " + e.getMessage() );
        }
        sendNotification(notification,context);
    }


    private void sendNotification(JSONObject notification, Context context) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                response -> Log.i(LOG_TAG, "onResponse: " + response.toString()),
                error -> Log.e(LOG_TAG, "onErrorResponse: Didn't work")){
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
