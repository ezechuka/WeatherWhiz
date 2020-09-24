package com.javalon.weatherwhiz.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.javalon.weatherwhiz.databinding.PopItemBinding
import com.javalon.weatherwhiz.model.HourlyForecast
import com.javalon.weatherwhiz.model.convertTimeStampToHour
import com.javalon.weatherwhiz.model.spannedTemperature
import com.javalon.weatherwhiz.utils.Constants

class PopAdapter(val unit: String, val context: Context, val hourlyForecast: List<HourlyForecast>) : RecyclerView.Adapter<PopAdapter.PopVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopVH {
        val itemBinding = PopItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PopVH(unit, context, itemBinding)
    }

    override fun getItemCount(): Int = hourlyForecast.size

    override fun onBindViewHolder(holder: PopVH, position: Int) {
        val forecast = hourlyForecast[position]
        holder.bind(forecast)
    }

    class PopVH(val unit: String, val context: Context, val itemBinding: PopItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(hourlyForecast: HourlyForecast) {
            with (hourlyForecast) {
                itemBinding.weatherCond.text = this.weather[0].main
                itemBinding.pop.text = (this.pop * 100).toInt().toString() + "%"

                val icon = this.weather[0].icon
                val weatherIconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
                Glide.with(context)
                    .load(weatherIconUrl)
                    .override(150, 150)
                    .fitCenter()
                    .into(itemBinding.weatherIcon)

                if (unit == Constants.METRIC)
                    itemBinding.hourlyTemp.text = spannedTemperature("C", this.temp.toInt().toString(), context)
                else
                    itemBinding.hourlyTemp.text = spannedTemperature("F", this.temp.toInt().toString(), context)

                itemBinding.hour.text = convertTimeStampToHour(this.dt)
            }
        }
    }
}