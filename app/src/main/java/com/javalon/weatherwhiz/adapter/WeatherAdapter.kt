
package com.javalon.weatherwhiz.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.javalon.weatherwhiz.databinding.WeatherItemBinding
import com.javalon.weatherwhiz.model.DailyForecast
import com.javalon.weatherwhiz.model.convertTimeStampToDay
import com.javalon.weatherwhiz.model.spannedTemperature
import com.javalon.weatherwhiz.utils.Constants
import kotlinx.android.synthetic.main.weather_cell_title.view.*

class WeatherAdapter(
    val unit: String,
    val context: Context,
    private val onTitleClick: OnTitleListener,
    private val dailyForecast: Array<DailyForecast>
) : RecyclerView.Adapter<WeatherAdapter.WeatherVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherVH {
        val itemBinding = WeatherItemBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return WeatherVH(unit, context, itemBinding, onTitleClick)
    }

    override fun getItemCount() = dailyForecast.size

    override fun onBindViewHolder(holder: WeatherVH, position: Int) {
        val forecast = dailyForecast[position]
        val isExpanded = forecast.isExpanded
        holder.itemBinding.cellDetail.root.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.bind(forecast)
    }

    class WeatherVH(
        val unit: String,
        val context: Context,
        val itemBinding: WeatherItemBinding,
        private val onTitleClickListener: OnTitleListener
    ) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {
        fun bind(forecast: DailyForecast) {
            itemBinding.cellTitle.titleLayout.setOnClickListener(this)

            with(forecast) {
                // populate cell title
                if (unit == Constants.METRIC)
                    itemBinding.cellTitle.titleLayout.weather_temp.text = spannedTemperature("C", this.feelsLike.day.toString(), context)
                else
                    itemBinding.cellTitle.titleLayout.weather_temp.text = spannedTemperature("F", this.temp.max.toString(), context)

                itemBinding.cellTitle.titleLayout.today_date.text = convertTimeStampToDay(this.dt)
                itemBinding.cellTitle.titleLayout.weather_cond.text = this.weather[0].description
                val icon = this.weather[0].icon
                val weatherIconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
                Glide.with(context)
                    .load(weatherIconUrl)
                    .override(150, 150)
                    .fitCenter()
                    .into(itemBinding.cellTitle.titleLayout.weather_image)

                // populate cell detail
                itemBinding.cellDetail.uviValue.text = this.uvi.toString()

                if (unit == Constants.METRIC)
                    itemBinding.cellDetail.feelsLikeValue.text = spannedTemperature("C", this.feelsLike.day.toString(), context)
                else
                    itemBinding.cellDetail.feelsLikeValue.text = spannedTemperature("F", this.feelsLike.day.toString(), context)

                val windSpeed = this.windSpeed.toString() + "m/s"
                itemBinding.cellDetail.windValue.text = unitSpannable(
                    windSpeed, windSpeed.indexOf("m/s"),
                    windSpeed.length
                )

                val prepValue = (this.pop * 100).toInt().toString() + "%"
                itemBinding.cellDetail.prepValue.text =
                    unitSpannable(prepValue, prepValue.indexOf("%"), prepValue.length)

                val humidityValue = this.humidity.toString() + "%"
                itemBinding.cellDetail.humidityValue.text = unitSpannable(
                    humidityValue, humidityValue.indexOf("%"),
                    humidityValue.length
                )

                val pressValue = this.pressure.toString() + "hPa"
                itemBinding.cellDetail.pressureValue.text = unitSpannable(
                    pressValue, pressValue.indexOf("hPa"),
                    pressValue.length
                )
            }
        }

        private fun unitSpannable(unit: String, startIndex: Int, endIndex: Int): SpannableString {
            val spanned = SpannableString(unit)
            spanned.setSpan(AbsoluteSizeSpan(20), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spanned
        }

        override fun onClick(p: View?) {
            onTitleClickListener.onTitleClick(adapterPosition)
        }
    }

    interface OnTitleListener {
        fun onTitleClick(position: Int)
    }
}