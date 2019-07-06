package com.emndeniz.rtc.call3d.webrtc;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

public interface WebRTCClient {



    /**
     * Send offer SDP to the other participant.
     */
    void sendOfferSdp(final SessionDescription sdp);

    /**
     * Send answer SDP to the other participant.
     */
    void sendAnswerSdp(final SessionDescription sdp);

    /**
     * Send Ice candidate to the other participant.
     */
    void sendLocalIceCandidate(final IceCandidate candidate);

    /**
     * Send removed ICE candidates to the other participant.
     */
    void sendLocalIceCandidateRemovals(final IceCandidate[] candidates);

    /**
     * End current call
     */
    void endCall();


    /**
     * Peer connection events.
     */
    public interface PeerConnectionEvents {
        /**
         * Callback fired once local SDP is created and set.
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is disconnected (IceConnectionState is
         * DISCONNECTED).
         */
        void onIceDisconnected();

        /**
         * Callback fired once DTLS connection is established (PeerConnectionState
         * is CONNECTED).
         */
        void onConnected();

        /**
         * Callback fired once DTLS connection is disconnected (PeerConnectionState
         * is DISCONNECTED).
         */
        void onDisconnected();

        /**
         * Callback fired once peer connection is closed.
         */
        void onPeerConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);
    }


    interface WebRTCSignalingEvents {
        /**
         * Callback fired once when peers connected
         */
        void onConnected();
        /**
         * Callback fired once remote SDP is received.
         */
        void onRemoteDescription(final SessionDescription sdp);

        /**
         * Callback fired once remote Ice candidate is received.
         */
        void onRemoteIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once remote Ice candidate removals are received.
         */
        void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once channel is closed.
         */
        void onChannelClose();

        /**
         * Callback fired once channel error happened.
         */
        void onChannelError(final String description);
    }


}
