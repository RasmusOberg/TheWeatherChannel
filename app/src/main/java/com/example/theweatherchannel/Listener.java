package com.example.theweatherchannel;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class Listener implements SensorEventListener {
    private static final String TAG = "Listener";
    private MainActivity main;
    private String temp, pressure, humidity;
    private String tempTime, pressureTime, humidityTime;
    private String total;

    public Listener(MainActivity main){
        this.main = main;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            temp = event.values[0] + " degrees Celsius";
            tempTime = String.valueOf(event.timestamp);
        }
        if(event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY){
            humidity = event.values[0] + " % humidity";
            humidityTime = String.valueOf(event.timestamp);
        }
        if(event.sensor.getType()== Sensor.TYPE_PRESSURE){
            pressure = event.values[0] + " hPa";
            pressureTime = String.valueOf(event.timestamp);
        }
        total = "\nTemperature  = " + temp + ", Timestamp: " + tempTime +
                "\nHumidity = " + humidity + ", Timestamp: " +humidityTime +
                "\nPressure  = " + pressure + ",Timestamp: " + pressureTime;
        Log.d(TAG, "onSensorChanged: " + total);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
