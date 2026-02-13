package org.ghost.skycast.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ghost.skycast.data.WeatherRepository
import org.ghost.skycast.data.WeatherResponse
import org.ghost.skycast.data.ForecastItem
import org.ghost.skycast.data.WeatherResult
import org.ghost.skycast.data.WeatherStorage
import timber.log.Timber

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val current: WeatherResponse,
        val forecast: List<ForecastItem>,
        val isOffline: Boolean,
        val lastUpdated: Long = System.currentTimeMillis()
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = WeatherStorage(application)
    private val repository = WeatherRepository(storage)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)

    private var _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        Timber.d("ViewModel: Initialized")
        refreshWeather()
    }

    fun updateLocationByCity(city: String) {
        Timber.d("ViewModel: updateLocationByCity -> $city")
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            handleResult(repository.fetchWeatherByCity(city))
        }
    }

    fun updateLocationByGps(lat: Double, lon: Double) {
        Timber.d("ViewModel: updateLocationByGps -> $lat, $lon")
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            handleResult(repository.fetchWeatherByLocation(lat, lon))
        }
    }

    fun refreshWeather() {
        Timber.d("ViewModel: refreshWeather triggered")
        viewModelScope.launch {
            _isRefreshing.update { true }
            // Keep the current data visible while loading if possible (optional UI tweak)
            // But to stick to the pattern, we show Loading
            if (_uiState.value is WeatherUiState.Error) {
                _uiState.value = WeatherUiState.Loading
            }

            handleResult(repository.refreshLastLocation())

            _isRefreshing.update { false }
        }
    }

    private fun handleResult(result: WeatherResult) {
        when (result) {
            is WeatherResult.Success -> {
                Timber.d("ViewModel: Result Success (Offline: ${result.isOffline})")
                _uiState.value = WeatherUiState.Success(
                    current = result.current,
                    forecast = result.forecast,
                    isOffline = result.isOffline
                )
            }
            is WeatherResult.Error -> {
                Timber.e("ViewModel: Result Error -> ${result.message}")
                _uiState.value = WeatherUiState.Error(result.message)
            }
        }
    }
}