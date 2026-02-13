package org.ghost.skycast.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1. Define the API endpoints
interface WeatherApi {
    // Current Weather by City Name
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    // Current Weather by Coordinates (GPS)
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    // 5-Day Forecast by City Name
    @GET("data/2.5/forecast")
    suspend fun getForecastByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse

    // 5-Day Forecast by Coordinates (GPS)
    @GET("data/2.5/forecast")
    suspend fun getForecastByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse
}

// 2. Data Models

// --- Current Weather Models ---
data class WeatherResponse(
    val id: Long,
    val name: String,
    val dt: Long, // Time of data calculation, unix, UTC
    val main: Main,
    val weather: List<WeatherDescription>,
    val wind: Wind,
    val sys: Sys,
    val coord: Coord
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int
)

data class WeatherDescription(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

data class Coord(
    val lon: Double,
    val lat: Double
)

// --- Forecast Models ---
data class ForecastResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<WeatherDescription>,
    val wind: Wind,
    val dt_txt: String // "2022-08-30 15:00:00"
)

data class City(
    val id: Long,
    val name: String,
    val coord: Coord,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

// 3. The Singleton Object
object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}