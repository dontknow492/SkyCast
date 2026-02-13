package org.ghost.skycast.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.ghost.skycast.R
import org.ghost.skycast.data.Coord
import org.ghost.skycast.data.ForecastItem
import org.ghost.skycast.data.Main
import org.ghost.skycast.data.Sys
import org.ghost.skycast.data.WeatherDescription
import org.ghost.skycast.data.WeatherResponse
import org.ghost.skycast.data.Wind
import org.ghost.skycast.ui.WeatherUiState
import org.ghost.skycast.ui.theme.SkyCastTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessView(
    state: WeatherUiState.Success,
    modifier: Modifier = Modifier,
) {
    // Determine gradient based on Day/Night (simple logic based on icon suffix 'd' or 'n')
    val isDay = state.current.weather.firstOrNull()?.icon?.endsWith("d") == true
    val gradientColors = if (isDay) {
        listOf(Color(0xFF4FC3F7), Color(0xFF29B6F6)) // Sky Blue
    } else {
        listOf(Color(0xFF1A237E), Color(0xFF311B92)) // Deep Blue/Purple
    }
    val sheetSize = if (state.isOffline) 100.dp else 220.dp

    BottomSheetScaffold(
        modifier = modifier,
        sheetPeekHeight = sheetSize,
        sheetContent = {
            ForecastSheet(Modifier, state.forecast)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
//        verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 1. Main Header Card
            item {
                MainWeatherCard(state.current, gradientColors)
            }

            // 2. Offline Banner (if needed)
            if (state.isOffline) {
                item {
                    OfflineBanner(state.lastUpdated)
                }
            }

            // 3. Grid Details
            item {
                DetailsGrid(state.current)
            }

            // 4. Forecast Section Header
        }
    }


}


