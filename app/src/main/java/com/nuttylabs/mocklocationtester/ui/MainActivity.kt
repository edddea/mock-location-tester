package com.nuttylabs.mocklocationtester.ui

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.nuttylabs.mocklocationtester.BuildConfig

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                RequestLocationPermission {

                }
                MockLocationScreen()
            }
        }
    }

    @Composable
    fun RequestLocationPermission(
        onPermissionGranted: () -> Unit
    ) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                onPermissionGranted()
            } else {
                // Manejar el caso donde el usuario niega el permiso (mostrar un aviso, etc.)
            }
        }

        // Efecto para pedir el permiso al iniciar esta pantalla
        LaunchedEffect(Unit) {
            val fineLocation =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (fineLocation != PackageManager.PERMISSION_GRANTED && coarseLocation != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                onPermissionGranted()
            }
        }
    }

    fun checkMockPermission(context: Context): Boolean {
        val opsManager = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        return try {
            // En Android Marshmallow (API 23) y superior
            opsManager.checkOp(
                AppOpsManager.OPSTR_MOCK_LOCATION,
                Process.myUid(),
                BuildConfig.APPLICATION_ID // Asegúrate de usar tu ID de paquete correcto
            ) == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun openDeveloperSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback si no se puede abrir directo
            Toast.makeText(
                context,
                "Por favor activa las opciones de desarrollador manualmente",
                Toast.LENGTH_LONG
            ).show()
        }
    }


}