package com.example.simplemediasoup.model

import org.json.JSONArray
import org.mediasoup.droid.Producer
import java.util.concurrent.ConcurrentHashMap

class Producers(
    private val mProducers: MutableMap<String, ProducerWrapper> = ConcurrentHashMap()
) {

    fun addProducer(producer: Producer) {
        mProducers[producer.id] = ProducerWrapper(mProducer = producer)
    }

    fun removeProducer(producerId: String) {
        mProducers.remove(producerId)
    }

    fun setProducerPaused(producerId: String) {
        val wrapper = mProducers[producerId]
        wrapper?.mProducer?.pause()
    }

    fun setProducerResumed(producerId: String) {
        val wrapper = mProducers[producerId]
        wrapper?.mProducer?.resume()
    }

    fun setProducerScore(producerId: String, score: JSONArray) {
        val wrapper = mProducers[producerId]
        wrapper?.mScore = score
    }

    fun filter(kind:String): ProducerWrapper? {
        mProducers.forEach { (_, wrapper) ->
            if (wrapper.mProducer == null) {
                return@forEach
            }
            if (wrapper.mProducer?.track == null) {
                return@forEach
            }

            if (kind == wrapper.mProducer?.track?.kind()) {
                return wrapper
            }
        }
        return null
    }

    fun clear() {
        mProducers.clear()
    }

}

data class ProducerWrapper(
    var mType: String? = null,
    var mScore: JSONArray? = null,
    var mProducer: Producer? =null
)