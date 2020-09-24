package com.javalon.weatherwhiz.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import androidx.core.content.res.ResourcesCompat
import com.javalon.weatherwhiz.R
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
@Parcelize
data class Weather(
    @Json(name = "current")
    val current: CurrentForecast,
    @Json(name = "daily")
    val daily: List<DailyForecast>,
    @Json(name = "hourly")
    val hourly: List<HourlyForecast>,
    @Json(name = "lat")
    val lat: Double,
    @Json(name = "lon")
    val lon: Double,
    @Json(name = "timezone")
    val timezone: String,
    @Json(name = "timezone_offset")
    val timezoneOffset: Int
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class CurrentForecast(
    @Json(name = "clouds")
    val clouds: Int,
    @Json(name = "dew_point")
    val dewPoint: Double,
    @Json(name = "dt")
    val dt: Long,
    @Json(name = "feels_like")
    val feelsLike: Double,
    @Json(name = "humidity")
    val humidity: Int,
    @Json(name = "pressure")
    val pressure: Int,
    @Json(name = "sunrise")
    val sunrise: Int,
    @Json(name = "sunset")
    val sunset: Int,
    @Json(name = "temp")
    val temp: Double,
    @Json(name = "uvi")
    val uvi: Double,
    @Json(name = "visibility")
    val visibility: Int,
    @Json(name = "weather")
    val weather: List<CurrentWeather>,
    @Json(name = "wind_deg")
    val windDeg: Int,
    @Json(name = "wind_speed")
    val windSpeed: Double
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class HourlyForecast(
    @Json(name = "clouds")
    val clouds: Int,
    @Json(name = "dew_point")
    val dewPoint: Double,
    @Json(name = "dt")
    val dt: Long,
    @Json(name = "feels_like")
    val feelsLike: Double,
    @Json(name = "humidity")
    val humidity: Int,
    @Json(name = "pop")
    val pop: Double,
    @Json(name = "pressure")
    val pressure: Int,
    @Json(name = "rain")
    val rain: Rain? = null,
    @Json(name = "temp")
    val temp: Double,
    @Json(name = "visibility")
    val visibility: Int,
    @Json(name = "weather")
    val weather: List<HourlyWeather>,
    @Json(name = "wind_deg")
    val windDeg: Int,
    @Json(name = "wind_speed")
    val windSpeed: Double
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class CurrentWeather(
    @Json(name = "description")
    val description: String,
    @Json(name = "icon")
    val icon: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "main")
    val main: String
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Rain(
    @Json(name = "1h")
    val h: Double
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class HourlyWeather(
    @Json(name = "description")
    val description: String,
    @Json(name = "icon")
    val icon: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "main")
    val main: String
) : Parcelable

fun convertTimeStampToDay(dt: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dt * 1000 // date
    val timeZone = TimeZone.getDefault()
    calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.timeInMillis))

    val simpleDateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val date = calendar.get(Calendar.DATE)
    val day = simpleDateFormat.format(calendar.time)

    return "$day\n$date'"
}

@SuppressLint("SimpleDateFormat")
fun convertTimeStampToHour(dt: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dt * 1000 // date
    val timeZone = TimeZone.getDefault()
    calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.timeInMillis))

    val simpleDateFormat = SimpleDateFormat("hha", Locale.getDefault())

    return simpleDateFormat.format(calendar.time)
}

fun spannedTemperature(unit: String, temp: String, context: Context): SpannableStringBuilder {
    val spannedTemp = SpannableStringBuilder("$temp°")
    spannedTemp.append(unit)
    spannedTemp.setSpan(
        SuperscriptSpan(), spannedTemp.length - 1, spannedTemp.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannedTemp.setSpan(
        RelativeSizeSpan(0.5f), spannedTemp.length - 1, spannedTemp.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    val typeFace = Typeface.create(
        ResourcesCompat.getFont(context, R.font.metropolis_bold),
        Typeface.BOLD
    )
    spannedTemp.setSpan(
        TypefaceSpan(typeFace), spannedTemp.length - 1, spannedTemp.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    return spannedTemp
}