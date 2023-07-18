package com.example.simplemediasoup.model

import com.example.simplemediasoup.utils.DeviceInfo
import org.json.JSONObject

data class Peer(
    var id: String? = null,
    var displayName: String? = null,
    var device: DeviceInfo? = null,
    var consumers: Set<String>? = null,
    var dataConsumers: Set<String>? = null
) {
    fun get(info: JSONObject): Peer {
        id = info.optString("id")
        displayName = info.optString("displayName")
        val deviceInfo = info.optJSONObject("device")
        deviceInfo?.let {
            device = DeviceInfo()
                .setFlag(deviceInfo.optString("flag"))
                .setName(deviceInfo.optString("name"))
                .setVersion(deviceInfo.optString("version"))
        } ?: {
            device = DeviceInfo.unknownDevice()
        }
        consumers = HashSet()
        dataConsumers = HashSet()
        return Peer(id, displayName, device, consumers, dataConsumers)
    }
}
