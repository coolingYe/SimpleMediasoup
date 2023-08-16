package com.example.simplemediasoup

import android.content.Context
import android.widget.TextView
import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.rtc.RoomClient
import com.example.simplemediasoup.utils.Utils.getRandomString
import org.json.JSONObject
import org.webrtc.VideoTrack

class RoomContract {

    interface View {
        fun getContext(): Context
        fun initViews()
    }

    interface Presenter {
        fun getRoomClient(): RoomClient?
        fun sendMessage(message: String)
        fun close()
        fun switchCamera()
        fun disableCamera()
        fun disableMicrophone()
        fun setTextImageTopDrawable(resourceId: Int, textView: TextView)
        fun enableMicrophone()
        fun enableCamera()
        fun createRTC(roomId: String ="happyroom",
                      peerId: String = getRandomString(8),
                      displayName: String = getRandomString(8),
                      microphoneEnable: Boolean = true,
                      cameraEnable: Boolean = true
        )
    }

    interface Interactor {
        fun addPeer(peerId: String, peerInfo: JSONObject)
        fun setLocalVideoTrack(videoTrack: VideoTrack)
        fun getPeers(): List<Peer>
        fun getRoomViewModel(): RoomStore
    }
}