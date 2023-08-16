package com.example.simplemediasoup.model

import com.example.simplemediasoup.rtc.RoomClient

data class RoomInfo(
    var mUrl: String? = null,
    var mRoomId: String ?= null,
    var mConnectionState: RoomClient.ConnectionState = RoomClient.ConnectionState.WAITING,
    var mActiveSpeakerId: List<String> ?= null,
    var mStatsPeerId: String ?=  null
)
