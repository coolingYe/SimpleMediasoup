package com.example.simplemediasoup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.model.Peers
import com.example.simplemediasoup.model.RoomInfo
import org.json.JSONObject

class RoomStore: ViewModel() {

    val roomInfo: MutableLiveData<RoomInfo> by lazy {
        MutableLiveData<RoomInfo>().also {
            it.value = RoomInfo()
        }
    }

    val peers: MutableLiveData<Peers> by lazy {
        MutableLiveData<Peers>().also {
            it.value = Peers()
        }
    }

    val me: MutableLiveData<Peer> by lazy {
        MutableLiveData<Peer>().also {
            it.value = Peer()
        }
    }

    fun addPeer(peerId: String, peerInfo: JSONObject) {
        val value = peers.value
        value?.addPeer(peerId, peerInfo)
        peers.postValue(value)
    }

    fun addAllPeer(peersMap: MutableMap<String, Peer>) {
        peers.postValue(Peers(peersMap))
    }

    fun removePeer(peerId: String) {
        peers.value?.removePeer(peerId)
    }
}