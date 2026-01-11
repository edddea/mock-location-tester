package com.example.mocklocationtester

import android.app.Application
import com.google.android.libraries.places.api.Places

class App : Application() {
  override fun onCreate() {
    super.onCreate()

    // If you haven't replaced the key yet, skip initializing Places to avoid a crash.
    val key = BuildConfig.MAPS_API_KEY
    if (key.isBlank() || key.startsWith("REEMPLAZA")) return

    if (!Places.isInitialized()) {
      Places.initialize(this, key)
    }
  }
}
