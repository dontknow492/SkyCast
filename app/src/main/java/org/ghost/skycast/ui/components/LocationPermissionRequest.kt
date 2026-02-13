package org.ghost.skycast.ui.components

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * A Composable wrapper that handles the permission logic.
 *
 * @param onPermissionGranted Logic to execute when we have permission (e.g., fetch weather).
 * @param onPermissionDenied Logic to execute if user refuses (e.g., show error or default to London).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionRequest(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable () -> Unit
) {
    // 1. Setup the permission state (asking for both Fine and Coarse location)
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 2. Check state and react
    if (locationPermissionsState.allPermissionsGranted) {
        // If we already have permission, just run the content (or trigger the load)
        // We use LaunchedEffect to ensure the callback fires once
        LaunchedEffect(Unit) {
            onPermissionGranted()
        }
        content()
    } else {
        // 3. If not granted, show a rationale or the request button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "We need your location to show local weather.")
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Grant Permission")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional: Let them skip using GPS
                androidx.compose.material3.TextButton(onClick = { onPermissionDenied() }) {
                    Text("Enter City Manually")
                }
            }
        }
    }
}