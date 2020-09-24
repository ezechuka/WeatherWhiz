package com.javalon.weatherwhiz.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.javalon.weatherwhiz.BuildConfig
import com.javalon.weatherwhiz.R
import com.javalon.weatherwhiz.adapter.PopAdapter
import com.javalon.weatherwhiz.databinding.FragmentWeatherHomeBinding
import com.javalon.weatherwhiz.model.SearchResults
import com.javalon.weatherwhiz.model.Weather
import com.javalon.weatherwhiz.model.convertTimeStampToDay
import com.javalon.weatherwhiz.model.spannedTemperature
import com.javalon.weatherwhiz.utils.Constants
import com.javalon.weatherwhiz.utils.GpsUtils
import com.javalon.weatherwhiz.utils.LocationUtils
import com.javalon.weatherwhiz.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WeatherHomeFragment : Fragment() {

    private val TAG: String? = "WEATHER-HOME"
    private lateinit var weatherBinding: FragmentWeatherHomeBinding
    private val weatherVM: WeatherViewModel by viewModels()
    private val REQUEST_LOCATION_CODE = 1
    private var isGps = false
    private lateinit var location: String
    private lateinit var progressDialog: Dialog
    private lateinit var dialog: Dialog
    private lateinit var editor: SharedPreferences.Editor
    private var hasInitializedRootView: Boolean = false
    private var rootView: View? = null
    private var unit = Constants.METRIC
    private lateinit var request: String
    private lateinit var city: String

    @Inject
    lateinit var locUtils: LocationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = Dialog(requireActivity())
        progressDialog.setCancelable(false)
        progressDialog.setContentView(R.layout.progress_dialog)
        progressDialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )
        progressDialog.show()

        GpsUtils(requireActivity()).turnOnGps(object : GpsUtils.OnGpsListener {
            override fun gpsStatus(isGPSEnabled: Boolean) {
                isGps = isGPSEnabled
                if (isGps)
                    getLocation()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val sharedPref = requireContext().getSharedPreferences(TAG, Context.MODE_PRIVATE)
        editor = sharedPref.edit()

        unit = sharedPref.getString(Constants.UNITS, Constants.METRIC).toString()
        return getPersistentView(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasInitializedRootView) {
            hasInitializedRootView = true
            hideWeatherInfoViews()

            weatherBinding.search.setOnClickListener { displaySearchDialog() }
            weatherBinding.refresh.setOnClickListener {
                if (request == Constants.LOCATION_WEATHER_REQUEST)
                    getLocation()
                else
                    initSearchObserver(city, BuildConfig.API_ID, unit)
            }

            weatherBinding.retryButton.setOnClickListener {
                if (request == Constants.LOCATION_WEATHER_REQUEST)
                    getLocation()
                else
                    initSearchObserver(city, BuildConfig.API_ID, unit)
            }

            weatherBinding.tempToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.celsius_toggle -> {
                        unit = Constants.METRIC
                        editor.putString(Constants.UNITS, Constants.METRIC)
                        editor.commit()

                        if (request == Constants.LOCATION_WEATHER_REQUEST)
                            getLocation()
                        else
                            initSearchObserver(city, BuildConfig.API_ID, unit)
                    }
                    R.id.fahrenheit_toggle -> {
                        unit = Constants.IMPERIAL
                        editor.putString(Constants.UNITS, Constants.IMPERIAL)
                        editor.commit()

                        if (request == Constants.LOCATION_WEATHER_REQUEST)
                            getLocation()
                        else
                            initSearchObserver(city, BuildConfig.API_ID, unit)
                    }
                }
            }
            weatherBinding.currentLocation.setOnClickListener {
                it.isEnabled = false
                getLocation()
            }
        }
    }

    val weatherRequest: (address: MutableList<Address>) -> Unit = {
        val lat = it[0].latitude
        val long = it[0].longitude
        val locality = it[0].locality
        val country = it[0].countryName

        location = "$locality, $country"

        weatherBinding.locationState.text = locality
        weatherBinding.locationCountry.text = country

        initObserver(lat, long, BuildConfig.API_ID, unit)
    }

    private fun getLocation() {
        if ((ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            && (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
        ) {

            locUtils.address.observeOnce(this@WeatherHomeFragment, Observer {
                weatherRequest(it as MutableList<Address>)
            })
        } else
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_LOCATION_CODE
            )
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            if (isGps)
                getLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.APP_GPS_REQUEST) {
                isGps = true
                getLocation()
            }
        }
    }

    private fun initSearchObserver(locationName: String, apiKey: String, unit: String) {
        request = Constants.SEARCH_WEATHER_REQUEST
        weatherVM.searchLocationData(locationName, apiKey, unit)

        weatherVM.searchData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    if (::dialog.isInitialized)
                        dialog.dismiss()
                    hideErrorViews()
                    showWeatherInfoViews()
                    bindViews(it.data?.body()!!)
                }
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.ERROR -> {
                    progressDialog.dismiss()
                    if (::dialog.isInitialized)
                        dialog.dismiss()
                    hideWeatherInfoViews()
                    showErrorViews()
                }
            }
        })
    }

    private fun bindViews(searchResults: SearchResults) {
        with(searchResults) {
            val mainWeatherCond = this.weather[0].main.toLowerCase()
            setWeatherBg(mainWeatherCond)

            weatherBinding.popRecyclerview.visibility = View.GONE
            weatherBinding.chanceLabel.visibility = View.GONE
            weatherBinding.divider.visibility = View.GONE

            if(!weatherBinding.currentLocation.isEnabled)
                weatherBinding.currentLocation.isEnabled = true

            weatherBinding.locationState.text = this.name
            weatherBinding.locationCountry.text = this.sys.country
            weatherBinding.todayDate.text = convertTimeStampToDay(this.dt)

            if (unit == Constants.METRIC) {
                weatherBinding.weatherTemp.text = spannedTemperature("C", this.main.temp.toString(), requireContext())

                weatherBinding.feelsLikeValue.text =
                    spannedTemperature("C", this.main.feelsLike.toString(), requireContext())
                weatherBinding.minTempValue.text = spannedTemperature("C", this.main.tempMin.toString(), requireContext())
                weatherBinding.maxTempValue.text = spannedTemperature("C", this.main.tempMax.toString(), requireContext())
            } else {
                weatherBinding.weatherTemp.text = spannedTemperature("F", this.main.temp.toString(), requireContext())

                weatherBinding.feelsLikeValue.text =
                    spannedTemperature("F", this.main.feelsLike.toString(), requireContext())
                weatherBinding.minTempValue.text = spannedTemperature("F", this.main.tempMin.toString(), requireContext())
                weatherBinding.maxTempValue.text = spannedTemperature("F", this.main.tempMax.toString(), requireContext())
            }

            val windSpeed = this.wind.speed.toString() + "m/s"
            weatherBinding.windSpeedValue.text = unitSpannable(windSpeed, windSpeed.indexOf("m/s"), windSpeed.length)

            val humidity = this.main.humidity.toString() + "%"
            weatherBinding.humidityValue.text = unitSpannable(humidity, humidity.indexOf("%"), humidity.length)

            val pressure = this.main.pressure.toString() + "hPa"
            weatherBinding.pressureValue.text = unitSpannable(pressure, pressure.indexOf("hPa"), pressure.length)

            weatherBinding.searchWeatherInfo.visibility = View.VISIBLE

            weatherBinding.weatherCond.text = this.weather[0].main

            val icon = this.weather[0].icon
            val weatherIconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
            Log.e(TAG, weatherIconUrl)
            Glide.with(this@WeatherHomeFragment)
                .load(weatherIconUrl)
                .override(150, 150)
                .fitCenter()
                .into(weatherBinding.weatherIcon)

            weatherBinding.cardviewWeatherInfo.setOnClickListener(null)
        }
    }

    private fun initObserver(latitude: Double, longitude: Double, apiKey: String, unit: String) {
        request = Constants.LOCATION_WEATHER_REQUEST
        weatherVM.getWeatherData(latitude, longitude, apiKey, unit)

        weatherVM.weatherData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    bindViews(it.data?.body()!!)
                    showWeatherInfoViews()
                    hideErrorViews()
                }
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.ERROR -> {
                    progressDialog.dismiss()
                    hideWeatherInfoViews()
                    showErrorViews()
                }
            }
        })
    }

    private fun bindViews(data: Weather) {
        if (weatherBinding.searchWeatherInfo.isVisible)
            weatherBinding.searchWeatherInfo.visibility = View.GONE

        with(data) {
            val mainWeatherCond = this.current.weather[0].main.toLowerCase()
            setWeatherBg(mainWeatherCond)

            val adapter = PopAdapter(unit, requireContext(), this.hourly.subList(0, 24))
            weatherBinding.popRecyclerview.adapter = adapter
            weatherBinding.popRecyclerview.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

            val desc = this.current.weather[0].main
            val temp = this.current.temp
            val date = convertTimeStampToDay(this.current.dt)

            val icon = this.current.weather[0].icon
            val weatherIconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
            Log.e(TAG, weatherIconUrl)
            Glide.with(this@WeatherHomeFragment)
                .load(weatherIconUrl)
                .override(150, 150)
                .fitCenter()
                .into(weatherBinding.weatherIcon)

            weatherBinding.weatherCond.text = desc

            this.daily[0].location = location
            if (unit == Constants.METRIC)
                weatherBinding.weatherTemp.text = spannedTemperature("C", temp.toString(), requireContext())
            else
                weatherBinding.weatherTemp.text = spannedTemperature("F", temp.toString(), requireContext())
            weatherBinding.todayDate.text = date

            weatherBinding.cardviewWeatherInfo.setOnClickListener {
                val detailAction =
                    WeatherHomeFragmentDirections.actionHomeToDetail(this.daily.toTypedArray())
                findNavController().navigate(detailAction)
            }

            val hourlyForecast = this.hourly

//            val prevData = weatherBinding.popLineChart.data
//            if (prevData != null) {
//                weatherBinding.popLineChart.clear()
//                weatherBinding.popLineChart.zoom(0f, 0f, 0f, 0f)
//            }
//
//            val entries = mutableListOf<Entry>()
//            val referenceTimestamp = this.hourly[0].dt
//            val newTimestamps = mutableListOf<Long>()
//            for (i in this.hourly.indices) {
//                if (i == 24) // 24 hours
//                    break
//
//                val newDt = this.hourly[i].dt - referenceTimestamp
//                val entry = Entry(newDt.toFloat(), this.hourly[i].pop.toFloat())
//                entries.add(entry)
//                newTimestamps.add(newDt)
//
//                Log.e(TAG, "Time: " + newDt.toString())
////                Log.e(TAG, "PoP: " + this.hourly[i].pop.toFloat())
//            }
//            Log.e(TAG, entries.size.toString())
//
//            val tf = Typeface.create(
//                ResourcesCompat.getFont(requireContext(), R.font.metropolis_regular),
//                Typeface.NORMAL
//            )
//            val lineDataset = LineDataSet(entries, "Chance of rain")
//            lineDataset.axisDependency = YAxis.AxisDependency.LEFT
//            lineDataset.valueTextColor = Color.WHITE
//            lineDataset.valueTypeface = tf
//
//            lineDataset.setCircleColor(Color.WHITE)
//            lineDataset.setDrawCircleHole(false)
//            lineDataset.setDrawFilled(true)
//
//            lineDataset.fillColor = Color.parseColor("#03DAC5")
////            lineDataset.fillDrawable = requireContext().resources.getDrawable(R.drawable.rainy, null)
//            lineDataset.fillAlpha = 50
//            lineDataset.setDrawHighlightIndicators(false)
////            lineDataset.mode = LineDataSet.Mode.CUBIC_BEZIER
////            lineDataset.cubicIntensity = 0.4f
//            lineDataset.circleRadius = 2f
//            lineDataset.enableDashedLine(10f, 2f, 0f)
//            lineDataset.valueFormatter = object : ValueFormatter() {
//
//                override fun getPointLabel(entry: Entry?): String {
//                    return "${(entry!!.y * 100.0).toInt()}%"
//                }
//            }
//
//            val dataSet = listOf<ILineDataSet>(lineDataset)
//            val lineData = LineData(dataSet)
//            weatherBinding.popLineChart.data = lineData
//
//            val xAxis = weatherBinding.popLineChart.xAxis
//
//            xAxis.setDrawGridLines(false)
//            xAxis.setDrawAxisLine(false)
////            xAxis.isGranularityEnabled = true
////            xAxis.granularity = 4f
//            xAxis.textColor = Color.WHITE
//            xAxis.typeface = tf
//
//            xAxis.setAvoidFirstLastClipping(true)
////            xAxis.setLabelCount(entries.size, true)
////            xAxis.valueFormatter = object : ValueFormatter() {
////
////                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
////                    Log.e(TAG, value.toString())
////                    val index = (value / 10000).toInt()
////                    return convertTimeStampToHour(referenceTimestamp + newTimestamps[index])
////                }
////            }
//
////            val yAxis = weatherBinding.popLineChart.axisLeft
////            yAxis.isGranularityEnabled = true
////            yAxis.granularity = 4f
//
//            val chartDesc = weatherBinding.popLineChart.description
//            chartDesc.text = "Chance of rain"
//            chartDesc.textColor = Color.WHITE
//            chartDesc.typeface = tf
//            chartDesc.textSize = 12f
//            weatherBinding.popLineChart.legend.isEnabled = false
//            weatherBinding.popLineChart.axisRight.isEnabled = false
//            weatherBinding.popLineChart.axisLeft.isEnabled = false
//            weatherBinding.popLineChart.zoom(2f, 0f, 0f, 2f)
//            weatherBinding.popLineChart.setPinchZoom(false)
//            weatherBinding.popLineChart.isScaleXEnabled = false
//            weatherBinding.popLineChart.isScaleYEnabled = false
//            weatherBinding.popLineChart.animateXY(3000, 3000, Easing.EaseInCubic)
//
//            weatherBinding.popLineChart.invalidate()
        }
    }

    private fun hideWeatherInfoViews() {
        // hide weather info vies & display error msg & retry button
        weatherBinding.currentLocation.visibility = View.GONE
        weatherBinding.locationCountry.visibility = View.GONE
        weatherBinding.locationState.visibility = View.GONE
        weatherBinding.search.visibility = View.GONE
        weatherBinding.popRecyclerview.visibility = View.GONE
        weatherBinding.weatherCond.visibility = View.GONE
        weatherBinding.weatherIcon.visibility = View.GONE
        weatherBinding.weatherTemp.visibility = View.GONE
        weatherBinding.moreTextview.visibility = View.GONE
        weatherBinding.todayDate.visibility = View.GONE
        weatherBinding.refresh.visibility = View.GONE
        weatherBinding.tempToggleGroup.visibility = View.GONE
        weatherBinding.chanceLabel.visibility = View.GONE
        weatherBinding.divider.visibility = View.GONE
    }

    private fun showWeatherInfoViews() {
        // show weather info vies & hide error msg & retry button
        weatherBinding.currentLocation.visibility = View.VISIBLE
        weatherBinding.locationCountry.visibility = View.VISIBLE
        weatherBinding.locationState.visibility = View.VISIBLE
        weatherBinding.search.visibility = View.VISIBLE
        weatherBinding.popRecyclerview.visibility = View.VISIBLE
        weatherBinding.weatherCond.visibility = View.VISIBLE
        weatherBinding.weatherIcon.visibility = View.VISIBLE
        weatherBinding.weatherTemp.visibility = View.VISIBLE
        weatherBinding.moreTextview.visibility = View.VISIBLE
        weatherBinding.todayDate.visibility = View.VISIBLE
        weatherBinding.refresh.visibility = View.VISIBLE
        weatherBinding.chanceLabel.visibility = View.VISIBLE
        weatherBinding.divider.visibility = View.VISIBLE

        weatherBinding.tempToggleGroup.visibility = View.VISIBLE
        if (unit == Constants.METRIC)
            weatherBinding.tempToggleGroup.check(R.id.celsius_toggle)
        else
            weatherBinding.tempToggleGroup.check(R.id.fahrenheit_toggle)
    }

    private fun hideErrorViews() {
        weatherBinding.offlineTitle.visibility = View.GONE
        weatherBinding.offlineMsg.visibility = View.GONE
        weatherBinding.retryButton.visibility = View.GONE
    }

    private fun showErrorViews() {
        weatherBinding.offlineTitle.visibility = View.VISIBLE
        weatherBinding.offlineMsg.visibility = View.VISIBLE
        weatherBinding.retryButton.visibility = View.VISIBLE

    }

    private fun displaySearchDialog() {
        dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.search_dialog)
        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        val searchEditText = dialog.findViewById<TextInputEditText>(R.id.search_bar)
        val continueButton = dialog.findViewById<MaterialButton>(R.id.continue_button)

        continueButton.setOnClickListener {
            city = searchEditText.text.toString()

            if (location.isEmpty())
                searchEditText.error = "EMPTY"
            else
                initSearchObserver(city, BuildConfig.API_ID, unit)
        }

        dialog.show()
    }

    private fun unitSpannable(unit: String, startIndex: Int, endIndex: Int) : SpannableString {
        val spanned = SpannableString(unit)
        spanned.setSpan(AbsoluteSizeSpan(20), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spanned
    }

    private fun setWeatherBg(condition: String) {
        when (condition) {
            Constants.DRIZZLE, Constants.RAIN, Constants.SNOW ->
                weatherBinding.weatherHome.background = resources.getDrawable(R.drawable.rainy, null)
            Constants.CLEAR -> weatherBinding.weatherHome.background = resources.getDrawable(R.drawable.clear, null)
            Constants.CLOUDS -> weatherBinding.weatherHome.background = resources.getDrawable(R.drawable.cloudy, null)
            Constants.FOG, Constants.SMOKE, Constants.MIST ->
                weatherBinding.weatherHome.background = resources.getDrawable(R.drawable.fog, null)
            Constants.HAZE, Constants.SAND, Constants.DUST ->
                weatherBinding.weatherHome.background = resources.getDrawable(R.drawable.sand, null)
            Constants.TORNADO -> weatherBinding.weatherHome.background = resources.getDrawable(R.drawable.tornado, null)
            Constants.THUNDERSTORM -> weatherBinding.weatherHome.background = resources.getDrawable(R.drawable.storm, null)
        }
    }

    private fun getPersistentView(inflater: LayoutInflater, container: ViewGroup?) : View? {
        if (rootView == null) {
            weatherBinding = FragmentWeatherHomeBinding.inflate(inflater, container, false)
            rootView = weatherBinding.root
        } else {
            val root = weatherBinding.root
            (weatherBinding.root as ViewGroup).removeView(root)
        }

        return rootView
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
}