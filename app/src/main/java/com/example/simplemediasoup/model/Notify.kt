package com.example.simplemediasoup.model

data class Notify(
    var id: String? = null,
    val type: String? = null,
    val title: String? = null,
    val text: String? = null,
) {
    val timeout: Int = when (type) {
        "info" -> 3000
        "error" -> 5000
        else -> 0
    }
}
