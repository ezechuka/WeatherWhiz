package com.javalon.weatherwhiz.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javalon.weatherwhiz.model.SearchResults
import com.javalon.weatherwhiz.model.Weather
import com.javalon.weatherwhiz.repository.WeatherRepository
import com.javalon.weatherwhiz.utils.NetworkHelper
import com.javalon.weatherwhiz.utils.Resource
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

class WeatherViewModel @ViewModelInject constructor(
    private val networkHelper: NetworkHelper,
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherData = MutableLiveData<Resource<Response<Weather>>>()
    val weatherData: LiveData<Resource<Response<Weather>>>
        get() = _weatherData

    private val _searchData = MutableLiveData<Resource<Response<SearchResults>>>()
    val searchData: LiveData<Resource<Response<SearchResults>>>
        get() = _searchData

    fun getWeatherData(latitude: Double, longitude: Double, apiKey: String, unit: String) {

        viewModelScope.launch(IO) {
            _weatherData.postValue(Resource.loading(null))
            if (networkHelper.isConnected()) {
                try {
                    val response = repository.getWeatherData(latitude, longitude, apiKey, unit)
                    _weatherData.postValue(Resource.success(response))
                } catch (ex: HttpException) {
                    _weatherData.postValue(Resource.error(null, ex.code().toString()))
                } catch (ex: ConnectException) {
                    _weatherData.postValue(Resource.error(null, "Please check your internet connection!"))
                } catch (ex: SocketTimeoutException) {
                    _weatherData.postValue(Resource.error(null, "Connection timed out!"))
                } catch (ex: SocketException) {
                    _weatherData.postValue(Resource.error(null, "Please check your internet connection!"))

                }
            } else {
                _weatherData.postValue(Resource.error(null, "No internet connection"))
            }
        }
    }

    fun searchLocationData(locationName: String, apiKey:String, unit: String) {
        viewModelScope.launch(IO) {
            _searchData.postValue(Resource.loading(null))
            if (networkHelper.isConnected()) {
                try {
                    val response = repository.getSearchLocationData(locationName, apiKey, unit)
                    _searchData.postValue(Resource.success(response))
                } catch (ex: HttpException) {
                    _weatherData.postValue(Resource.error(null, ex.code().toString()))
                } catch (ex: ConnectException) {
                    _weatherData.postValue(Resource.error(null, "Please check your internet connection!"))
                } catch (ex: SocketTimeoutException) {
                    _weatherData.postValue(Resource.error(null, "Connection timed out!"))
                } catch (ex: SocketException) {
                    _weatherData.postValue(Resource.error(null, "Please check your internet connection!"))
                }
            } else {
                _weatherData.postValue(Resource.error(null, "No internet connection"))
            }
        }
    }

}