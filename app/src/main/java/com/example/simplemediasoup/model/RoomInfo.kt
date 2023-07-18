package com.example.simplemediasoup.model

import com.example.simplemediasoup.rtc.RoomClient

data class RoomInfo(
    var mUrl: String? = null,
    val mRoomId: String ?= null,
    val mConnectionState: RoomClient.ConnectionState = RoomClient.ConnectionState.WAITING,
    val mActiveSpeakerId: List<String> ?= null,
    val mStatsPeerId: String ?=  null
)
