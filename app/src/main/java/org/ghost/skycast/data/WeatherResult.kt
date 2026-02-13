package org.ghost.skycast.data

/**
 * A sealed class that represents the outcome of a weather fetch operation.
 * It forces the UI to handle both Success and Error states explicitly.
 */
sealed class WeatherResult {

    /**
     * @param current The current weather data
     * @param forecast The processed list of daily forecasts
     * @param isOffline True if the data came from the local cache (no internet), False if live
     */
    data class Success(
        val current: WeatherResponse,
        val forecast: List<ForecastItem>,
        val isOffline: Boolean
    ) : WeatherResult()

    /**
     * @param message A user-friendly error message explaining what went wrong
     */
    data class Error(val message: String) : WeatherResult()
}