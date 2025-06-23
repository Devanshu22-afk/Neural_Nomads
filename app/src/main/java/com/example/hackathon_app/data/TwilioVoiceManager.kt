package com.example.hackathon_app.data

import android.content.Context
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice
import com.twilio.voice.CallInvite
import com.twilio.voice.RegistrationException
import com.twilio.voice.RegistrationListener
import android.util.Log

class TwilioVoiceManager(private val context: Context) {
    private var activeCall: Call? = null

    fun makeCall(accessToken: String, to: String, onCallConnected: () -> Unit, onCallDisconnected: () -> Unit) {
        val connectOptions = ConnectOptions.Builder(accessToken)
            .params(mapOf("to" to to))
            .build()
        activeCall = Voice.connect(context, connectOptions, object : Call.Listener {
            override fun onConnected(call: Call) {
                onCallConnected()
            }
            override fun onDisconnected(call: Call, error: CallException?) {
                onCallDisconnected()
            }
            override fun onConnectFailure(call: Call, error: CallException) {
                onCallDisconnected()
            }
            override fun onRinging(call: Call) {
                // Optionally handle ringing state
            }
            override fun onReconnecting(call: Call, callException: CallException) {
                // Optionally handle reconnecting state
            }
            override fun onReconnected(call: Call) {
                // Optionally handle reconnected state
            }
        })
    }

    fun disconnectCall() {
        activeCall?.disconnect()
    }

    // --- Incoming call registration logic ---
    // Note: To receive incoming calls, you must implement FCM and a BroadcastReceiver as per Twilio docs.
    fun registerForIncomingCalls(accessToken: String) {
        val registrationListener = object : RegistrationListener {
            override fun onRegistered(accessToken: String, fcmToken: String) {
                Log.d("TwilioVoice", "Registered for incoming calls")
            }
            override fun onError(registrationException: RegistrationException, accessToken: String, fcmToken: String) {
                Log.e("TwilioVoice", "Registration error: ${registrationException.message}")
            }
        }
        // For emulator testing, use "dummy" as FCM token. For real device, use actual FCM token.
        Voice.register(accessToken, Voice.RegistrationChannel.FCM, "dummy", registrationListener)
        // To handle incoming calls, you must implement a BroadcastReceiver for com.twilio.voice.INCOMING_CALL per Twilio docs.
    }
} 