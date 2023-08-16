package com.example.simplemediasoup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.simplemediasoup.databinding.ActivityGuideNewMeetingBinding
import com.example.simplemediasoup.utils.Utils.getRandomString


class NewMeetingGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuideNewMeetingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideNewMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.headerStartMeeting.tvHeaderLeft.setOnClickListener { finish() }
        binding.headerStartMeeting.tvHeaderTitle.text = getString(R.string.start_meeting)
        binding.layoutOpenCamera.switchOption.isChecked = true
        binding.editStartDisplayName.hint = getRandomString(8)

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, RoomActivity::class.java)
            intent.putExtra("displayName", binding.editStartDisplayName.text.ifEmpty { binding.editStartDisplayName.hint })
            intent.putExtra("cameraEnable", binding.layoutOpenCamera.switchOption.isChecked)
            startActivity(intent)
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

}