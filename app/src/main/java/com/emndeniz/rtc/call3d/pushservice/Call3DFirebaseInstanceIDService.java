package com.emndeniz.rtc.call3d.pushservice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.emndeniz.rtc.call3d.MainNavigationActivity;
import com.emndeniz.rtc.call3d.R;
import com.emndeniz.rtc.call3d.services.CallService;
import com.emndeniz.rtc.call3d.utils.NotificationKeys;
import com.emndeniz.rtc.call3d.utils.Utils;
import com.emndeniz.rtc.call3d.volley.Request;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class Call3DFirebaseInstanceIDService  extends FirebaseMessagingService {

    private final String LOG_TAG = "FBInstanceServiceId";
    private final String ADMIN_CHANNEL_ID ="admin_channel";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(LOG_TAG ,"onNewToken: " + token);
        getSharedPreferences(getResources().getString(R.string.shared_pref_key), MODE_PRIVATE).edit().putString(getResources().getString(R.string.fire_base_token), token).apply();

    }

    private void processNotification(JSONObject message) throws JSONException {
        CallService callService = CallService.getInstance(getApplication());
            String to = message.getString(NotificationKeys.TO);
            if(!to.equals(Utils.getLocalUserName())){
                Log.e(LOG_TAG,"Received notification not sent for current user, to:" + to);
                return;
            }
            String remoteUser = message.getString(NotificationKeys.FROM);
            Log.d(LOG_TAG,"Message Received from user " + remoteUser);


            if(message.getString(NotificationKeys.NOTIFICATION_TYPE).equals(NotificationKeys.CALL_NOTIFICATION)){
                JSONObject callNotification = Request.getCallRequestFromJson(message);
                String type = callNotification.getString(NotificationKeys.CALL_NOTIFICATION_TYPE);
                switch (type) {
                    case NotificationKeys.CALL_START_OFFER:
                        Log.d(LOG_TAG,"Offer received");
                        callService.incomingCallReceived(callNotification.getString(NotificationKeys.SDP), remoteUser);
                        break;
                    case NotificationKeys.CALL_START_ANSWER:
                        Log.d(LOG_TAG,"Answer received");
                        callService.answerReceived(callNotification.getString(NotificationKeys.SDP));
                        break;
                    case NotificationKeys.CALL_CANDIDATE:
                        Log.d(LOG_TAG,"Candidate received");
                        JSONObject candidateJson = callNotification.getJSONObject(NotificationKeys.CANDIDATE);
                        callService.addRemoteIceCandidates(Request.getICECandidateFromJSON(candidateJson));
                        break;
                    default:
                        Log.e(LOG_TAG, "Unknown call notification type, type: " + type);
                        break;
                }
            }else{
                Log.w(LOG_TAG,"Received notification type not have any usage, type:" + message.getString(NotificationKeys.CALL_NOTIFICATION_TYPE));
            }

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(LOG_TAG, "onMessageReceived, data:" + remoteMessage.getData() );
        JSONObject message = Request.getNotificationMessage(remoteMessage.getData().get(NotificationKeys.NOTIFICATION_MESSAGE));

        if(message != null){
            try {
                processNotification(message);
            } catch (JSONException e) {
                Log.e(LOG_TAG,"failed to parse Call Notification e:" + e.getLocalizedMessage());
            }
        }

    }


    /*
        For debug purpose
     */
    private void createLocalNotification(RemoteMessage remoteMessage){
        final Intent intent = new Intent(this, MainNavigationActivity.class);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);

      /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
      */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels(notificationManager);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_menu_camera);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setLargeIcon(largeIcon)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message"))
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager){
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to devie notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

}
