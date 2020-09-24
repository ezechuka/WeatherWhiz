package com.javalon.weatherwhiz.api

import com.javalon.weatherwhiz.model.SearchResults
import com.javalon.weatherwhiz.model.Weather
import retrofit2.Response

interface ApiHelper {
    suspend fun getWeatherData(latitude: Double, longitude: Double,
        apiKey: String, unit: String) : Response<Weather>

    suspend fun searchWeatherLocationData(
        locationName: String, apiKey: String, unit: String
    ) : Response<SearchResults>
}