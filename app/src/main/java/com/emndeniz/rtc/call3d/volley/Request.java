package com.emndeniz.rtc.call3d.volley;

import android.support.annotation.Nullable;
import android.util.Log;

import com.emndeniz.rtc.call3d.utils.NotificationKeys;
import com.emndeniz.rtc.call3d.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class Request {

    private final static String LOG_TAG = Request.class.getSimpleName();

    public static JSONObject createCallStartRequestJson(String remoteUser, String sdp){
        JSONObject data = new JSONObject();
        JSONObject callNotification = new JSONObject();
        try {
            data.put(NotificationKeys.FROM , Utils.getLocalUserName());
            data.put(NotificationKeys.TO,remoteUser);
            data.put(NotificationKeys.NOTIFICATION_TYPE,NotificationKeys.CALL_NOTIFICATION);

            callNotification.put(NotificationKeys.CALL_NOTIFICATION_TYPE,NotificationKeys.CALL_START_OFFER);
            callNotification.put(NotificationKeys.SDP,sdp);

            data.put(NotificationKeys.CALL_NOTIFICATION,callNotification);
        }catch (JSONException e){
            Log.d(LOG_TAG,"createCallRequestJson failed to create , error: " + e.getLocalizedMessage());
        }
        return data;
    }

    public static JSONObject createCallAnswerRequestJson(String remoteUser, String sdp){
        JSONObject data = new JSONObject();
        JSONObject callNotification = new JSONObject();
        try {
            data.put(NotificationKeys.FROM , Utils.getLocalUserName());
            data.put(NotificationKeys.TO,remoteUser);
            data.put(NotificationKeys.NOTIFICATION_TYPE,NotificationKeys.CALL_NOTIFICATION);

            callNotification.put(NotificationKeys.CALL_NOTIFICATION_TYPE,NotificationKeys.CALL_START_ANSWER);
            callNotification.put(NotificationKeys.SDP,sdp);

            data.put(NotificationKeys.CALL_NOTIFICATION,callNotification);
        }catch (JSONException e){
            Log.d(LOG_TAG,"createCallAnswerRequestJson failed to create , error: " + e.getLocalizedMessage());
        }
        return data;
    }

    @Nullable
    public static JSONObject getNotificationMessage(String notificationStr){
        JSONObject message = null;
        try {
            message = new JSONObject(notificationStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG,"getNotificationMessage failed with JSON exception, error:" + e.getLocalizedMessage());
        }
        return message;
    }
    @Nullable
    public static JSONObject getCallRequestFromJson(JSONObject jsonObject){
        JSONObject callRequestJson = null;
        try {
            callRequestJson = jsonObject.getJSONObject(NotificationKeys.CALL_NOTIFICATION);
        } catch (JSONException e) {
            Log.e(LOG_TAG,"getCallRequestFromJJSON failed with JSON exception, error:" + e.getLocalizedMessage());
        }
        return callRequestJson;
    }

    @Nullable
    public static SessionDescription getSessionDescriptionFromCallRequestJson(JSONObject jsonObject){
        SessionDescription sessionDescription = null;

        try {
            String sdp = jsonObject.getString(NotificationKeys.SDP);
            String type = jsonObject.getString(NotificationKeys.CALL_NOTIFICATION_TYPE);

            if(type.equals(NotificationKeys.CALL_START_OFFER)){
                sessionDescription =  new SessionDescription(SessionDescription.Type.OFFER,sdp);
            }else if (type.equals(NotificationKeys.CALL_START_ANSWER)){
                sessionDescription =  new SessionDescription(SessionDescription.Type.ANSWER,sdp);
            }else{
                //TODO we may want to parse also iceCandidate in here
                Log.e(LOG_TAG,"getSessionDescriptionFromCallRequestJson, invalid type received. Type:" + type );
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG,"getCallRequestFromJJSON failed with JSON exception, error:" + e.getLocalizedMessage());
        }
        return sessionDescription;
    }

    public static JSONObject createIceCandidateRequestJson(String remoteUser, IceCandidate candidate){
        JSONObject data = new JSONObject();
        JSONObject callNotification = new JSONObject();
        JSONObject iceCandidate =  new JSONObject();
        try {
            data.put(NotificationKeys.FROM , Utils.getLocalUserName());
            data.put(NotificationKeys.TO,remoteUser);
            data.put(NotificationKeys.NOTIFICATION_TYPE,NotificationKeys.CALL_NOTIFICATION);

            callNotification.put(NotificationKeys.CALL_NOTIFICATION_TYPE,NotificationKeys.CALL_CANDIDATE);

            iceCandidate.put(NotificationKeys.CANDIDATE_M_LINE_INDEX,candidate.sdpMLineIndex);
            iceCandidate.put(NotificationKeys.CANDIDATE_ID,candidate.sdpMid);
            iceCandidate.put(NotificationKeys.CANDIDATE_SDP,candidate.sdp);

            callNotification.put(NotificationKeys.CANDIDATE,iceCandidate);

            data.put(NotificationKeys.CALL_NOTIFICATION,callNotification);
        }catch (JSONException e){
            Log.d(LOG_TAG,"createCallAnswerRequestJson failed to create , error: " + e.getLocalizedMessage());
        }
        return data;
    }


    @Nullable
    public static IceCandidate getICECandidateFromJSON(JSONObject jsonObject){
        IceCandidate iceCandidate = null;
        try {
            String id = jsonObject.getString(NotificationKeys.CANDIDATE_ID);
            int mLineIndex = jsonObject.getInt(NotificationKeys.CANDIDATE_M_LINE_INDEX);
            String sdp = jsonObject.getString(NotificationKeys.CANDIDATE_SDP);
            iceCandidate =  new IceCandidate(id,mLineIndex,sdp);

        } catch (JSONException e) {
            Log.e(LOG_TAG,"getICECandidateFromJSON failed with json exception, error:" + e.getLocalizedMessage());
        }
        return iceCandidate;
    }
}
