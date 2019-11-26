package com.example.theweatherchannel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private Sensor pressure, temp, humidity;
    private boolean tempPresent, pressurePresent, humidityPresent;
    private TextView information;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        listener = new Listener(this);
        information = findViewById(R.id.information);

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
        new FetchWeatherInformation().execute("api.openweathermap.org/data/2.5/weather?lat=35&lon=139");
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
        @Override
        protected String doInBackground(String... strings) {
            return null;
        }

    }
}
