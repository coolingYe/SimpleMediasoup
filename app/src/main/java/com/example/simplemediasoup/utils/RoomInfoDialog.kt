package com.example.simplemediasoup.utils

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.simplemediasoup.R
import com.example.simplemediasoup.RoomActivity
import com.example.simplemediasoup.RoomStore
import com.example.simplemediasoup.databinding.DialogRoomInfoLayoutBinding

class RoomInfoDialog(private val roomActivity: RoomActivity) :
    AlertDialog(roomActivity, R.style.Dialog) {

    private lateinit var binding: DialogRoomInfoLayoutBinding
    private lateinit var roomStore: RoomStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogRoomInfoLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomStore = ViewModelProvider(roomActivity)[RoomStore::class.java]
        roomStore.roomInfo.value?.apply {
            binding.tvMeetingHostValue.text = roomStore.self.value?.mDisplayName
            binding.tvMeetingPwdValue.text = "null"
            binding.tvMeetingCurrentUserIdValue.text = mRoomId
            binding.tvMeetingLinkValue.text = mUrl
        }
    }

    override fun onStart() {
        super.onStart()
        if (window != null) {
            val params = window!!.attributes
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            window!!.attributes = params
        }
    }
}