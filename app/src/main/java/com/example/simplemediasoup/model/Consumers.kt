package com.example.simplemediasoup.model

import org.json.JSONArray
import org.mediasoup.droid.Consumer
import java.util.concurrent.ConcurrentHashMap

class Consumers(
    private val consumers: MutableMap<String, ConsumerWrapper> = ConcurrentHashMap()
) {
    fun addConsumer(type: String, consumer: Consumer, remotelyPaused: Boolean) {
        consumers[consumer.id] = ConsumerWrapper(mType = type, mRemotelyPaused = remotelyPaused, mConsumer = consumer)
    }

    fun removeConsumer(consumerId: String) {
        consumers.remove(consumerId)
    }

    fun setConsumerPaused(consumerId: String, originator: String) {
        val wrapper = consumers[consumerId]
        wrapper?.let {
            if ("local" == originator) {
                it.mLocallyPaused = true
            } else it.mRemotelyPaused = true
        }
    }

    fun setConsumerResumed(consumerId: String, originator: String) {
        val wrapper = consumers[consumerId]
        wrapper?.let {
            if ("local" == originator) {
                it.mLocallyPaused = false
            } else it.mRemotelyPaused = false
        }
    }

    fun setConsumerCurrentLayers(consumerId: String, spatialLayer: Int, temporalLayer: Int) {
        val wrapper = consumers[consumerId]
        wrapper?.let {
            it.mSpatialLayer = spatialLayer
            it.mTemporalLayer = temporalLayer
        }
    }

    fun setConsumerScore(consumerId: String, score: JSONArray) {
        val wrapper = consumers[consumerId]
        wrapper?.let {
            it.mScore = score
        }
    }

    fun getConsumer(consumerId: String): ConsumerWrapper? {
        return consumers[consumerId]
    }

    fun clear() {
        consumers.clear()
    }
}

data class ConsumerWrapper(
    var mType: String? = null,
    var mLocallyPaused: Boolean = false,
    var mRemotelyPaused: Boolean = false,
    var mSpatialLayer: Int = 0,
    var mTemporalLayer: Int = 0,
    var mConsumer: Consumer? = null,
    var mScore: JSONArray? = null,
    var mPreferredSpatialLayer: Int = 0,
    var mPreferredTemporalLayer: Int = 0
)
