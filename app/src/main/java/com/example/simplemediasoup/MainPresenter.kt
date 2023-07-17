package com.example.simplemediasoup

import com.example.simplemediasoup.MainContract.Interactor

class MainPresenter(
    private val mView: MainContract.View,
    mInteractor: Interactor
) : MainContract.Presenter {

    private var roomClient: RoomClient? = null

    override fun createRTC() {
        val roomClient = RoomClient(mView.getContext(), "happyroom", "be5ln5de", forceH264 = false, forceVP9 = false)
        this.roomClient = roomClient
        roomClient.join()
    }

    override fun getRoomClient(): RoomClient? = roomClient

    override fun sendMessage(message: String) {
        roomClient?.sendChatMessage(message)
    }

}