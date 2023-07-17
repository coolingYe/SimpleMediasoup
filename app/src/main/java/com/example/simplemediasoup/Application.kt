package com.example.simplemediasoup

import android.app.Application
import org.mediasoup.droid.Logger
import org.mediasoup.droid.MediasoupClient

class Application: Application() {

    override fun onCreate() {
        super.onCreate()

        Logger.setLogLevel(Logger.LogLevel.LOG_DEBUG)
        Logger.setDefaultHandler()

        MediasoupClient.initialize(applicationContext)
    }
}