@Composable
fun ForecastSheet(modifier: Modifier = Modifier, forecastItems: List<ForecastItem>) {
    LazyColumn {
        stickyHeader {
            Text(
                text = "5-Day Forecast",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
        }
        items(forecastItems) { forecastItem ->
            ForecastUiItem(item = forecastItem)
        }
    }
}

@Composable
fun MainWeatherCard(weather: WeatherResponse, gradientColors: List<Color>) {
    val iconCode = weather.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@4x.png" // Using @4x for higher res

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(350.dp), // Fixed height for a grand look
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Date and Location
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(iconUrl)
                            .crossfade(true)
                            .build(),
                        error = painterResource(R.drawable.full_sun_v2),
                        contentDescription = "Current Weather",
                        modifier = Modifier.align(Alignment.TopEnd).fillMaxSize(0.7f),
                        contentScale = ContentScale.Fit
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = weather.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = formatUnixDate(weather.dt),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "${weather.main.temp.roundToInt()}°",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 90.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                                ?: "",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }

                }


                // Bottom: High/Low and Feels Like
                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerticalInfoItem(
                        label = "Min",
                        value = "${weather.main.temp_min.roundToInt()}°",
                        color = Color.White
                    )
                    VerticalInfoItem(
                        label = "Max",
                        value = "${weather.main.temp_max.roundToInt()}°",
                        color = Color.White
                    )
                    VerticalInfoItem(
                        label = "Feels Like",
                        value = "${weather.main.feels_like.roundToInt()}°",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsGrid(weather: WeatherResponse) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherDetailCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.water_drop),
                title = "Humidity",
                value = "${weather.main.humidity}%",
                color = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            WeatherDetailCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.rounded_air_24),
                title = "Wind",
                value = "${weather.wind.speed} m/s",
                subValue = "Dir: ${weather.wind.deg}°", // Could use an arrow icon here
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherDetailCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.speed_alt),
                title = "Pressure",
                value = "${weather.main.pressure} hPa",
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            WeatherDetailCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.full_sun),
                title = "Sun Cycle",
                value = formatTime(weather.sys.sunrise),
                subValue = "Set: ${formatTime(weather.sys.sunset)}",
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun WeatherDetailCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    value: String,
    subValue: String? = null,
    color: Color
) {
    Card(
        modifier = modifier.height(106.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalArrangement = if (subValue != null)
                Arrangement.Top
            else
                Arrangement.Center
        ) {
            Row {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                Icon(
                    painter = icon,
                    contentDescription = title,
                    modifier = Modifier.size(34.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            subValue?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun OfflineBanner(lastUpdated: Long) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.cloud_off),
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Offline Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Last updated: ${formatTime(lastUpdated / 1000)}", // Convert millis to seconds for helper
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun VerticalInfoItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}


@Composable
fun ForecastUiItem(
    modifier: Modifier = Modifier,
    item: ForecastItem
) {
    // Parsing date for display (e.g., "Mon 12 PM")
    val formattedDate = remember(item.dt_txt) {
        try {
            val inputFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = LocalDateTime.parse(item.dt_txt, inputFormatter)
            val outputFormatter = DateTimeFormatter.ofPattern("EEE, h a", Locale.getDefault())
            date.format(outputFormatter)
        } catch (e: Exception) {
            item.dt_txt
        }
    }

    val iconCode = item.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Date and Condition
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                        ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            // Center: Weather Icon (Placeholder for now, use AsyncImage in production)
            // Ideally, map item.weather[0].icon to a resource or URL
            Surface(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(iconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.FillBounds,
                        // Fallback to a generic Cloud icon if the internet is down or URL fails
                        error = painterResource(R.drawable.cloud_xmark),
                        // Show the same cloud icon while loading
                        placeholder = painterResource(R.drawable.dark_cloud)
                    )
                }
            }

            // Right: Temp and Details
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${item.main.temp.roundToInt()}°",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mini Details Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiniDetail(
                        icon = painterResource(R.drawable.water_drop),
                        text = "${item.main.humidity}%"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MiniDetail(
                        icon = painterResource(R.drawable.rounded_air_24),
                        text = "${item.wind.speed.roundToInt()} m/s"
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniDetail(icon: Painter, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun ForecastItemPreview() {
    val data = ForecastItem(
        dt = 1771070400,
        main = Main(
            temp = 9.66,
            humidity = 87,
            feels_like = 9.66,
            temp_min = 9.66,
            temp_max = 9.66,
            pressure = 1017
        ),
        weather = listOf(
            WeatherDescription(
                id = 804,
                main = "Clouds",
                description = "overcast clouds",
                icon = "04n"
            ),
        ),
        wind = Wind(
            speed = 0.52,
            deg = 178
        ),
        dt_txt = "2026-02-14 12:00:00"
    )
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            ForecastUiItem(modifier = Modifier, item = data)
        }
    }
}

@Preview
@Composable
private fun SuccessViewPreview() {
    val sampleWeatherState = WeatherUiState.Success(
        current = WeatherResponse(
            id = 5375480,
            name = "Mountain View",
            dt = 1770988321,
            main = Main(
                temp = 8.49,
                humidity = 91,
                feels_like = 7.38,
                temp_min = 7.1,
                temp_max = 11.24,
                pressure = 1020
            ),
            weather = listOf(
                WeatherDescription(
                    id = 804,
                    main = "Clouds",
                    description = "overcast clouds",
                    icon = "04n"
                )
            ),
            wind = Wind(
                speed = 2.06,
                deg = 180
            ),
            sys = Sys(
                country = "US",
                sunrise = 1770994792,
                sunset = 1771033526
            ),
            coord = Coord(
                lon = -122.084,
                lat = 37.422
            )
        ),
        forecast = listOf(
            ForecastItem(
                dt = 1771070400,
                main = Main(
                    temp = 9.66,
                    humidity = 87,
                    feels_like = 9.66,
                    temp_min = 9.66,
                    temp_max = 9.66,
                    pressure = 1017
                ),
                weather = listOf(
                    WeatherDescription(
                        id = 804,
                        main = "Clouds",
                        description = "overcast clouds",
                        icon = "04n"
                    )
                ),
                wind = Wind(
                    speed = 0.52,
                    deg = 178
                ),
                dt_txt = "2026-02-14 12:00:00"
            ),
            ForecastItem(
                dt = 1771156800,
                main = Main(
                    temp = 11.73,
                    humidity = 80,
                    feels_like = 11.05,
                    temp_min = 11.73,
                    temp_max = 11.73,
                    pressure = 1013
                ),
                weather = listOf(
                    WeatherDescription(
                        id = 804,
                        main = "Clouds",
                        description = "overcast clouds",
                        icon = "04n"
                    )
                ),
                wind = Wind(
                    speed = 2.57,
                    deg = 155
                ),
                dt_txt = "2026-02-15 12:00:00"
            ),
            ForecastItem(
                dt = 1771243200,
                main = Main(
                    temp = 10.78,
                    humidity = 98,
                    feels_like = 10.47,
                    temp_min = 10.78,
                    temp_max = 10.78,
                    pressure = 1002
                ),
                weather = listOf(
                    WeatherDescription(
                        id = 502,
                        main = "Rain",
                        description = "heavy intensity rain",
                        icon = "10n"
                    )
                ),
                wind = Wind(
                    speed = 0.35,
                    deg = 18
                ),
                dt_txt = "2026-02-16 12:00:00"
            ),
            ForecastItem(
                dt = 1771329600,
                main = Main(
                    temp = 8.39,
                    humidity = 89,
                    feels_like = 4.92,
                    temp_min = 8.39,
                    temp_max = 8.39,
                    pressure = 1006
                ),
                weather = listOf(
                    WeatherDescription(
                        id = 500,
                        main = "Rain",
                        description = "light rain",
                        icon = "10n"
                    )
                ),
                wind = Wind(
                    speed = 6.84,
                    deg = 238
                ),
                dt_txt = "2026-02-17 12:00:00"
            ),
            ForecastItem(
                dt = 1771416000,
                main = Main(
                    temp = 7.18,
                    humidity = 83,
                    feels_like = 4.77,
                    temp_min = 7.18,
                    temp_max = 7.18,
                    pressure = 1010
                ),
                weather = listOf(
                    WeatherDescription(
                        id = 500,
                        main = "Rain",
                        description = "light rain",
                        icon = "10n"
                    )
                ),
                wind = Wind(
                    speed = 3.58,
                    deg = 239
                ),
                dt_txt = "2026-02-18 12:00:00"
            )
        ),
        isOffline = !true,
        lastUpdated = 1770989488256
    )

    SkyCastTheme(darkTheme = isSystemInDarkTheme()) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            SuccessView(
                modifier = Modifier.padding(innerPadding),
                state = sampleWeatherState,
            )
        }
    }

}

// --- Helpers ---
private fun formatUnixDate(unixTime: Long): String {
    val instant = Instant.ofEpochSecond(unixTime)
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}

private fun formatTime(unixTime: Long): String {
    val instant = Instant.ofEpochSecond(unixTime)
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}