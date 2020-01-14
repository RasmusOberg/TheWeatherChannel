package com.example.theweatherchannel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import static android.hardware.SensorManager.getAltitude;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private Sensor pressure, temp, humidity;
    private boolean tempPresent, pressurePresent, humidityPresent;
    private double longitude, latitude;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String api;
    private Button apiButton, sensorButton, stopSensorButton, compare;
    private TextView apiPressure, apiTemp, apiHumidity, sensorPressure, sensorTemp, sensorHumidity, difference;
    private double sTemp, aTemp, aPressure, sPressure, sHumidity, aHumidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        listener = new Listener(this);
        initializeComponents();
        getLongitudeLatitude();
    }

    public void initializeComponents(){
        difference = findViewById(R.id.differences);
        apiPressure = findViewById(R.id.apiPressure);
        apiTemp = findViewById(R.id.apiTemp);
        apiHumidity = findViewById(R.id.apiHumidity);
        sensorHumidity = findViewById(R.id.sensorHumdity);
        sensorPressure = findViewById(R.id.sensorPressure);
        sensorTemp = findViewById(R.id.sensorTemp);
        apiButton = findViewById(R.id.apiButton);
        apiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchWeatherInformation().execute(api);
            }
        });
        sensorButton = findViewById(R.id.sensorButton);
        sensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSensors();
            }
        });
        stopSensorButton = findViewById(R.id.stopSensorButton);
        stopSensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterSensorListeners();
            }
        });
        compare = findViewById(R.id.compare);
        compare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alldiff;
                String tempDiff = "";
//                if(aTemp ||)
                if(sTemp == aTemp){
                    tempDiff += "Both show the same : " + sTemp + " C";
                }else if(sTemp > aTemp){
                    double diff = sTemp - aTemp;
                    tempDiff += "Sensor is showing " + diff + " higher than API";
                }else{
                    double diff = aTemp - sTemp;
                    tempDiff += "API is showing " + diff + " higher than the sensor";
                }
            }
        });
    }

    public void getLongitudeLatitude() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d(TAG, "onLocationChanged: " + latitude + " " + longitude);
                api = "http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&units=metric&APPID=868caba3626811db78521241d52982a3";
                Log.d(TAG, "onLocationChanged: " + api);

            }

            public void onProviderDisabled(String provider) { }

            public void onProviderEnabled(String provider) {}

            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    public void getSensors(){
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            tempPresent = true;
            sensorManager.registerListener(listener, temp, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Ambient Temperature-sensor not available.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: Ambient Temperature not available.");
            tempPresent = false;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            humidityPresent = true;
            sensorManager.registerListener(listener, humidity, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Relative Humidity-sensor not available.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: Relative Humidity Sensor not available.");
            humidityPresent = false;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            pressurePresent = true;
            pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(listener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Pressure-sensor not available.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: Pressure Sensor not available.");
            pressurePresent = false;
        }
    }

    protected void onResume() {
        super.onResume();
        if (tempPresent)
            sensorManager.registerListener(listener, temp, SensorManager.SENSOR_DELAY_NORMAL);
        if (humidityPresent)
            sensorManager.registerListener(listener, humidity, SensorManager.SENSOR_DELAY_NORMAL);
        if (pressurePresent)
            sensorManager.registerListener(listener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensorListeners();
    }

    public void unregisterSensorListeners(){
        if (tempPresent)
            sensorManager.unregisterListener(listener);
        if (humidityPresent)
            sensorManager.unregisterListener(listener);
        if (pressurePresent)
            sensorManager.unregisterListener(listener);
    }

    public void setTemp(String string, double a){
        sensorTemp.setText(string + " degrees C");
        sTemp = a;
    }

    public void setPressure(String string, double a){
        sensorPressure.setText(string + " hPa");
        sPressure = a;
    }

    public void setHumidity(String string, double a){
        sensorHumidity.setText(string + " %");
        sHumidity = a;
    }


    private class FetchWeatherInformation extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG, "doInBackground: " + urls[0]);
            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream is = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                int data = reader.read();
                while(data != -1){
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }
                return result;
            }catch (MalformedURLException e){
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: "+ e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try{
                String weather = new JSONObject(s).getString("weather"); //array with general information
                Log.d(TAG, "onPostExecute: Weather = " + weather);

                JSONArray array = new JSONArray(weather);
                String mainWeather = array.getString(0);
//                Log.d(TAG, "onPostExecute: test = " + array);
//                Log.d(TAG, "onPostExecute: test2 = " + mainWeather);
//                String main = new JSONObject(mainWeather).getString("main");
                String weatherDescription = new JSONObject(mainWeather).getString("description");
//                Log.d(TAG, "onPostExecute: mainweather = " + main);
//                Log.d(TAG, "onPostExecute: weatherdesc = " + weatherDescription);
                String main1 = new JSONObject(s).getString("main");
                String temperature = new JSONObject(main1).getString("temp");
                String pressure = new JSONObject(main1).getString("pressure");
                String humidity = new JSONObject(main1).getString("humidity");
                String windspeed = new JSONObject(new JSONObject(s).getString("wind")).getString("speed");
                float altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, Float.valueOf(pressure));
//                Log.d(TAG, "onPostExecute: temp = " + temperature);
//                Log.d(TAG, "onPostExecute: pressure = " + pressure);
//                Log.d(TAG, "onPostExecute: wind = " + windspeed);
                Log.d(TAG, "onPostExecute: humidity = " + humidity);
//                Log.d(TAG, "onPostExecute: Altitude = " + altitude);
                aTemp = Double.parseDouble(temperature);
                aPressure = Double.parseDouble(pressure);
                aHumidity = Double.parseDouble(humidity);
                apiTemp.setText(temperature + " degrees C");
                apiHumidity.setText(humidity + " %");
                apiPressure.setText(pressure + " hPa");
            }catch(JSONException e){
                Log.d(TAG, "onPostExecute: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
