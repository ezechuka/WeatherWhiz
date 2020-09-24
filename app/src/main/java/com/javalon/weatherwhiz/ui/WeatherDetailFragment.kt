package com.javalon.weatherwhiz.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.javalon.weatherwhiz.R
import com.javalon.weatherwhiz.adapter.WeatherAdapter
import com.javalon.weatherwhiz.databinding.FragmentWeatherDetailBinding
import com.javalon.weatherwhiz.model.DailyForecast
import com.javalon.weatherwhiz.utils.Constants

class WeatherDetailFragment : Fragment(), WeatherAdapter.OnTitleListener {

    private lateinit var detailBinding: FragmentWeatherDetailBinding
    private lateinit var forecast: Array<DailyForecast>
    private lateinit var adapter: WeatherAdapter
    private val TAG: String? = "WEATHER-HOME"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        detailBinding = FragmentWeatherDetailBinding.inflate(inflater, container, false)
        forecast = WeatherDetailFragmentArgs.fromBundle(requireArguments()).dailyForecast

        val sharedPref = requireContext().getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val unit = sharedPref.getString(Constants.UNITS, Constants.METRIC).toString()

        adapter = WeatherAdapter(unit, requireContext(), this, forecast)
        detailBinding.location.text = forecast[0].location
        detailBinding.recyclerviewForecast.adapter = adapter
        detailBinding.recyclerviewForecast.layoutManager = LinearLayoutManager(requireActivity())
        detailBinding.recyclerviewForecast.addItemDecoration(DividerItemDecoration(requireActivity(),
                    DividerItemDecoration.VERTICAL))

        return detailBinding.root
    }

    override fun onTitleClick(position: Int) {
        val forecast = forecast[position]
        forecast.isExpanded = !forecast.isExpanded
        adapter.notifyItemChanged(position)
    }
}