package com.nuttylabs.mocklocationtester.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.nuttylabs.mocklocationtester.BuildConfig
import com.nuttylabs.mocklocationtester.ui.MockLocationController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockLocationScreen() {
  val ctx = LocalContext.current
  val activity = ctx as ComponentActivity
  val mocker = remember { MockLocationController(activity) }
  val scope = rememberCoroutineScope()

  var selected by remember { mutableStateOf(LatLng(19.432608, -99.133209)) } // CDMX
  var latText by remember { mutableStateOf(selected.latitude.toString()) }
  var lngText by remember { mutableStateOf(selected.longitude.toString()) }
  var status by remember { mutableStateOf("Tip: Activa 'Ubicación ficticia' y elige esta app en Dev Options.") }

  fun syncInputsFromSelected() {
    latText = selected.latitude.toString()
    lngText = selected.longitude.toString()
  }

  val cameraState = rememberCameraPositionState()

  LaunchedEffect(Unit) {
    cameraState.move(CameraUpdateFactory.newLatLngZoom(selected, 15f))
  }

  val fields = remember {
    listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
  }

  val autocompleteLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    val data: Intent? = result.data
    when {
      result.resultCode == Activity.RESULT_OK && data != null -> {
        val place = Autocomplete.getPlaceFromIntent(data)
        val ll = place.latLng
        if (ll != null) {
          selected = LatLng(ll.latitude, ll.longitude)
          syncInputsFromSelected()
          scope.launch { cameraState.animate(CameraUpdateFactory.newLatLngZoom(selected, 16f)) }
          status = "Lugar: ${place.name ?: place.address ?: "OK"}"
        } else {
          status = "No se pudo obtener lat/lng del lugar."
        }
      }
      result.resultCode == Activity.RESULT_CANCELED -> {}
      data != null -> {
        val st = Autocomplete.getStatusFromIntent(data)
        status = st.statusMessage ?: "Error en Autocomplete"
      }
    }
  }

  Scaffold(
    topBar = { TopAppBar(title = { Text("Mock Location Tester") }) }
  ) { pad ->
    Column(
      modifier = Modifier
        .padding(pad)
        .fillMaxSize()
    ) {

      if (BuildConfig.MAPS_API_KEY.startsWith("REEMPLAZA")) {
        AssistChip(
          onClick = {},
          label = { Text("⚠️ Reemplaza tu API key en BuildConfig y en AndroidManifest.xml") },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          modifier = Modifier.weight(1f),
          onClick = {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
              .build(ctx)
            autocompleteLauncher.launch(intent)
          }
        ) { Text("Buscar dirección") }

        OutlinedButton(
          modifier = Modifier.weight(1f),
          onClick = { scope.launch { cameraState.animate(CameraUpdateFactory.newLatLngZoom(selected, 16f)) } }
        ) { Text("Centrar") }
      }

      Box(Modifier.weight(1f).fillMaxWidth()) {
        GoogleMap(
          modifier = Modifier.fillMaxSize(),
          cameraPositionState = cameraState,
          onMapClick = { picked ->
            selected = picked
            syncInputsFromSelected()
            status = "Seleccionado en mapa"
          }
        ) {
          Marker(
            state = MarkerState(position = selected),
            title = "Mock location"
          )
        }
      }

      Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = latText,
            onValueChange = { latText = it },
            label = { Text("Lat") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
          )
          OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = lngText,
            onValueChange = { lngText = it },
            label = { Text("Lng") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
          )
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            modifier = Modifier.weight(1f),
            onClick = {
              val lat = latText.toDoubleOrNull()
              val lng = lngText.toDoubleOrNull()
              if (lat == null || lng == null) {
                status = "Lat/Lng inválidos"
                return@Button
              }
              selected = LatLng(lat, lng)
              scope.launch { cameraState.animate(CameraUpdateFactory.newLatLngZoom(selected, 16f)) }
              status = "Set desde inputs"
            }
          ) { Text("Set en mapa") }

          Button(
            modifier = Modifier.weight(1f),
            onClick = {
              try {
                mocker.enableMockProvider()
                status = "Mock provider ON"
              } catch (se: SecurityException) {
                status = "Falta seleccionar esta app como 'Ubicación ficticia' en Dev Options"
              } catch (e: Exception) {
                status = "Error: ${e.message ?: e::class.java.simpleName}"
              }
            }
          ) { Text("Start mock") }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            modifier = Modifier.weight(1f),
            onClick = {
              try {
                mocker.push(selected.latitude, selected.longitude)
                status = "Enviado: ${selected.latitude}, ${selected.longitude}"
              } catch (se: SecurityException) {
                status = "Sin permiso de ubicación ficticia (Dev Options)"
              } catch (e: Exception) {
                status = "Error: ${e.message ?: e::class.java.simpleName}"
              }
            }
          ) { Text("Send location") }

          OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = {
              mocker.disableMockProvider()
              status = "Mock provider OFF"
            }
          ) { Text("Stop") }
        }

        Spacer(Modifier.height(8.dp))
        Text(status, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}
