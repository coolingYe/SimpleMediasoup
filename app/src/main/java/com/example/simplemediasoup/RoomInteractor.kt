package com.example.simplemediasoup

import com.example.simplemediasoup.model.Peer
import org.json.JSONObject

class RoomInteractor(private val roomStore: RoomStore) : RoomContract.Interactor {

    override fun addPeer(peerId: String, peerInfo: JSONObject) {
        roomStore.addPeer(peerId, peerInfo)
    }

    override fun addAllPeer(peersMap: MutableMap<String, Peer>) {
        roomStore.addAllPeer(peersMap)
    }

}