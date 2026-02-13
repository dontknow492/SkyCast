package org.ghost.skycast.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import timber.log.Timber

class WeatherStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("skycast_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_MODE = "mode"
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_LAT = "lat"
        private const val KEY_LON = "lon"
        private const val KEY_CACHE_CURRENT = "cache_current_weather"
        private const val KEY_CACHE_FORECAST = "cache_forecast"

        const val MODE_GPS = "gps"
        const val MODE_CITY = "city"
    }

    fun saveCityMode(cityName: String) {
        prefs.edit().apply {
            putString(KEY_MODE, MODE_CITY)
            putString(KEY_CITY_NAME, cityName)
            apply()
        }
        Timber.d("Storage: Saved mode CITY - $cityName")
    }

    fun saveGpsMode(lat: Double, lon: Double) {
        prefs.edit().apply {
            putString(KEY_MODE, MODE_GPS)
            putString(KEY_LAT, lat.toString())
            putString(KEY_LON, lon.toString())
            apply()
        }
        Timber.d("Storage: Saved mode GPS - $lat, $lon")
    }

    fun getLastMode(): String = prefs.getString(KEY_MODE, MODE_CITY) ?: MODE_CITY
    fun getLastCity(): String = prefs.getString(KEY_CITY_NAME, "London") ?: "London"

    fun getLastLocation(): Pair<Double, Double> {
        val lat = prefs.getString(KEY_LAT, "51.5074")?.toDoubleOrNull() ?: 51.5074
        val lon = prefs.getString(KEY_LON, "-0.1278")?.toDoubleOrNull() ?: -0.1278
        return Pair(lat, lon)
    }

    // --- Data Caching with Logs ---

    fun saveWeatherCache(response: WeatherResponse) {
        try {
            val json = gson.toJson(response)
            // Using commit() instead of apply() to ensure it writes immediately before app death
            prefs.edit().putString(KEY_CACHE_CURRENT, json).commit()
            Timber.d("Storage: Cached current weather for ${response.name}")
        } catch (e: Exception) {
            Timber.e(e, "Storage: Failed to save weather cache")
        }
    }

    fun getWeatherCache(): WeatherResponse? {
        val json = prefs.getString(KEY_CACHE_CURRENT, null)
        if (json == null) {
            Timber.w("Storage: No current weather cache found")
            return null
        }
        return try {
            gson.fromJson(json, WeatherResponse::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Storage: Failed to parse weather cache")
            null
        }
    }

    fun saveForecastCache(response: ForecastResponse) {
        try {
            val json = gson.toJson(response)
            prefs.edit().putString(KEY_CACHE_FORECAST, json).commit()
            Timber.d("Storage: Cached forecast")
        } catch (e: Exception) {
            Timber.e(e, "Storage: Failed to save forecast cache")
        }
    }

    fun getForecastCache(): ForecastResponse? {
        val json = prefs.getString(KEY_CACHE_FORECAST, null) ?: return null
        return try {
            gson.fromJson(json, ForecastResponse::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Storage: Failed to parse forecast cache")
            null
        }
    }
}