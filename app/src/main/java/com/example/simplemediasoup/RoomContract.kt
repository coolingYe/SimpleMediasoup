package com.example.simplemediasoup

import android.content.Context
import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.rtc.RoomClient
import org.json.JSONObject
import org.webrtc.VideoTrack

class RoomContract {

    interface View {
        fun getContext(): Context
        fun initViews()
    }

    interface Presenter {
        fun createRTC()
        fun getRoomClient(): RoomClient?
        fun sendMessage(message: String)
        fun close()
        fun switchCamera()
    }

    interface Interactor {
        fun addPeer(peerId: String, peerInfo: JSONObject)
        fun setLocalVideoTrack(videoTrack: VideoTrack)
        fun getPeers(): List<Peer>
        fun getRoomViewModel(): RoomStore
    }
}