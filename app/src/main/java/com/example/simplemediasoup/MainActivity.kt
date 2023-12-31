package com.example.simplemediasoup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.simplemediasoup.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBarNewMeeting.setOnClickListener {
            startActivity(Intent(this, NewMeetingGuideActivity::class.java))
        }

        binding.ivBarJoin.setOnClickListener {
            startActivity(Intent(this, JoinGuideActivity::class.java))
        }
    }

}