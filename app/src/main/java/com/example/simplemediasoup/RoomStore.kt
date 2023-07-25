package com.example.simplemediasoup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simplemediasoup.model.*
import com.example.simplemediasoup.utils.DeviceInfo
import org.json.JSONArray
import org.json.JSONObject
import org.mediasoup.droid.Consumer
import org.mediasoup.droid.DataConsumer
import org.mediasoup.droid.Producer
import org.webrtc.VideoTrack

class RoomStore : ViewModel() {

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

    val consumers: MutableLiveData<Consumers> by lazy {
        MutableLiveData<Consumers>().also {
            it.value = Consumers()
        }
    }

    val producers: MutableLiveData<Producers> by lazy {
        MutableLiveData<Producers>().also {
            it.value = Producers()
        }
    }

    val self: MutableLiveData<Self> by lazy {
        MutableLiveData<Self>().also {
            it.value = Self()
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

    fun addNotify(title: String, text: String) {
        notify.postValue(Notify(title = title, text = text))
    }

    fun addNotify(text: String) {
        notify.postValue(Notify(text = text))
    }

    fun addPeer(peerId: String, peerInfo: JSONObject) {
        val value = peers.value?.apply {
            this.addPeer(peerId, peerInfo)
        }
        peers.postValue(value)
    }

    fun addProducer(producer: Producer) {
        val producerValue = producers.value?.apply {
            this.addProducer(producer)
        }
        producers.postValue(producerValue)
    }

    fun setProducerPaused(producerId: String) {
        val producerValue = producers.value?.apply {
            this.setProducerPaused(producerId)
        }
        producers.postValue(producerValue)
    }

    fun setProducerResumed(producerId: String) {
        val producerValue = producers.value?.apply {
            this.setProducerResumed(producerId)
        }
        producers.postValue(producerValue)
    }

    fun removeProducer(producerId: String) {
        val producerValue = producers.value?.apply {
            this.removeProducer(producerId)
        }
        producers.postValue(producerValue)
    }

    fun setProducerScore(producerId: String, score: JSONArray) {
        val producerValue = producers.value?.apply {
            this.setProducerScore(producerId, score)
        }
        producers.postValue(producerValue)
    }

    fun addDataConsumer(peerId: String, dataConsumer: DataConsumer) {
        val value = peers.value?.apply {
            this.addDataConsumer(peerId, dataConsumer)
        }
        peers.postValue(value)
    }

    fun setSelfInfo(peerId: String, displayName: String, deviceInfo: DeviceInfo) {
        val selfInfo = self.value?.apply {
            mId = peerId
            mDisplayName = displayName
            mDevice = deviceInfo
        }
        self.postValue(selfInfo)
    }

    fun setMediaCapabilities(microphoneEnable: Boolean, cameraEnable: Boolean) {
        val selfInfo = self.value?.apply {
            mMicrophoneEnable = microphoneEnable
            mCameraEnable = cameraEnable
        }
        self.postValue(selfInfo)
    }

    fun setAudioOnlyState(enable: Boolean) {
        val selfInfo = self.value?.apply {
            mAudioOnly = enable
        }
        self.postValue(selfInfo)
    }

    fun setAudioOnlyInProgress(enable: Boolean) {
        val selfInfo = self.value?.apply {
            mAudioOnlyInProgress = enable
        }
        self.postValue(selfInfo)
    }

    fun setAudioMutedState(enable: Boolean) {
        val selfInfo = self.value?.apply {
            mAudioMuted = enable
        }
        self.postValue(selfInfo)
    }

    fun setRestartIceInProgress(restartIceInProgress: Boolean) {
        val selfInfo = self.value?.apply {
            mRestartIceInProgress = restartIceInProgress
        }
        self.postValue(selfInfo)
    }

    fun setCamInProgress(inProgress: Boolean) {
        val selfInfo = self.value?.apply {
            mCamInProgress = inProgress
        }
        self.postValue(selfInfo)
    }

    fun removePeer(peerId: String) {
        val value = peers.value?.apply {
            this.removePeer(peerId)
        }
        peers.postValue(value)
    }

    fun removeConsumer(peerId: String, consumerId: String) {
        val consumersValue = consumers.value?.apply {
            this.removeConsumer(consumerId)
        }
        consumers.postValue(consumersValue)
        val peerValue = peers.value?.apply {
            this.removeConsumer(peerId, consumerId)
        }
        peers.postValue(peerValue)
    }

    fun getPeers(): List<Peer>? {
        return peers.value?.getAllPeer()
    }

    fun setLocalVideoTrack(videoTrack: VideoTrack) {
        localVideoTrack.postValue(videoTrack)
    }

    fun addConsumer(peerId: String, type: String, consumer: Consumer, remotelyPaused: Boolean) {
        val consumerValue = consumers.value?.apply {
            this.addConsumer(type, consumer, remotelyPaused)
        }
        consumers.postValue(consumerValue)
        val value = peers.value?.apply {
            this.addConsumer(peerId, consumer)
        }
        peers.postValue(value)
    }
}