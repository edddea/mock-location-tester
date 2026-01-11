package com.example.mocklocationtester.mock

import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import androidx.activity.ComponentActivity

class MockLocationController(activity: ComponentActivity) {
  private val lm: LocationManager = activity.getSystemService(LocationManager::class.java)
  private val provider = LocationManager.GPS_PROVIDER

  fun enableMockProvider() {
    try { lm.removeTestProvider(provider) } catch (_: Exception) {}

    lm.addTestProvider(
      provider,
      false, false, false, false,
      true, true, true,
      1, 1
    )
    lm.setTestProviderEnabled(provider, true)
  }

  fun push(lat: Double, lng: Double, accuracyM: Float = 5f) {
    val loc = Location(provider).apply {
      latitude = lat
      longitude = lng
      accuracy = accuracyM
      time = System.currentTimeMillis()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
      }
    }
    lm.setTestProviderLocation(provider, loc)
  }

  fun disableMockProvider() {
    try {
      lm.setTestProviderEnabled(provider, false)
      lm.removeTestProvider(provider)
    } catch (_: Exception) {}
  }
}
