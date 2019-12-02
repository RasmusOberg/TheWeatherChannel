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
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private Sensor pressure, temp, humidity;
    private boolean tempPresent, pressurePresent, humidityPresent;
    private TextView information;
    private double longitude, latitude;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String api;
    private int test = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        listener = new Listener(this);
        information = findViewById(R.id.information);
        getLongitudeLatitude();
        getSensors();
//        new FetchWeatherInformation().execute("api.openweathermap.org/data/2.5/weather?lat=35&lon=139");
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
                new FetchWeatherInformation().execute(api);
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
            Log.d(TAG, "onCreate: Ambient Temperature not available.");
            tempPresent = false;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            humidityPresent = true;
            sensorManager.registerListener(listener, humidity, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d(TAG, "onCreate: Relative Humidity Sensor not available.");
            humidityPresent = false;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            pressurePresent = true;
            pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(listener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
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
        if (tempPresent)
            sensorManager.unregisterListener(listener);
        if (humidityPresent)
            sensorManager.unregisterListener(listener);
        if (pressurePresent)
            sensorManager.unregisterListener(listener);
    }

    public void setInformation(String text) {
        information.setText(text);
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
                String object = new JSONObject(s).getString("weather"); //array with general information
                Log.d(TAG, "onPostExecute: Weather = " + object);
                String obj2 = new JSONObject(s).getString("main");
                Log.d(TAG, "onPostExecute: main = " + obj2);
            }catch(JSONException e){
                Log.d(TAG, "onPostExecute: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
