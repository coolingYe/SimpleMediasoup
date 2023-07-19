package com.example.simplemediasoup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simplemediasoup.model.Notify
import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.model.Peers
import com.example.simplemediasoup.model.RoomInfo
import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.DataConsumer
import org.webrtc.VideoTrack

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

    val localVideoTrack: MutableLiveData<VideoTrack> by lazy {
        MutableLiveData<VideoTrack>().also {
            it.value = null
        }
    }

    val notify: MutableLiveData<Notify> by lazy { 
        MutableLiveData<Notify>().also { 
            it.value = null
        }
    }
    
    fun addNotify(title: String, text:String) {
        notify.postValue(Notify(title = title, text = text))
    }

    fun addPeer(peerId: String, peerInfo: JSONObject) {
        val value = peers.value
        value?.addPeer(peerId, peerInfo)
        peers.postValue(value)
    }

    fun addDataConsumer(peerId: String, dataConsumer: DataConsumer) {
        val value = peers.value
        value?.addDataConsumer(peerId, dataConsumer)
        peers.postValue(value)
    }

    fun addAllPeer(peersMap: MutableMap<String, Peer>) {
        peers.postValue(Peers(peersMap))
    }

    fun removePeer(peerId: String) {
        peers.value?.removePeer(peerId)
    }

    fun getPeers(): List<Peer>? {
       return peers.value?.getAllPeer()
    }

    fun setLocalVideoTrack(videoTrack: VideoTrack) {
        localVideoTrack.postValue(videoTrack)
    }

    fun addConsumer(peerId: String, type: String, consumer: Consumer, remotelyPaused: Boolean) {
        val value = peers.value
        value?.addConsumer(peerId, consumer)
        peers.postValue(value)
    }
}