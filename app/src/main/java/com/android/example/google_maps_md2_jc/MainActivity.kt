package com.android.example.google_maps_md2_jc

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MapScreen(fusedLocationClient)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = false)) }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            mapProperties = mapProperties.copy(isMyLocationEnabled = true)
            getUserLocation(context, fusedLocationClient) { location ->
                userLocation = location
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Map") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = mapProperties,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Your Location"
                    )

                    // Add hardcoded nearby places
                    val places = listOf(
                        LatLng(it.latitude + 0.002, it.longitude + 0.002),
                        LatLng(it.latitude - 0.002, it.longitude - 0.002)
                    )
                    places.forEach { place ->
                        Marker(
                            state = MarkerState(position = place),
                            title = "Point of Interest",
                            snippet = "Description of the place"
                        )
                    }
                }
            }

            if (!permissionState.allPermissionsGranted) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Location permission is required for this app to function.", Modifier.padding(16.dp))
                    Button(
                        onClick = { permissionState.launchMultiplePermissionRequest() },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Grant Location Permission")
                    }
                }
            }
        }
    }
}

private fun getUserLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                location?.let {
                    onLocationReceived(LatLng(it.latitude, it.longitude))
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to get location.", Toast.LENGTH_SHORT).show()
            }
    }
}
