package com.example.simplemediasoup.rtc

import android.content.Context
import android.text.TextUtils
import androidx.annotation.MainThread
import org.mediasoup.droid.Logger
import org.webrtc.*
import org.webrtc.CameraVideoCapturer.CameraEventsHandler
import org.webrtc.CameraVideoCapturer.CameraSwitchHandler

class PeerConnectionUtils {

    private var mThreadChecker: ThreadUtils.ThreadChecker = ThreadUtils.ThreadChecker()
    private var mPeerConnectionFactory: PeerConnectionFactory? = null
    private var mAudioSource: AudioSource? = null
    private var mVideoSource: VideoSource? = null
    private var mVideoCapturer: CameraVideoCapturer? = null
    private var mIsFrontCamera: Boolean = true

    companion object {
        private const val TAG = "PeerConnectionUtils"
        private const val VIDEO_TRACK_ID = "Video1"
        private const val AUDIO_TRACK_ID = "Audio1"

        private const val VIDEO_WIDTH = 640
        private const val VIDEO_HEIGHT = 480
        private const val VIDEO_FPS = 30

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
            createVideoCapture(context)
        }

        mVideoSource = mPeerConnectionFactory?.createVideoSource(false)
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        mVideoCapturer?.initialize(surfaceTextureHelper, context, mVideoSource?.capturerObserver)
        mVideoCapturer?.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FPS)
    }

    fun setIsFrontCamera(isFrontCamera: Boolean) {
        this.mIsFrontCamera = isFrontCamera
    }

    private fun createVideoCapture(context: Context) {
        Logger.d(TAG, "createCamCapture()")
        mThreadChecker.checkIsOnValidThread()
        val isCamera2Supported = Camera2Enumerator.isSupported(context)
        val cameraEnumerator: CameraEnumerator = if (isCamera2Supported) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator()
        }
        val deviceNames = cameraEnumerator.deviceNames
        for (deviceName in deviceNames) {
            val needFrontFacing: Boolean = mIsFrontCamera
            var selectedDeviceName: String? = null
            if (false) {
                if (cameraEnumerator.isFrontFacing(deviceName)) {
                    selectedDeviceName = deviceName
                }
            } else {
                if (!cameraEnumerator.isFrontFacing(deviceName)) {
                    selectedDeviceName = deviceName
                }
            }
            if (!TextUtils.isEmpty(selectedDeviceName)) {
                mVideoCapturer = cameraEnumerator.createCapturer(
                    selectedDeviceName,
                    object : CameraEventsHandler {
                        override fun onCameraError(s: String) {
                            Logger.e(TAG, "onCameraError, $s")
                        }

                        override fun onCameraDisconnected() {
                            Logger.w(TAG, "onCameraDisconnected")
                        }

                        override fun onCameraFreezed(s: String) {
                            Logger.w(TAG, "onCameraFreezed, $s")
                        }

                        override fun onCameraOpening(s: String) {
                            Logger.d(TAG, "onCameraOpening, $s")
                        }

                        override fun onFirstFrameAvailable() {
                            Logger.d(TAG, "onFirstFrameAvailable")
                        }

                        override fun onCameraClosed() {
                            Logger.d(TAG, "onCameraClosed")
                        }
                    })
                break
            }
        }
        checkNotNull(mVideoCapturer) { "Failed to create Camera Capture" }
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

    fun switchCamera(switchHandler: CameraSwitchHandler) {
        mThreadChecker.checkIsOnValidThread()
        mVideoCapturer?.switchCamera(switchHandler)
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
    }
}