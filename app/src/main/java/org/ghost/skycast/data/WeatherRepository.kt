package org.ghost.skycast.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ghost.skycast.BuildConfig
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class WeatherRepository(private val storage: WeatherStorage) {

    // ⚠️ IMPORTANT: Ensure this key is valid.
    // If you regenerated the file, this might have reset to "xyz".
    private val apiKey = BuildConfig.WEATHER_API_KEY



    suspend fun fetchWeatherByCity(cityName: String): WeatherResult = withContext(Dispatchers.IO) {
        Timber.i("Repo: Fetching weather for city: $cityName")
        try {
            val current = RetrofitClient.api.getCurrentWeatherByCity(cityName, apiKey)
            val forecast = RetrofitClient.api.getForecastByCity(cityName, apiKey)

            // Save successful data
            storage.saveCityMode(cityName)
            storage.saveWeatherCache(current)
            storage.saveForecastCache(forecast)

            Timber.d("Repo: Network success")
            WeatherResult.Success(current, processForecast(forecast), isOffline = false)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun fetchWeatherByLocation(lat: Double, lon: Double): WeatherResult = withContext(Dispatchers.IO) {
        Timber.i("Repo: Fetching weather for GPS: $lat, $lon")
        try {
            val current = RetrofitClient.api.getCurrentWeatherByCoords(lat, lon, apiKey)
            val forecast = RetrofitClient.api.getForecastByCoords(lat, lon, apiKey)

            storage.saveGpsMode(lat, lon)
            storage.saveWeatherCache(current)
            storage.saveForecastCache(forecast)

            Timber.d("Repo: Network success")
            WeatherResult.Success(current, processForecast(forecast), isOffline = false)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun refreshLastLocation(): WeatherResult {
        val mode = storage.getLastMode()
        Timber.i("Repo: Refreshing last location (Mode: $mode)")

        return if (mode == WeatherStorage.MODE_GPS) {
            val (lat, lon) = storage.getLastLocation()
            fetchWeatherByLocation(lat, lon)
        } else {
            val city = storage.getLastCity()
            fetchWeatherByCity(city)
        }
    }

    private fun handleException(e: Exception): WeatherResult {
        Timber.w(e, "Repo: Network request failed")

        // 1. Check for Critical API Errors (401 Unauthorized)
        if (e is HttpException && e.code() == 401) {
            Timber.e("CRITICAL: API Key rejected. Check WeatherRepository.kt")
            return WeatherResult.Error("API Key Invalid. Please check your code.")
        }

        // 2. Try to load from Cache
        val cachedCurrent = storage.getWeatherCache()
        val cachedForecast = storage.getForecastCache()

        return if (cachedCurrent != null) {
            Timber.i("Repo: Returning cached data")
            // Even if forecast is missing, we can at least show current weather
            val forecastList = if (cachedForecast != null) processForecast(cachedForecast) else emptyList()

            WeatherResult.Success(
                current = cachedCurrent,
                forecast = forecastList,
                isOffline = true
            )
        } else {
            Timber.e("Repo: No cache available")
            val errorMessage = if (e is IOException) "No Internet Connection" else e.localizedMessage
            WeatherResult.Error("Offline: $errorMessage")
        }
    }

    private fun processForecast(response: ForecastResponse): List<ForecastItem> {
        return try {
            response.list.filter { it.dt_txt.contains("12:00:00") }
        } catch (e: Exception) {
            Timber.e(e, "Repo: Error processing forecast")
            emptyList()
        }
    }
}