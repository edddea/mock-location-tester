plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.mocklocationtester"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mocklocationtester"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // TODO: Replace with your Google API key (Maps SDK + Places)
        buildConfigField("String", "MAPS_API_KEY", "\"AIzaSyBLui4Dk-WjU-9S_TWsMOqtFaw7JOydtfI\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    kotlin {
        jvmToolchain(17)
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:6.4.1")

    // Places (Autocomplete overlay)
    implementation("com.google.android.libraries.places:places:3.5.0")
}
