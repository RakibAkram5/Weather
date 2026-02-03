package mad.srs.weather;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity {
    TextView cityName;
    Button search;
    String url;
    TextView show;

    class getWeather extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                reader.close();
                urlConnection.disconnect();
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null || result.isEmpty()) {
                show.setText("Cannot find weather data");
                Toast.makeText(MainActivity.this, "Failed to fetch weather", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Parse JSON response
                JSONObject jsonObject = new JSONObject(result);

                // Check if city was found
                if (jsonObject.has("cod") && jsonObject.getString("cod").equals("404")) {
                    show.setText("City not found");
                    return;
                }

                // Extract city name
                String cityName = jsonObject.getString("name");

                // Extract temperature (convert from Kelvin to Celsius)
                JSONObject main = jsonObject.getJSONObject("main");
                double temperature = main.getDouble("temp") - 273.15;
                double feelsLike = main.getDouble("feels_like") - 273.15;
                int humidity = main.getInt("humidity");
                double pressure = main.getDouble("pressure");

                // Extract weather description
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                String description = weatherObj.getString("description");
                String mainWeather = weatherObj.getString("main");

                // Extract wind speed
                JSONObject wind = jsonObject.getJSONObject("wind");
                double windSpeed = wind.getDouble("speed");

                // Build display string
                String weatherInfo = "City: " + cityName + "\n\n" +
                        "Temperature: " + String.format("%.1f", temperature) + "°C\n" +
                        "Feels Like: " + String.format("%.1f", feelsLike) + "°C\n" +
                        "Weather: " + mainWeather + "\n" +
                        "Description: " + description + "\n" +
                        "Humidity: " + humidity + "%\n" +
                        "Pressure: " + pressure + " hPa\n" +
                        "Wind Speed: " + windSpeed + " m/s";

                show.setText(weatherInfo);

            } catch (Exception e) {
                e.printStackTrace();
                show.setText("Error parsing weather data");
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.editTextCity);
        search = findViewById(R.id.search);
        show = findViewById(R.id.textview);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString().trim();

                if (city != null && !city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Fetching weather...", Toast.LENGTH_SHORT).show();
                    url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=d844c613481bd475cbbd4bdc118c2e61";
                    getWeather task = new getWeather();
                    task.execute(url);
                } else {
                    Toast.makeText(MainActivity.this, "Enter City", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}