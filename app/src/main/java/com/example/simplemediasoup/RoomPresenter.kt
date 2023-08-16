package com.example.simplemediasoup

import android.annotation.SuppressLint
import android.view.Display
import android.widget.TextView
import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.rtc.RoomClient
import com.example.simplemediasoup.utils.Utils.getRandomString
import org.json.JSONObject
import org.webrtc.VideoTrack

class RoomPresenter(
    private val mView: RoomContract.View,
    private val mInteractor: RoomInteractor
) : RoomContract.Presenter {

    private var mRoomClient: RoomClient? = null

    override fun createRTC(roomId: String, peerId: String, displayName: String, microphoneEnable: Boolean, cameraEnable: Boolean) {
        mView.initViews()
        val roomClient = RoomClient(mView.getContext(), mInteractor.getRoomViewModel(), roomId, peerId, displayName, microphoneEnable, cameraEnable)
        mInteractor.getRoomViewModel().roomClient = roomClient
        this.mRoomClient = roomClient
        roomClient.join()
    }

    override fun getRoomClient(): RoomClient? = mRoomClient

    override fun sendMessage(message: String) {
        mRoomClient?.sendChatMessage(message)
    }

    override fun switchCamera() {
        mRoomClient?.switchCamera()
    }

    override fun close() {
        mRoomClient?.close()
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

    override fun enableCamera() {
        mRoomClient?.enableCamera()
    }

    override fun enableMicrophone() {
        mRoomClient?.enableMicrophone()
    }

    override fun disableCamera() {
        mRoomClient?.disableCamera()
    }

    override fun disableMicrophone() {
        mRoomClient?.disableMicrophone()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setTextImageTopDrawable(resourceId: Int, textView: TextView) {
        val drawTop = mView.getContext().getDrawable(resourceId)
        textView.setCompoundDrawablesWithIntrinsicBounds(null, drawTop, null, null)
    }

}