package com.example.simplemediasoup.model

data class Chat(
    var peerId: String ?= null,
    var name: String ?= null,
    var content: String ?= null,
    var type: Int = 0
)
