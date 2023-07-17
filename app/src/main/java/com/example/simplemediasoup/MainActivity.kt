package com.example.simplemediasoup

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity(), MainContract.View {

    private val mPresenter by lazy {
        MainPresenter(this, MainInteractor())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPresenter.createRTC()
        initListener()
    }

    private fun initListener() {
        val btnSend = findViewById<Button>(R.id.btnSend)
        btnSend.setOnClickListener {
            mPresenter.sendMessage("text")
        }
    }

    override fun getContext(): Context {
        return this@MainActivity
    }
}