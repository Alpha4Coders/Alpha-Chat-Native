package com.example.alpha_chat_native

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AlphaChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase explicitly to prevent startup crashes if auto-init fails
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            android.util.Log.e("AlphaChatApplication", "Firebase init failed", e)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
