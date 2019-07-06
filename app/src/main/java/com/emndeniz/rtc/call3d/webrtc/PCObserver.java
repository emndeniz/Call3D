package com.emndeniz.rtc.call3d.webrtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;

import java.util.concurrent.ExecutorService;

public class PCObserver implements PeerConnection.Observer {

    private final String LOG_TAG = PCObserver.class.getSimpleName();
    private ExecutorService executorService;
    private WebRTCClient.PeerConnectionEvents peerConnectionEvents;
    PCObserver(ExecutorService executorService,WebRTCClient.PeerConnectionEvents peerConnectionEvents){
        this.executorService = executorService;
        this.peerConnectionEvents = peerConnectionEvents;
    }
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(LOG_TAG,"onSignalingChange : " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(LOG_TAG,"onIceConnectionChange : " + iceConnectionState);
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
        Log.d(LOG_TAG,"onIceConnectionChange : " + newState);
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(LOG_TAG,"onIceConnectionReceivingChange : " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(LOG_TAG,"onIceGatheringChange : " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(LOG_TAG,"onIceCandidate : " + iceCandidate);
        peerConnectionEvents.onIceCandidate(iceCandidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(LOG_TAG,"onIceCandidatesRemoved : " + iceCandidates);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(LOG_TAG,"onAddStream : " + mediaStream);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(LOG_TAG,"onRemoveStream : " + mediaStream);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(LOG_TAG,"onDataChannel : " + dataChannel);
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(LOG_TAG,"onRenegotiationNeeded : ");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(LOG_TAG,"onAddTrack : " + rtpReceiver + ", " + mediaStreams);
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        Log.d(LOG_TAG,"onTrack : " + transceiver);
    }
}
