package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Check if FirebaseApp is already initialized (e.g. if google-services.json is present)
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                isInitialized = true
                Log.i(TAG, "Firebase initialized via google-services.json")
                return
            }

            // Retrieve from BuildConfig exposed by Secrets Gradle Plugin
            val apiKey = BuildConfig.FIREBASE_API_KEY
            val projectId = BuildConfig.FIREBASE_PROJECT_ID
            val appId = BuildConfig.FIREBASE_APPLICATION_ID

            if (apiKey.isBlank() || apiKey.contains("Dummy") || projectId.isBlank() || projectId.contains("dummy")) {
                Log.w(TAG, "Firebase parameters are placeholder/dummy values. Dynamic initialization skipped.")
                return
            }

            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setProjectId(projectId)
                .setApplicationId(appId)
                .setDatabaseUrl("https://$projectId.firebaseio.com")
                .build()

            FirebaseApp.initializeApp(context, options)
            isInitialized = true
            Log.i(TAG, "Firebase dynamically initialized with credentials: Project=$projectId")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
        }
    }

    val isReady: Boolean
        get() = isInitialized

    val auth: FirebaseAuth?
        get() = if (isInitialized) FirebaseAuth.getInstance() else null

    val firestore: FirebaseFirestore?
        get() = if (isInitialized) FirebaseFirestore.getInstance() else null
}
