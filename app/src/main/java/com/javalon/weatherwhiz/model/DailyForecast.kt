package com.javalon.weatherwhiz.model


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class DailyForecast(
    var isExpanded: Boolean = false,
    var location: String? = null,
    @Json(name = "clouds")
    val clouds: Int,
    @Json(name = "dew_point")
    val dewPoint: Double,
    @Json(name = "dt")
    val dt: Long,
    @Json(name = "feels_like")
    val feelsLike: FeelsLike,
    @Json(name = "humidity")
    val humidity: Int,
    @Json(name = "pop")
    val pop: Double,
    @Json(name = "pressure")
    val pressure: Int,
    @Json(name = "rain")
    val rain: Double? = null,
    @Json(name = "sunrise")
    val sunrise: Int,
    @Json(name = "sunset")
    val sunset: Int,
    @Json(name = "temp")
    val temp: Temp,
    @Json(name = "uvi")
    val uvi: Double? = null,
    @Json(name = "weather")
    val weather: List<DailyWeather>,
    @Json(name = "wind_deg")
    val windDeg: Int,
    @Json(name = "wind_speed")
    val windSpeed: Double
): Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class FeelsLike(
    @Json(name = "day")
    val day: Double,
    @Json(name = "eve")
    val eve: Double,
    @Json(name = "morn")
    val morn: Double,
    @Json(name = "night")
    val night: Double
): Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Temp(
    @Json(name = "day")
    val day: Double,
    @Json(name = "eve")
    val eve: Double,
    @Json(name = "max")
    val max: Double,
    @Json(name = "min")
    val min: Double,
    @Json(name = "morn")
    val morn: Double,
    @Json(name = "night")
    val night: Double
): Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class DailyWeather(
    @Json(name = "description")
    val description: String,
    @Json(name = "icon")
    val icon: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "main")
    val main: String
): Parcelable