package com.example.simplemediasoup.model

import com.example.simplemediasoup.utils.DeviceInfo

data class Self(
    var mId: String?=null,
    var mDisplayName: String?=null,
    var mDisplayNameSet: String? = null,
    var mDevice: DeviceInfo = DeviceInfo(),
    var mMicrophoneEnable:Boolean = false,
    var mCameraEnable: Boolean = false,
    var mChangeCamera: Boolean = false,

    var mCamInProgress: Boolean = false,
    var mShareInProgress: Boolean = false,

    var mAudioOnly: Boolean = false,
    var mAudioOnlyInProgress: Boolean = false,
    var mAudioMuted: Boolean = false,
    var mRestartIceInProgress: Boolean = false
) {

    fun clear() {
        mCamInProgress = false
        mAudioOnly = false
        mAudioOnlyInProgress = false
        mAudioMuted = false
        mRestartIceInProgress = false
    }
}
