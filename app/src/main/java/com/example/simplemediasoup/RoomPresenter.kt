package com.example.simplemediasoup

import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.rtc.RoomClient
import org.json.JSONObject
import org.webrtc.VideoTrack

class RoomPresenter(
    private val mView: RoomContract.View,
    private val mInteractor: RoomInteractor
) : RoomContract.Presenter {

    private var roomClient: RoomClient? = null

    override fun createRTC() {
        mView.initViews()
        val roomClient = RoomClient(mView.getContext(), mInteractor.getRoomViewModel(),"happyroom", "be5ln5de", forceH264 = false, forceVP9 = false)
        this.roomClient = roomClient
        roomClient.join()
    }

    override fun getRoomClient(): RoomClient? = roomClient

    override fun sendMessage(message: String) {
        roomClient?.sendChatMessage(message)
    }

    override fun switchCamera() {
        roomClient?.switchCamera()
    }

    override fun close() {
        roomClient?.close()
    }

    fun addPeer(peerId: String, peerInfo: JSONObject) {
        mInteractor.addPeer(peerId, peerInfo)
    }

    fun setLocalVideoTrack(track: VideoTrack) {
        mInteractor.setLocalVideoTrack(track)
    }

    fun getPeers(): List<Peer> {
        return mInteractor.getPeers()
    }

}