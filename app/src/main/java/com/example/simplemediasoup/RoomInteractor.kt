package com.example.simplemediasoup

import com.example.simplemediasoup.model.Peer
import org.json.JSONObject
import org.webrtc.VideoTrack

class RoomInteractor(private val roomStore: RoomStore) : RoomContract.Interactor {

    override fun addPeer(peerId: String, peerInfo: JSONObject) {
        roomStore.addPeer(peerId, peerInfo)
    }

    override fun setLocalVideoTrack(videoTrack: VideoTrack) {
        roomStore.localVideoTrack.postValue(videoTrack)
    }

    override fun getPeers(): List<Peer> = roomStore.peers.value?.getAllPeer() ?: emptyList()

    override fun getRoomViewModel(): RoomStore = roomStore

}