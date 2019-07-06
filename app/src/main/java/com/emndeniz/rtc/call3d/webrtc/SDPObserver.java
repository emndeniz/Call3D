package com.emndeniz.rtc.call3d.webrtc;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.concurrent.ExecutorService;

public class SDPObserver implements SdpObserver {

    private final String LOG_TAG = SDPObserver.class.getSimpleName();

    private ExecutorService executorService;
    private WebRTCClient.PeerConnectionEvents events;
    SDPObserver(ExecutorService executorService, WebRTCClient.PeerConnectionEvents events){
        this.executorService = executorService;
        this.events = events;
    }
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(LOG_TAG,"onCreateSuccess : " + sessionDescription.description);
        events.onLocalDescription(sessionDescription);
    }

    @Override
    public void onSetSuccess() {
        Log.d(LOG_TAG,"onSetSuccess : " );
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(LOG_TAG,"onCreateFailure : " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(LOG_TAG,"onSetFailure : " + s);
    }
}
