package com.example.simplemediasoup

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.simplemediasoup.databinding.ActivityGuideJoinBinding
import com.example.simplemediasoup.utils.Utils.getRandomString

class JoinGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.headerJoinMeeting.tvHeaderLeft.setOnClickListener { finish() }
        binding.headerJoinMeeting.tvHeaderTitle.text = getString(R.string.join_meeting)
        binding.notAutoConnectAudio.tvOptionTitle.text = getString(R.string.do_not_auto_connect_audio)
        binding.notAutoOpenCamera.tvOptionTitle.text = getString(R.string.do_not_auto_open_camera)
        binding.notAutoConnectAudio.switchOption.isChecked = true
        binding.notAutoOpenCamera.switchOption.isChecked = true

        val displayName = getRandomString(8)
        binding.editDisplayName.hint = displayName
    }

    override fun onStart() {
        super.onStart()
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}