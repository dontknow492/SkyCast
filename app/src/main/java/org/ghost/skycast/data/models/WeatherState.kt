package org.ghost.skycast.data.models

import org.ghost.skycast.enum.TimeOfDay
import org.ghost.skycast.enum.WeatherType

data class WeatherState(val timeOfDay: TimeOfDay, val weatherType: WeatherType)