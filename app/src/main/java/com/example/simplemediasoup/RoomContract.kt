package com.example.simplemediasoup

import android.content.Context
import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.rtc.RoomClient
import org.json.JSONObject

class RoomContract {

    interface View {
        fun getContext(): Context
        fun initViews()
    }

    interface Presenter {
        fun createRTC()
        fun getRoomClient(): RoomClient?
        fun sendMessage(message: String)
    }

    interface Interactor {
        fun addPeer(peerId: String, peerInfo: JSONObject)
        fun addAllPeer(peersMap: MutableMap<String, Peer>)
    }
}