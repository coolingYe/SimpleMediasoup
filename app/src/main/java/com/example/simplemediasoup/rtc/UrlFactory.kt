package com.example.simplemediasoup.rtc

import java.util.*

class UrlFactory {

    companion object {
//        private const val HOSTNAME = "v3demo.mediasoup.org"
        private const val HOSTNAME = "192.168.2.168"
        private const val PORT = 4443
    }

    fun getInvitationLink(roomId: String = "happyroom", forceH264: Boolean, forceVP9: Boolean): String {
        var url = String.format(Locale.US, "https://%s/?roomId=%s", HOSTNAME, roomId)
        if (forceH264) {
            url += "&forceH246=true"
        } else if (forceVP9) url += "&forceVP9=true"
        return url
    }

    fun getProtooUrl(roomId: String = "happyroom", peerId: String, forceH264: Boolean, forceVP9: Boolean): String {
        var url = String.format(Locale.US, "wss://%s:%d/?roomId=%s&peerId=%s", HOSTNAME, PORT, roomId, peerId)
        if (forceH264) {
            url += "&forceH246=true"
        } else if (forceVP9) url += "&forceVP9=true"
        return url
    }
}