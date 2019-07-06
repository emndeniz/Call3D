package com.emndeniz.rtc.call3d.webrtc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerConnectionClient {
    private static final String LOG_TAG = PeerConnectionClient.class.getSimpleName();

    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_TRACK_TYPE = "video";
    PeerConnectionFactory.Options options;
    PeerConnectionFactory peerConnectionFactory;
    //@Nullable
   // private PeerConnectionFactory factory;
    @Nullable
    private PeerConnection peerConnection;
    @Nullable
    private SessionDescription localSdp; // either offer or answer SDP
    @Nullable
    private List<IceCandidate> queuedRemoteCandidates;
    private final PCObserver pcObserver;
    private final SDPObserver sdpObserver;
    @Nullable
    private VideoTrack localVideoTrack;
    @Nullable
    private VideoTrack remoteVideoTrack;
    private AudioTrack localAudioTrack;
    @Nullable
    private RtpSender localVideoSender;

    @Nullable
    private VideoSink localRender;
    @Nullable private List<VideoSink> remoteSinks;

    private MediaConstraints audioConstraints;
    private MediaConstraints videoConstraints;
    private MediaConstraints sdpMediaConstraints;

    @Nullable
    private VideoSource videoSource;
    private AudioSource audioSource;

    @Nullable
    private VideoCapturer videoCapturer;

    private final int VIDEO_WIDTH = 720;
    private final int VIDEO_HEIGHT = 480;
    private final int VIDEO_FPS = 30;

    private Context appContext;
    private EglBase eglBase;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean useCamera2;

    public PeerConnectionClient(Context context, EglBase eglBase, WebRTCClient.PeerConnectionEvents events, boolean useCamera2) {
        options = new PeerConnectionFactory.Options();
        appContext = context;
        this.eglBase = eglBase;
        pcObserver = new PCObserver(executor,events);
        sdpObserver = new SDPObserver(executor,events);
        this.useCamera2 = useCamera2;
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                       // .setFieldTrials(fieldTrials)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
    }

    public void createPeerConnectionFactory() {
        if (peerConnectionFactory != null) {
            throw new IllegalStateException("PeerConnectionFactory has already been constructed");
        }
        //executor.execute(this::createPeerConnectionFactoryInternal);
        createPeerConnectionFactoryInternal();
    }

    private void createPeerConnectionFactoryInternal() {

        final AudioDeviceModule adm = createJavaAudioDevice();
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                eglBase.getEglBaseContext(), true , true);
        decoderFactory = new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        Log.d(LOG_TAG, "Peer connection factory created.");
    }

    public void createPeerConnection(VideoSink localRender, List<VideoSink> remoteSinks ,VideoCapturer videoCapturer){
        if(peerConnectionFactory == null){
            Log.e(LOG_TAG, "Peerconnection factory is not created");
            return;
        }
        this.localRender = localRender;
        this.videoCapturer = videoCapturer;
        this.remoteSinks = remoteSinks;
        createMediaConstraintsInternal();
        createPeerConnectionInternal();
    }

    private void createMediaConstraintsInternal(){
        audioConstraints = new MediaConstraints();
        // Create SDP constraints.
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    }

    private void createPeerConnectionInternal(){
        if (peerConnectionFactory == null) {
            Log.e(LOG_TAG, "Peerconnection is not created");
            return;
        }
        Log.d(LOG_TAG, "Create peer connection internal.");

        queuedRemoteCandidates = new ArrayList<>();

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(getIceServers());

        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = true;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, pcObserver);


        List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
        peerConnection.addTrack(createVideoTrack(videoCapturer), mediaStreamLabels);
        // We can add the renderers right away because we don't need to wait for an
        // answer to get the remote track.
        remoteVideoTrack = getRemoteVideoTrack();
        remoteVideoTrack.setEnabled(true);
        for (VideoSink remoteSink : remoteSinks) {
            remoteVideoTrack.addSink(remoteSink);
        }

        peerConnection.addTrack(createAudioTrack(), mediaStreamLabels);
        //findVideoSender();

    }

    public void setRemoteDescription(final String sdp, SessionDescription.Type type) {
       // executor.execute(() -> {
            if (peerConnection == null) {
                return;
            }


            Log.d(LOG_TAG, "Set remote SDP.");
            SessionDescription sdpRemote = new SessionDescription(type, sdp);
            peerConnection.setRemoteDescription(sdpObserver, sdpRemote);
        //});
    }
    public void setLocalDescription( SessionDescription sdp) {
        // executor.execute(() -> {
        if (peerConnection == null) {
            return;
        }


        Log.d(LOG_TAG, "Set local SDP.");
        //SessionDescription sdpRemote = new SessionDescription(, sdp);
        peerConnection.setLocalDescription(sdpObserver, sdp);
        //});
    }

    public void onIceCandidate(IceCandidate candidate){
        peerConnection.addIceCandidate(candidate);
    }


    private ArrayList<PeerConnection.IceServer> getIceServers(){
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer stun1 =  PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        PeerConnection.IceServer stun2 =  PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer();
        PeerConnection.IceServer stun3 =  PeerConnection.IceServer.builder("stun:74.125.143.127:19302").createIceServer();
        PeerConnection.IceServer turn1 =  PeerConnection.IceServer.builder("turn:numb.viagenie.ca").setUsername("webrtc@live.com").setPassword("muazkh").createIceServer();
        PeerConnection.IceServer turn2 =  PeerConnection.IceServer.builder("turn:64.233.161.127:19305").setUsername("CKWWyegFEgYypr1jeb4Yzc/s6OMTIICjBQ").setPassword("F8YKr69X//l12z8fu+0HwtWualg=").createIceServer();

        iceServers.add(stun1);
        iceServers.add(stun2);
        iceServers.add(stun3);
        iceServers.add(turn1);
        iceServers.add(turn2);

        return iceServers;
    }

    @Nullable
    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        final SurfaceTextureHelper surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        videoSource = peerConnectionFactory.createVideoSource(capturer.isScreencast());
        capturer.initialize(surfaceTextureHelper, appContext, videoSource.getCapturerObserver());
        capturer.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FPS);

        localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(true);
        localVideoTrack.addSink(localRender);
        return localVideoTrack;
    }

    //TODO For this app purposes it should be more than one track

    // Returns the remote VideoTrack, assuming there is only one.
    private @Nullable VideoTrack getRemoteVideoTrack() {
        for (RtpTransceiver transceiver : peerConnection.getTransceivers()) {
            MediaStreamTrack track = transceiver.getReceiver().track();
            if (track instanceof VideoTrack) {
                return (VideoTrack) track;
            }
        }
        return null;
    }

    private AudioTrack createAudioTrack() {
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true); //TODO for mute cases add a logic here
        return localAudioTrack;
    }

    public void createOffer() {
        //executor.execute(() -> {
            if (peerConnection != null) {
                Log.d(LOG_TAG, "PC Create OFFER");
                peerConnection.createOffer(sdpObserver, sdpMediaConstraints);
            }
       // });
    }

    public void createAnswer() {
        //executor.execute(() -> {
            if (peerConnection != null) {
                Log.d(LOG_TAG, "PC create ANSWER");
                peerConnection.createAnswer(sdpObserver, sdpMediaConstraints);
            }
       // });
    }
    private void findVideoSender() {
        for (RtpSender sender : peerConnection.getSenders()) {
            if (sender.track() != null) {
                String trackType = sender.track().kind();
                if (trackType.equals(VIDEO_TRACK_TYPE)) {
                    Log.d(LOG_TAG, "Found video sender.");
                    localVideoSender = sender;
                }
            }
        }
    }
    private AudioDeviceModule createJavaAudioDevice() {

        // Set audio record error callbacks.
        JavaAudioDeviceModule.AudioRecordErrorCallback audioRecordErrorCallback = new JavaAudioDeviceModule.AudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                Log.e(LOG_TAG, "onWebRtcAudioRecordInitError: " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                    JavaAudioDeviceModule.AudioRecordStartErrorCode errorCode, String errorMessage) {
                Log.e(LOG_TAG, "onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
                Log.e(LOG_TAG, "onWebRtcAudioRecordError: " + errorMessage);
                reportError(errorMessage);
            }
        };

        JavaAudioDeviceModule.AudioTrackErrorCallback audioTrackErrorCallback = new JavaAudioDeviceModule.AudioTrackErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                Log.e(LOG_TAG, "onWebRtcAudioTrackInitError: " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                    JavaAudioDeviceModule.AudioTrackStartErrorCode errorCode, String errorMessage) {
                Log.e(LOG_TAG, "onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                Log.e(LOG_TAG, "onWebRtcAudioTrackError: " + errorMessage);
                reportError(errorMessage);
            }
        };
        return JavaAudioDeviceModule.builder(appContext)
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .createAudioDeviceModule();
    }

    private void reportError(final String errorMessage) {
        Log.e(LOG_TAG, "Peerconnection error: " + errorMessage);
    }

    public VideoCapturer createVideoCapturer() {
        final org.webrtc.VideoCapturer videoCapturer;

        Logging.d(LOG_TAG, "Creating capturer using camera1 API.");
        if(useCamera2){
            videoCapturer = createCameraCapturer(new Camera2Enumerator(appContext));
        }else{
            videoCapturer = createCameraCapturer(new Camera1Enumerator());
        }


        if (videoCapturer == null) {
            Log.e(LOG_TAG,"Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(LOG_TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(LOG_TAG, "Creating front facing camera capturer.");
                org.webrtc.VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(LOG_TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(LOG_TAG, "Creating other camera capturer.");
                org.webrtc.VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

}
