package com.example.alpha_chat_native.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.alpha_chat_native.MainActivity
import com.example.alpha_chat_native.R
import com.example.alpha_chat_native.data.remote.AlphaChatApi
import com.example.alpha_chat_native.data.remote.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AlphaFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var api: AlphaChatApi

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    /**
     * Called when a new token for the default Firebase project is generated.
     */
    override fun onNewToken(token: String) {
        Timber.d("New FCM token generated: $token")
        sendRegistrationToServer(token)
    }

    /**
     * Called when message is received.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("From: \${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload: \${remoteMessage.data}")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Timber.d("Message Notification Body: \${it.body}")
            sendNotification(it.title ?: "AlphaChat", it.body ?: "New Message")
        }
    }

    private fun sendRegistrationToServer(token: String) {
        serviceScope.launch {
            try {
                // Since this might be called on app install/startup before user logs in,
                // the API call might fail if there's no auth token. The MainActivity
                // should also register the token when login succeeds.
                api.registerFcmToken(FcmTokenRequest(token))
                Timber.d("FCM token successfully registered to backend.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to register FCM token to backend.")
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "alphachat_messages_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo) // Assuming logo exists
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for new chat messages"
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
