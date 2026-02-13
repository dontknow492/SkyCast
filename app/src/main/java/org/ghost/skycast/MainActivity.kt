package org.ghost.skycast

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import org.ghost.skycast.location.DefaultLocationClient
import org.ghost.skycast.ui.WeatherViewModel
import org.ghost.skycast.ui.components.LocationPermissionRequest
import org.ghost.skycast.ui.screens.WeatherScreen
import org.ghost.skycast.ui.theme.SkyCastTheme

class WeatherViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class MainActivity : ComponentActivity() {
    private lateinit var weatherViewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel with Application context
        val viewModelFactory = WeatherViewModelFactory(application)
        weatherViewModel = ViewModelProvider(this, viewModelFactory)[WeatherViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            SkyCastTheme {

                val context = LocalContext.current
                val locationClient = remember {
                    DefaultLocationClient(context)
                }
                val coroutineScope = rememberCoroutineScope()

                LocationPermissionRequest(
                    onPermissionGranted = {
                        // Permission granted! Now fetch the actual GPS coordinates
                        coroutineScope.launch {
                            try {
                                val location = locationClient.getCurrentLocation()
                                if (location != null) {
                                    weatherViewModel.updateLocationByGps(
                                        location.latitude,
                                        location.longitude
                                    )
                                }
                            } catch (e: Exception) {
                                // GPS off? Fallback to city
                                weatherViewModel.updateLocationByCity("London")
                            }
                        }
                    },
                    onPermissionDenied = {
                        // User said no, load default
                        weatherViewModel.updateLocationByCity("London")
                    }
                ) {
                    WeatherScreen(
                        viewModel = weatherViewModel,
                    )
                }

            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SkyCastTheme {
        Greeting("Android")
    }
}