package com.javalon.weatherwhiz.api

import com.javalon.weatherwhiz.model.SearchResults
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService): ApiHelper {
    override suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        unit: String
    ) = apiService.getWeatherData(latitude, longitude, apiKey, unit)

    override suspend fun searchWeatherLocationData(locationName: String, apiKey: String, unit: String): Response<SearchResults> =
        apiService.searchWeatherLocationData(locationName, apiKey, unit)
}