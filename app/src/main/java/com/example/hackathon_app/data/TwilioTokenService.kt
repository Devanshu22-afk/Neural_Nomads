package com.example.hackathon_app.data

import java.net.HttpURLConnection
import java.net.URL

object TwilioTokenService {
    fun fetchToken(tokenUrl: String): String? {
        return try {
            val url = URL(tokenUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 