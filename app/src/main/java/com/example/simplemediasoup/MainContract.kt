package com.example.simplemediasoup

import android.content.Context

class MainContract {
    interface View {
        fun getContext(): Context
    }
    interface Presenter {
        fun createRTC()
        fun getRoomClient(): RoomClient?
        fun sendMessage(message: String)
    }
    interface Interactor {

    }
}