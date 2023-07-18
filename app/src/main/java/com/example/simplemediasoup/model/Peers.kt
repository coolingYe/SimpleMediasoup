package com.example.simplemediasoup.model

import org.json.JSONObject
import java.util.*

class Peers(
    var peersInfo: MutableMap<String, Peer> = Collections.synchronizedMap(LinkedHashMap())
) {

    fun addPeer(peerId: String, peerInfo: JSONObject) {
        peersInfo[peerId] = Peer().get(peerInfo)
    }

    fun removePeer(peerId: String) {
        peersInfo.remove(peerId)
    }

    fun setPeerDisplayName(peerId: String, displayName: String) {
        val peer = peersInfo[peerId]
        peer?.let {
            it.displayName = displayName
        }
    }

    fun getAllPeer(): List<Peer> {
        val peer = arrayListOf<Peer>()
        peersInfo.forEach{
            peer.add(it.value)
        }
        return peer
    }

    fun clear() {
        peersInfo.clear()
    }
}
