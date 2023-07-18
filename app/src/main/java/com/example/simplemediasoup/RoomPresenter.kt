package com.example.simplemediasoup

import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.rtc.RoomClient
import org.json.JSONObject

class RoomPresenter(
    private val mView: RoomContract.View,
    private val mInteractor: RoomInteractor
) : RoomContract.Presenter {

    private var roomClient: RoomClient? = null

    override fun createRTC() {
        mView.initViews()
        val roomClient = RoomClient(mView.getContext(), this,"happyroom", "be5ln5de", forceH264 = false, forceVP9 = false)
        this.roomClient = roomClient
        roomClient.join()
    }

    override fun getRoomClient(): RoomClient? = roomClient

    override fun sendMessage(message: String) {
        roomClient?.sendChatMessage(message)
    }

    fun addPeer(peerId: String, peerInfo: JSONObject) {
        mInteractor.addPeer(peerId, peerInfo)
    }

    fun addAllPeer(peersMap: MutableMap<String, Peer>) {
        mInteractor.addAllPeer(peersMap)
    }

}