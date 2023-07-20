package com.example.simplemediasoup.model

import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.DataConsumer
import java.util.*

class Peers(
    var peersInfo: MutableMap<String, Peer> = Collections.synchronizedMap(LinkedHashMap())
) {

    fun addPeer(peerId: String, peerInfo: JSONObject) {
        peersInfo[peerId] = Peer().get(peerInfo)
    }

    fun addDataConsumer(peerId: String, consumer: DataConsumer) {
        val peer = getPeer(peerId)
        peer?.dataConsumers?.add(consumer.id)
    }

    fun addConsumer(peerId: String, consumer: Consumer) {
        val peer = getPeer(peerId)
        peer?.consumers?.add(consumer.id)
    }

    fun removePeer(peerId: String) {
        peersInfo.remove(peerId)
    }

    fun removeConsumer(peerId: String, consumerId: String) {
        val peer = getPeer(peerId)
        peer?.consumers?.remove(consumerId)
    }

    fun removeDataConsumer(peerId: String, consumerId: String) {
        val peer = getPeer(peerId)
        peer?.dataConsumers?.remove(consumerId)
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

    fun getPeer(peerId: String?): Peer? {
        return peersInfo[peerId]
    }

    fun clear() {
        peersInfo.clear()
    }
}
