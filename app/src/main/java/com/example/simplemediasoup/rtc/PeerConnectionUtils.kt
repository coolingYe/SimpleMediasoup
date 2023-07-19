package com.example.simplemediasoup.rtc

import android.content.Context
import androidx.annotation.MainThread
import org.mediasoup.droid.Logger
import org.webrtc.*

class PeerConnectionUtils {

    private var mThreadChecker: ThreadUtils.ThreadChecker = ThreadUtils.ThreadChecker()
    private var mPeerConnectionFactory: PeerConnectionFactory? = null
    private var mAudioSource: AudioSource? = null
    private var mVideoSource: VideoSource? = null
    private var mVideoCapturer: VideoCapturer? = null

    companion object {
        private const val TAG = "PeerConnectionUtils"
        private const val VIDEO_TRACK_ID = "Video1"
        private const val AUDIO_TRACK_ID = "Audio1"

        private const val Width = 640
        private const val Height = 480
        private const val FPS = 30

        private val eglBase: EglBase = EglBase.create()

        fun getEglContext(): EglBase.Context {
            return this.eglBase.eglBaseContext
        }
    }

    private fun createPeerConnectionFactory(context: Context) {
        Logger.d(TAG, "createPeerConnectionFactory()")
        mThreadChecker.checkIsOnValidThread()

        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        mPeerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoDecoderFactory(decoderFactory)
            .setVideoEncoderFactory(encoderFactory)
            .createPeerConnectionFactory()
    }

    fun createVideoTrack(context: Context, id: String = VIDEO_TRACK_ID): VideoTrack? {
        Logger.d(TAG, "createVideoTrack()")
        mThreadChecker.checkIsOnValidThread()
        if (mVideoSource == null) {
            createVideoSource(context)
        }

        return mPeerConnectionFactory?.createVideoTrack(id, mVideoSource)
    }

    @MainThread
    private fun createVideoSource(context: Context) {
        Logger.d(TAG, "createVideoSource")
        mThreadChecker.checkIsOnValidThread()
        if (mPeerConnectionFactory == null) {
            createPeerConnectionFactory(context)
        }

        if (mVideoCapturer == null) {
            mVideoCapturer = createVideoCapture(context)
        }

        mVideoSource = mPeerConnectionFactory?.createVideoSource(false)
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        mVideoCapturer?.initialize(surfaceTextureHelper, context, mVideoSource?.capturerObserver)
        mVideoCapturer?.startCapture(Width, Height, FPS)
    }

    private fun createVideoCapture(context: Context): VideoCapturer? {
        return if (Camera2Enumerator.isSupported(context)) {
            createCameraCapture(Camera2Enumerator(context))
        } else {
            createCameraCapture(Camera1Enumerator(true))
        }
    }

    private fun createCameraCapture(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapture: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapture != null) {
                    return videoCapture
                }
            }
        }

        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapture: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapture != null) {
                    return videoCapture
                }
            }
        }
        return null
    }

    fun createAudioTrack(context: Context, id: String = AUDIO_TRACK_ID): AudioTrack? {
        Logger.d(TAG, "createAudioTrack()")
        mThreadChecker.checkIsOnValidThread()
        if (mAudioSource == null) {
            createAudioSource(context)
        }

        return mPeerConnectionFactory?.createAudioTrack(id, mAudioSource)
    }

    private fun createAudioSource(context: Context) {
        Logger.d(TAG, "createAudioSource()")
        mThreadChecker.checkIsOnValidThread()
        if (mPeerConnectionFactory == null)  {
            createPeerConnectionFactory(context)
        }

        mAudioSource = mPeerConnectionFactory?.createAudioSource(MediaConstraints())
    }

    fun dispose() {
        mThreadChecker.checkIsOnValidThread()
        mAudioSource?.let {
            it.dispose()
            mAudioSource = null
        }

        mVideoCapturer?.let {
            it.dispose()
            mVideoCapturer = null
        }

        mVideoSource?.let {
            it.dispose()
            mVideoSource = null
        }

        mPeerConnectionFactory?.let {
            it.dispose()
            mPeerConnectionFactory = null
        }

        eglBase.release()
    }
}