package com.example.simplemediasoup.model

import org.mediasoup.droid.Consumer
import org.mediasoup.droid.DataConsumer

data class ConsumerHolder(
    val peerId: String? = null,
    val consumer: Consumer? = null
)

data class DataConsumerHolder(
    val peerId: String? = null,
    val dataConsumer: DataConsumer? = null
)
