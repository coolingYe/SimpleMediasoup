package com.example.simplemediasoup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.simplemediasoup.databinding.ActivityParticipantsBinding

class ParticipantsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParticipantsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParticipantsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}