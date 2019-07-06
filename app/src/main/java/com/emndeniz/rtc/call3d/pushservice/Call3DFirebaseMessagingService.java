package com.emndeniz.rtc.call3d.pushservice;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class Call3DFirebaseMessagingService extends FirebaseMessagingService {



        private final String LOG_TAG = "FBMessagingService";
        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            //TODO not using this, we may want to remove it
            Log.d(LOG_TAG,"onMessageReceived, data:" + remoteMessage.getData() );
        }


}
