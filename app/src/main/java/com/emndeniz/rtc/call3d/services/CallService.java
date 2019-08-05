package com.emndeniz.rtc.call3d.services;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.Log;

import com.emndeniz.rtc.call3d.CallFragment;
import com.emndeniz.rtc.call3d.R;
import com.emndeniz.rtc.call3d.webrtc.PeerConnectionClient;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.ArrayList;

public class CallService {

    private static final String LOG_TAG = CallService.class.getSimpleName();
    private static  CallService mInstance;
    private Context context;
    private PeerConnectionClient peerConnectionClient;

    private ArrayList<IceCandidate> queedCandidates = new ArrayList<>();
    private CallService(Context context){
        this.context = context;
    }

    public static CallService getInstance(Context context){
        if (mInstance == null){
            mInstance = new CallService(context);
        }
        return mInstance;
    }


    public void setPeerConnectionClient(PeerConnectionClient peerConnectionClient){
        this.peerConnectionClient = peerConnectionClient;
    }

    public void incomingCallReceived(String sdp, String remoteUserName){

        Activity activity = (Activity) context;
        FragmentManager manager = activity.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        CallFragment callFragment = CallFragment.newInstance(remoteUserName,false,sdp);
       // Bundle args = new Bundle();
        //args.putString(activity.getString(R.string.remote_sdp), sdp);
        //callFragment.setArguments(args);
        transaction.replace(R.id.main_fragment_container,callFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    public void answerReceived(String sdp){
        Log.d(LOG_TAG,"answerReceived");
        peerConnectionClient.setRemoteDescription(sdp, SessionDescription.Type.ANSWER);
    }
    public void addRemoteIceCandidates(IceCandidate iceCandidate){
        Log.d(LOG_TAG,"addRemoteIceCandidates");
        peerConnectionClient.onIceCandidate(iceCandidate);

    }
}
