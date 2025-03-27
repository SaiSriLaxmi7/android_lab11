package com.humber.weathernowapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.humber.weathernowapp.ui.theme.WeatherNowAppTheme
import org.json.JSONException

class MainActivity : ComponentActivity() {

    private lateinit var editCity: EditText
    private lateinit var btnGetWeather: Button
    private lateinit var txtWeatherResult: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var progressBar: ProgressBar

    private val API_KEY = "6db3061975e04bf02c5c1c5d6b0729ee";
    private val BASE_URL = "https://api.openweathermap.org/data/2.5/weather"
    private val PREFS_NAME = "weather_prefs"
    private val CITY_KEY = "last_city"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        editCity = findViewById(R.id.editCity)
        btnGetWeather = findViewById(R.id.btnGetWeather)
        txtWeatherResult = findViewById(R.id.txtWeatherResult)
        weatherIcon = findViewById(R.id.weatherIcon)
        progressBar = findViewById(R.id.progressBar)

        // Initialize SharedPreferences
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lastCity = prefs.getString(CITY_KEY, "")

        // Load last searched city (if any)
        if (!lastCity.isNullOrEmpty()) {
            editCity.setText(lastCity)
            getWeatherInfo(lastCity)
        }

        btnGetWeather.setOnClickListener {
            val city = editCity.text.toString().trim()
            if (city.isNotEmpty()) {
                prefs.edit().putString(CITY_KEY, city).apply()
                getWeatherInfo(city)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeatherInfo(city: String) {
        val url = "$BASE_URL?q=$city&appid=$API_KEY&units=metric"

        progressBar.visibility = View.VISIBLE
        txtWeatherResult.text = ""
        weatherIcon.setImageDrawable(null)

        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE

                try {
                    val main = response.getJSONObject("main")
                    val temp = main.getDouble("temp")
                    val humidity = main.getInt("humidity")

                    val weather = response.getJSONArray("weather").getJSONObject(0)
                    val description = weather.getString("description")
                    val iconCode = weather.getString("icon")

                    val result = "Temperature: $temp°C\nHumidity: $humidity%\nCondition: $description"
                    txtWeatherResult.text = result

                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                    Glide.with(this).load(iconUrl).into(weatherIcon)

                } catch (e: JSONException) {
                    txtWeatherResult.text = "Error parsing weather data"
                }
            },
            { error ->
                progressBar.visibility = View.GONE
                txtWeatherResult.text = when (error.networkResponse?.statusCode) {
                    404 -> "City not found. Please try again."
                    else -> "Unable to fetch weather data. Check your connection."
                }
            })
        queue.add(request)
    }

}