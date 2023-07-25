package com.example.simplemediasoup.rtc

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.WorkerThread
import com.example.simplemediasoup.RoomStore
import com.example.simplemediasoup.model.ConsumerHolder
import com.example.simplemediasoup.model.DataConsumerHolder
import com.example.simplemediasoup.service.WebSocketTransport
import com.example.simplemediasoup.utils.DeviceInfo
import com.example.simplemediasoup.utils.JsonUtils.jsonPut
import com.example.simplemediasoup.utils.JsonUtils.toJsonObject
import com.example.simplemediasoup.utils.Log
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONException
import org.json.JSONObject
import org.mediasoup.droid.*
import org.protoojs.droid.Message
import org.protoojs.droid.Peer
import org.protoojs.droid.ProtooException
import org.webrtc.AudioTrack
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.VideoTrack
import java.nio.ByteBuffer

class RoomClient(
    private val mContext: Context,
    private val mStore: RoomStore,
    mRoomId: String,
    private val mPeerId: String,
    private val mDisplayName: String = "Dio",
    forceH264: Boolean,
    forceVP9: Boolean
) {

    private val TAG = "RoomClient"

    private var mWorkHandler: Handler
    private var mMainHandler: Handler
    private var mPeerConnectionUtils: PeerConnectionUtils? = null

    private var mClosed = false

    private var mProtooUrl: String? = null
    private var mProtoo: Protoo? = null
    private var mMediasoupDevice: Device = Device()

    private var mSendTransport: SendTransport? = null
    private var mRecvTransport: RecvTransport? = null

    private var mChatDataProducer: DataProducer? = null
    private var mVideoProducer: Producer? = null
    private var mAudioProducer: Producer? = null

    private var mLocalVideoTrack: VideoTrack? = null
    private var mLocalAudioTrack: AudioTrack? = null

    private var mConsumers: MutableMap<String, ConsumerHolder>? = null
    private var mDataConsumers: MutableMap<String, DataConsumerHolder>? = null

    private val mCompositeDisposable = CompositeDisposable()

    enum class ConnectionState {
        WAITING,
        CONNECTING,
        CONNECTED,
        CLOSED,
    }

    init {
        mProtooUrl = UrlFactory().getProtooUrl(mRoomId, mPeerId, forceH264, forceVP9)
        mStore.setSelfInfo(mPeerId, mDisplayName, DeviceInfo.androidDevice())
        // init worker handler.
        val handlerThread = HandlerThread("worker")
        handlerThread.start()
        mWorkHandler = Handler(handlerThread.looper)
        mMainHandler = Handler(Looper.getMainLooper())
        mWorkHandler.post { mPeerConnectionUtils = PeerConnectionUtils() }
    }

    fun join() {
        Logger.d(TAG, "join()" + this.mProtooUrl)
        mWorkHandler.post {
            val transport = WebSocketTransport(mProtooUrl!!)
            mProtoo =
                Protoo(transport, peerListener)
        }
    }

    fun enableMicrophone() {
        mWorkHandler.post {
            try {
                if (mAudioProducer != null) {
                    return@post
                }

                if (mMediasoupDevice.isLoaded.not()) {
                    return@post
                }

                if (mMediasoupDevice.canProduce("audio").not()) {
                    return@post
                }

                if (mSendTransport == null) {
                    return@post
                }

                if (mLocalAudioTrack == null) {
                    mLocalAudioTrack = mPeerConnectionUtils?.createAudioTrack(mContext)
                    mLocalAudioTrack?.setEnabled(true)
                }

                mAudioProducer = mSendTransport?.produce(
                    {
                        if (mAudioProducer != null) {
                            mStore.removeProducer(mAudioProducer?.id!!)
                            mAudioProducer = null
                        }
                    },
                    mLocalAudioTrack,
                    null, null, null
                )
                mStore.addProducer(mAudioProducer!!)

            } catch (e: MediasoupException) {
                e.printStackTrace()
                mLocalAudioTrack?.setEnabled(false)
            }
        }
    }

    fun disableMicrophone() {
        mWorkHandler.post {
            if (mAudioProducer == null) {
                return@post
            }
            mAudioProducer?.close()
            mStore.removeProducer(mAudioProducer?.id!!)
            try {
                val json = JSONObject().apply {
                    put("producerId", mAudioProducer?.id)
                }
                mProtoo?.syncRequest("closeProducer", json)
            } catch (e: ProtooException) {
                e.printStackTrace()
            }
            mAudioProducer = null
        }
    }

    fun muteMicrophone() {
        mWorkHandler.post {
            mAudioProducer?.pause()

            try {
                val json = JSONObject().apply {
                    put("producerId", mAudioProducer?.id)
                }
                mProtoo?.syncRequest("pauseProducer", json)
                mStore.setProducerPaused(mAudioProducer?.id!!)
            } catch (e: ProtooException) {
                e.printStackTrace()
            }
        }
    }

    fun unMuteMicrophone() {
        mWorkHandler.post {
            mAudioProducer?.resume()

            try {
                val json = JSONObject().apply {
                    put("resumeProducer", mAudioProducer?.id)
                }
                mProtoo?.syncRequest("resumeProducer", json)
                mStore.setProducerResumed(mAudioProducer?.id!!)
            } catch (e: ProtooException) {
                e.printStackTrace()
            }
        }
    }

    fun enableCamera() {
        mStore.setCamInProgress(true)
        mWorkHandler.post {
            try {
                if (mVideoProducer != null) {
                    return@post
                }

                if (mMediasoupDevice.isLoaded.not()) {
                    return@post
                }

                if (mMediasoupDevice.canProduce("video").not()) {
                    return@post
                }

                if (mSendTransport == null) {
                    return@post
                }

                if (mLocalVideoTrack == null) {
                    mLocalVideoTrack = mPeerConnectionUtils?.createVideoTrack(mContext)
                    mLocalVideoTrack?.setEnabled(true)
                    mLocalVideoTrack?.let { mStore.setLocalVideoTrack(it) }
                }

                mVideoProducer = mSendTransport?.produce(
                    {
                        if (mVideoProducer != null) {
                            mStore.removeProducer(mVideoProducer?.id!!)
                            mVideoProducer = null
                        }
                    },
                    mLocalVideoTrack, null, null, null
                )
                mStore.addProducer(mVideoProducer!!)
            } catch (e: MediasoupException) {
                e.printStackTrace()
                mLocalVideoTrack?.setEnabled(false)
            }
        }
        mStore.setCamInProgress(false)
    }

    fun disableCamera() {
        mWorkHandler.post {
            if (mVideoProducer == null) {
                return@post
            }
            mVideoProducer?.close()
            mStore.removeProducer(mVideoProducer?.id!!)

            try {
                val json = JSONObject().apply {
                    put("producerId", mVideoProducer?.id)
                }
                mProtoo?.syncRequest("closeProducer", json)
            } catch (e: ProtooException) {
                e.printStackTrace()
            }
            mVideoProducer = null
            mLocalVideoTrack?.let {
                it.setEnabled(false)
                it.dispose()
                mLocalVideoTrack = null
            }
        }
    }

    fun switchCamera() {
        mStore.setCamInProgress(true)
        mWorkHandler.post {
            mPeerConnectionUtils?.switchCamera(object: CameraVideoCapturer.CameraSwitchHandler {
                override fun onCameraSwitchDone(p0: Boolean) {
                    mStore.setCamInProgress(false)
                }

                override fun onCameraSwitchError(p0: String?) {
                    mStore.setCamInProgress(false)
                }
            })
        }
    }

    private val peerListener = object : Peer.Listener {
        override fun onOpen() {
            Log.d(this, "onOpen()")
            mWorkHandler.post { joinImpl() }
        }

        override fun onFail() {
            Log.d(this, "onFail()")
        }

        override fun onRequest(request: Message.Request, handler: Peer.ServerRequestHandler) {
            mWorkHandler.post {
                try {
                    when (request.method) {
                        "newConsumer" -> {
                            Log.d(this, "newConsumer")
                            onNewConsumer(request, handler)
                        }
                        "newDataConsumer" -> {
                            Log.d(this, "newDataConsumer")
                            onNewDataConsumer(request, handler)
                        }
                        else -> {
                            Logger.w(TAG, "unknown protoo request.method " + request.method)
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "handleRequestError.", e)
                }
            }
        }

        override fun onNotification(notification: Message.Notification) {
            Logger.d(TAG, "onNotification() " + notification.method + ", " + notification.data.toString())
            mWorkHandler.post {
                try {
                    handleNotification(notification)
                } catch (e: Exception) {
                    Logger.e(TAG, "handleNotifiction error.", e)
                }
            }
        }

        override fun onDisconnected() {
            mWorkHandler.post {
                disposeTransportDevice()
            }
        }

        override fun onClose() {
            if (mClosed) return
            mWorkHandler.post {
                if (mClosed) return@post
                close()
            }
        }

    }

    fun handleNotification(notification: Message.Notification) {
        val jsonData = notification.data
        when(notification.method) {
            "newPeer" -> {
                val id = jsonData.getString("id")
                val displayName = jsonData.optString("displayName")
                mStore.addPeer(id, jsonData)
                mStore.addNotify(text = displayName + "has joined room")
            }
            "peerClosed" -> {
                val peerId = jsonData.getString("peerId")
                val displayName = jsonData.optString("displayName")
                mStore.removePeer(peerId)
                mStore.addNotify(text = displayName + "has exited room")
            }
        }
    }

    private fun onNewConsumer(request: Message.Request, handler: Peer.ServerRequestHandler) {
        try {
            val data = request.data
            val peerId = data.optString("peerId")
            val producerId = data.optString("producerId")
            val id = data.optString("id")
            val kind = data.optString("kind")
            val rtpParameters = data.optString("rtpParameters")
            val type = data.optString("type")
            val appData = data.optString("appData")
            val producerPaused = data.optBoolean("producerPaused")

            val consumer = mRecvTransport?.consume({
                mConsumers?.remove(it.id)
            }, id, producerId, kind, rtpParameters, appData)
            mConsumers?.put(consumer!!.id, ConsumerHolder(peerId, consumer))
            mStore.addConsumer(peerId, type, consumer!!, producerPaused)
            handler.accept()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onNewDataConsumer(request: Message.Request, handler: Peer.ServerRequestHandler) {
        try {
            val data = request.data
            val peerId = data.optString("peerId")
            val dataProducerId = data.optString("dataProducerId")
            val id = data.optString("id")
            val sctpStreamParameters = data.optJSONObject("sctpStreamParameters")
            val streamId = sctpStreamParameters.optLong("streamId")
            val label = data.optString("label")
            val protocol = data.optString("protocol")
            val appData = data.optString("appData")

            val listener = object : DataConsumer.Listener {
                override fun OnConnecting(dataConsumer: DataConsumer?) {}

                override fun OnOpen(dataConsumer: DataConsumer?) {
                    Logger.d(TAG, "DataConsumer \"open\" event")
                }

                override fun OnClosing(dataConsumer: DataConsumer?) {

                }

                override fun OnClose(dataConsumer: DataConsumer?) {
                    mDataConsumers?.remove(dataConsumer!!.id)
                }

                override fun OnMessage(dataConsumer: DataConsumer, buffer: DataChannel.Buffer) {
                    try {
                        val sctp = JSONObject(dataConsumer.sctpStreamParameters)
                        Logger.w(
                            TAG,
                            "DataConsumer \"message\" event [streamId" + sctp.optInt("streamId") + "]"
                        )
                        val data = ByteArray(buffer.data.remaining())
                        buffer.data.get(data)
                        val message = java.lang.String(data, "UTF-8")
                        if ("chat" == dataConsumer.label) {
                            val peerList = mStore.getPeers() ?: emptyList()
                            val sendingPeer: com.example.simplemediasoup.model.Peer =
                                peerList.first { it.dataConsumers!!.contains(dataConsumer.id) }
                            mStore.addNotify(sendingPeer.displayName + " says: ", message as String)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun OnTransportClose(dataConsumer: DataConsumer?) {

                }

            }

            val dataConsumer = mRecvTransport?.consumeData(
                listener,
                id,
                dataProducerId,
                streamId,
                label,
                protocol,
                appData
            )
            mDataConsumers?.put(dataConsumer!!.id, DataConsumerHolder(peerId, dataConsumer))
            mStore.addDataConsumer(peerId, dataConsumer!!)
            handler.accept()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        if (mClosed) return
        mClosed = true
        mWorkHandler.post {

            // Close mProtoo Protoo
            if (mProtoo != null) {
                mProtoo?.close()
                mProtoo = null
            }

            disposeTransportDevice()

            mLocalAudioTrack?.let {
                it.setEnabled(false)
                it.dispose()
            }

            mLocalVideoTrack?.let {
                it.setEnabled(false)
                it.dispose()
            }

            mPeerConnectionUtils?.dispose()

            // quit worker handler thread.
            mWorkHandler.looper.quit()
        }

        // dispose request.
        mCompositeDisposable.dispose()
    }

    @WorkerThread
    private fun disposeTransportDevice() {
        Logger.d(TAG, "disposeTransportDevice()")
        // Close mediasoup Transports.
        if (mSendTransport != null) {
            mSendTransport?.close()
            mSendTransport?.dispose()
            mSendTransport = null
        }

        if (mRecvTransport != null) {
            mRecvTransport?.close()
            mRecvTransport?.dispose()
            mRecvTransport = null
        }

        // dispose device.
        mMediasoupDevice.dispose()
    }

    @WorkerThread
    private fun joinImpl() {
        Logger.d(TAG, "joinImpl()")

        try {
            val routerRtpCapabilities = mProtoo?.syncRequest("getRouterRtpCapabilities")
            mMediasoupDevice.load(routerRtpCapabilities!!, null)
            val rtpCapabilities = mMediasoupDevice.rtpCapabilities

            createSendTransport()
            createRecvTransport()

            val sctpCapabilities = mMediasoupDevice.sctpCapabilities

            val req = JSONObject().apply {
                put("displayName", mDisplayName)
                put("device", DeviceInfo.androidDevice().toJSONObject())
                put("rtpCapabilities", toJsonObject(rtpCapabilities))
                put("sctpCapabilities", sctpCapabilities)
            }

            val joinResponse = mProtoo?.syncRequest("join", req)

            val jsonMe = JSONObject().apply {
                put("id", mPeerId)
                put("displayName", mDisplayName)
                put("device", DeviceInfo.androidDevice().toJSONObject())
            }
            mStore.addPeer(mPeerId, jsonMe)

            val resp = toJsonObject(joinResponse)
            val peers = resp.optJSONArray("peers")
            run {
                var i = 0
                while (peers != null && i < peers.length()) {
                    val peer: JSONObject = peers.getJSONObject(i)
                    mStore.addPeer(peer.optString("id"), peer)
                    i++
                }
            }

            val microphoneEnable = mMediasoupDevice.canProduce("audio")
            val cameraEnable = mMediasoupDevice.canProduce("video")
            mStore.setMediaCapabilities(microphoneEnable, cameraEnable)
            mMainHandler.post(this::enableMicrophone)
            mMainHandler.post(this::enableCamera)

        } catch (e: Exception) {
            e.printStackTrace()
            mMainHandler.post { close() }
        }
    }

    private fun createRecvTransport() {
        Logger.d(TAG, "createRecvTransport()")

        val sctpCapabilities = mMediasoupDevice.sctpCapabilities
        val req = JSONObject().apply {
            put("forceTcp", false)
            put("producing", false)
            put("consuming", true)
            put("sctpCapabilities", sctpCapabilities)
        }
        val resp = mProtoo?.syncRequest("createWebRtcTransport", req)
        val info = resp?.let { JSONObject(it) }
        Logger.d(TAG, "device#createRecvTransport() $info")
        val id = info!!.optString("id")
        val iceParameters = info.optString("iceParameters")
        val iceCandidates = info.optString("iceCandidates")
        val dtlsParameters = info.optString("dtlsParameters")
        val sctpParameters = info.optString("sctpParameters")

        mRecvTransport = mMediasoupDevice.createRecvTransport(
            recvTransportListener,
            id,
            iceParameters,
            iceCandidates,
            dtlsParameters,
            sctpParameters
        )
    }

    @WorkerThread
    fun createSendTransport() {
        Logger.d(TAG, "createSendTransport()")
        val sctpCapabilities = mMediasoupDevice.sctpCapabilities
        val req = JSONObject().apply {
            put("forceTcp", false)
            put("producing", true)
            put("consuming", false)
            put("sctpCapabilities", sctpCapabilities)
        }

        val resp = mProtoo?.syncRequest("createWebRtcTransport", req)
        val info = resp?.let { JSONObject(it) }
        Logger.d(TAG, "device#createSendTransport() $info")
        val id = info!!.optString("id")
        val iceParameters = info.optString("iceParameters")
        val iceCandidates = info.optString("iceCandidates")
        val dtlsParameters = info.optString("dtlsParameters")
        val sctpParameters = info.optString("sctpParameters")

        mSendTransport = mMediasoupDevice.createSendTransport(
            sendTransportListener,
            id,
            iceParameters,
            iceCandidates,
            dtlsParameters,
            sctpParameters
        )

        mSendTransport?.let {
            mMainHandler.post {
                enableChatDataProducer()
            }
        }
    }

    private val recvTransportListener = object : RecvTransport.Listener {
        private val listenerTAG = TAG + "_RecvTrans"

        override fun onConnect(transport: Transport?, dtlsParameters: String?) {
            if (mClosed) return
            Logger.d(listenerTAG, "onConnect()")
            mProtoo?.request(
                "connectWebRtcTransport"
            ) { req ->
                jsonPut(req, "transportId", transport!!.id)
                jsonPut(req, "dtlsParameters", toJsonObject(dtlsParameters))
            }?.let {
                mCompositeDisposable.add(
                    it
                        .subscribe(
                            { d -> Logger.d(listenerTAG, "connectWebRtcTransport res: $d") }
                        ) { t ->
                            Logger.e(
                                TAG,
                                "connectWebRtcTransport for mSendTransport failed",
                                t
                            )
                        })
            }
        }

        override fun onConnectionStateChange(transport: Transport?, connectionState: String?) {
            Logger.d(listenerTAG, "onConnectionStateChange: $connectionState")
        }

    }

    private val sendTransportListener = object : SendTransport.Listener {

        private val listenerTAG = TAG + "_SendTrans"

        override fun onConnect(transport: Transport, dtlsParameters: String) {
            if (mClosed) return
            Logger.d(listenerTAG + "_send", "onConnect()")
            mProtoo?.request(
                "connectWebRtcTransport"
            ) { req ->
                jsonPut(req, "transportId", transport.id)
                jsonPut(req, "dtlsParameters", toJsonObject(dtlsParameters))
            }?.let {
                mCompositeDisposable.add(
                    it
                        .subscribe(
                            { d -> Logger.d(listenerTAG, "connectWebRtcTransport res: $d") }
                        ) { t ->
                            Logger.e(
                                TAG,
                                "connectWebRtcTransport for mSendTransport failed",
                                t
                            )
                        })
            }
        }

        override fun onConnectionStateChange(transport: Transport?, connectionState: String?) {
            Logger.d(listenerTAG, "onConnectionStateChange: $connectionState")
            if ("connected" == connectionState) {

            }
        }

        override fun onProduce(
            transport: Transport,
            kind: String,
            rtpParameters: String,
            appData: String
        ): String {
            if (mClosed) return ""
            Logger.d(listenerTAG, "onProduce()")
            val producerId = fetchProduceId { req ->
                jsonPut(req, "transportId", transport.id)
                jsonPut(req, "kind", kind)
                jsonPut(req, "rtpParameters", toJsonObject(rtpParameters))
                jsonPut(req, "appData", appData)
            }
            Logger.d(listenerTAG, "producerId: $producerId")
            return producerId
        }

        override fun onProduceData(
            transport: Transport?,
            sctpStreamParameters: String?,
            label: String?,
            protocol: String?,
            appData: String?
        ): String {
            if (mClosed) return ""
            Logger.d(listenerTAG, "onProduceData()")
            val producerDataId = fetchProduceDataId { req ->
                jsonPut(req, "transportId", transport!!.id)
                jsonPut(
                    req,
                    "sctpStreamParameters",
                    toJsonObject(sctpStreamParameters)
                )
                jsonPut(req, "label", label)
                jsonPut(req, "protocol", protocol)
                jsonPut(req, "appData", toJsonObject(appData))
            }
            Logger.d(listenerTAG, "producerDataId: $producerDataId")
            return producerDataId
        }

    }

    private fun enableChatDataProducer() {
        Logger.d(TAG, "enableChatDataProducer()")
        mWorkHandler.post {
            if (mChatDataProducer != null) {
                return@post
            }
            try {
                val listener = object : DataProducer.Listener {
                    override fun onOpen(dataProducer: DataProducer?) {
                        Logger.d(TAG, "chat DataProducer \"open\" event")
                    }

                    override fun onClose(dataProducer: DataProducer?) {
                        Logger.e(TAG, "chat DataProducer \"close\" event")
                        mChatDataProducer = null
                    }

                    override fun onBufferedAmountChange(
                        dataProducer: DataProducer?,
                        sentDataSize: Long
                    ) {
                    }

                    override fun onTransportClose(dataProducer: DataProducer?) {
                        mChatDataProducer = null
                    }

                }
                mChatDataProducer = mSendTransport?.produceData(
                    listener,
                    "chat",
                    "low",
                    false,
                    1,
                    0,
                    "{\"info\":\"my-chat-DataProducer\"}"
                )

            } catch (e: Exception) {
                Logger.e(TAG, "enableChatDataProducer() | Failed: ", e)
            }
        }
    }

    fun sendChatMessage(txt: String) {
        Logger.d(TAG, "sendChatMessage()")
        mWorkHandler.post {
            if (mChatDataProducer == null) {
                return@post
            }

            try {
                mChatDataProducer?.send(
                    DataChannel.Buffer(
                        ByteBuffer.wrap(txt.toByteArray()),
                        false
                    )
                )
            } catch (e: Exception) {
                Logger.e(TAG, "chat DataProducer.send() failed:", e)
            }
        }

    }

    private fun fetchProduceId(generator: Protoo.RequestGenerator): String {
        Logger.d(TAG, "fetchProduceId:()")
        return try {
            val response: String = mProtoo?.syncRequest("produce", generator)!!
            JSONObject(response).optString("id")
        } catch (e: ProtooException) {
            e.printStackTrace()
            ""
        } catch (e: JSONException) {
            e.printStackTrace()
            ""
        }
    }

    private fun fetchProduceDataId(generator: Protoo.RequestGenerator): String {
        Logger.d(TAG, "fetchProduceDataId:()")
        return try {
            val response: String = mProtoo?.syncRequest("produceData", generator)!!
            JSONObject(response).optString("id")
        } catch (e: ProtooException) {
            e.printStackTrace()
            ""
        } catch (e: JSONException) {
            e.printStackTrace()
            ""
        }
    }

}