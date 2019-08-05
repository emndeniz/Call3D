package com.emndeniz.rtc.call3d;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emndeniz.rtc.call3d.pushservice.FireBaseNotificationSender;
import com.emndeniz.rtc.call3d.services.CallService;
import com.emndeniz.rtc.call3d.utils.Utils;
import com.emndeniz.rtc.call3d.webrtc.PeerConnectionClient;
import com.emndeniz.rtc.call3d.webrtc.WebRTCClient;

import org.webrtc.Camera2Enumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CallFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CallFragment extends Fragment implements WebRTCClient.PeerConnectionEvents {

    private static final String LOG_TAG = CallFragment.class.getSimpleName();
    private static final String REMOTE_USER = "REMOTE_USER";
    private static final String IS_OUTGOING_CALL = "IsOutgoingCAll";
    private static final String REMOTE_SDP = "remoteSDP"; // TODO: Move it to call service




    private String remoteUserName;
    private boolean isOutGoingCall;
    private String remoteSDP;

    private OnFragmentInteractionListener mListener;

    @Nullable
    private SurfaceViewRenderer remoteVideoView;
    @Nullable
    private SurfaceViewRenderer localVideoView;
    final EglBase eglBase = EglBase.create();
    PeerConnectionClient peerConnectionClient;


    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    @Nullable private List<VideoSink> remoteSinks = new ArrayList<>();;

    @Override
    public void onLocalDescription(SessionDescription sdp) {
        peerConnectionClient.setLocalDescription(sdp);
        if(sdp.type == SessionDescription.Type.OFFER){
            sendCallRequest(sdp.description);
        }else{
            sendAnswer(sdp.description);
        }

    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        sendIceCandidate(candidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {

    }

    @Override
    public void onIceConnected() {

    }

    @Override
    public void onIceDisconnected() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }

    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(LOG_TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }
    public CallFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param remoteUser Parameter 1.
     * @return A new instance of fragment CallFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CallFragment newInstance(String remoteUser, boolean isOutGoingCall, String remoteSDP) {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        args.putString(REMOTE_USER, remoteUser);
        args.putBoolean(IS_OUTGOING_CALL,isOutGoingCall);
        args.putString(REMOTE_SDP,remoteSDP);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            remoteUserName = getArguments().getString(REMOTE_USER);
            isOutGoingCall = getArguments().getBoolean(IS_OUTGOING_CALL);
            remoteSDP = getArguments().getString(REMOTE_SDP);

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_call, container, false);

        initViews(rootView);
        initVideoViews();

        //Bundle arguments = getArguments();
        //boolean isOutgoingCall = arguments.getBoolean(getString(R.string.is_outgoing_call_flag));

        //String remoteSDP = arguments.getString(getString(R.string.is_outgoing_call_flag),null);
        startPeerConnection();

        CallService.getInstance(getActivity()).setPeerConnectionClient(peerConnectionClient);
        if(isOutGoingCall){
            peerConnectionClient.createOffer();
        }else{
            peerConnectionClient.setRemoteDescription(remoteSDP, SessionDescription.Type.OFFER);
            peerConnectionClient.createAnswer();

           // peerConnectionClient.createAnswer();
        }


        return rootView;
    }

    private void initViews(View rootView){
        remoteVideoView = rootView.findViewById(R.id.remote_video_view);
        localVideoView = rootView.findViewById(R.id.local_video_view);
    }

    private void initVideoViews(){


        remoteVideoView.init(eglBase.getEglBaseContext(), null);
        //remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        localVideoView.init(eglBase.getEglBaseContext(),null);
        //localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);


        remoteVideoView.setZOrderMediaOverlay(true);
        //remoteVideoView.setEnableHardwareScaler(true /* enabled */);
        localVideoView.setZOrderMediaOverlay(true);

        localProxyVideoSink.setTarget(localVideoView);
        remoteProxyRenderer.setTarget(remoteVideoView);
        //fullscreenRenderer.setMirror(isSwappedFeeds);
        //pipRenderer.setMirror(!isSwappedFeeds);

    }

    private void startPeerConnection(){
        peerConnectionClient = new PeerConnectionClient(getActivity().getApplicationContext(),eglBase,this
        ,useCamera2());
        peerConnectionClient.createPeerConnectionFactory();


        remoteSinks.add(remoteProxyRenderer);

        VideoCapturer videoCapturer = peerConnectionClient.createVideoCapturer();

        peerConnectionClient.createPeerConnection(localProxyVideoSink,remoteSinks,videoCapturer);

    }

    private String getLocalUserName(){

        SharedPreferences sharedPref = getActivity().getSharedPreferences(getResources().getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        return sharedPref.getString(getResources().getString(R.string.user_name),null);
    }
    private void sendCallRequest(String sdp){
        FireBaseNotificationSender.newInstance().sendCallStartNotification(remoteUserName,sdp,getActivity());
    }
    private void sendAnswer(String sdp){
        Log.d(LOG_TAG, "Sending sdp to remote : " + remoteUserName);
        FireBaseNotificationSender.newInstance().sendCallAnswerNotification(remoteUserName,sdp,getActivity());
    }

    private void sendIceCandidate(IceCandidate candidate){
        Log.d(LOG_TAG, "Sending candidate to remote : " + remoteUserName);
        FireBaseNotificationSender.newInstance().sendCandidate(remoteUserName,candidate,getActivity());
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(getActivity()) && getActivity().getIntent().getBooleanExtra(Utils.EXTRA_CAMERA2, true);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
