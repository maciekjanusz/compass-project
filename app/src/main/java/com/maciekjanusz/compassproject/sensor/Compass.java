package com.maciekjanusz.compassproject.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static java.lang.Math.*;

public class Compass implements SensorEventListener {

    private static final float FILTER_ALPHA = 0.97f;

    private final CompassListener listener;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magnetometer;

    private final float[] accelerometerValues = new float[3];
    private final float[] magnetometerValues = new float[3];
    private final float[] orientation = new float[3];
    private final float[] rotMatR = new float[9];
    private final float[] rotMatI = new float[9];

    public Compass(Context context, CompassListener listener) {
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.listener = listener;
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // magnetometer low-pass filtering
                magnetometerValues[0] = FILTER_ALPHA * magnetometerValues[0]
                        + (1 - FILTER_ALPHA) * event.values[0];
                magnetometerValues[1] = FILTER_ALPHA * magnetometerValues[1]
                        + (1 - FILTER_ALPHA) * event.values[1];
                magnetometerValues[2] = FILTER_ALPHA * magnetometerValues[2]
                        + (1 - FILTER_ALPHA) * event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // accelerometer low-pass filtering
                accelerometerValues[0] = FILTER_ALPHA * accelerometerValues[0]
                        + (1 - FILTER_ALPHA) * event.values[0];
                accelerometerValues[1] = FILTER_ALPHA * accelerometerValues[1]
                        + (1 - FILTER_ALPHA) * event.values[1];
                accelerometerValues[2] = FILTER_ALPHA * accelerometerValues[2]
                        + (1 - FILTER_ALPHA) * event.values[2];
            }

            boolean success = SensorManager
                    .getRotationMatrix(rotMatR, rotMatI, accelerometerValues, magnetometerValues);

            if (success) {
                SensorManager.getOrientation(rotMatR, orientation);
                float bearing = (float) toDegrees(orientation[0]);
                float pitch = (float) toDegrees(orientation[1]);
                float roll = (float) toDegrees(orientation[2]);
                bearing = (bearing + 360f) % 360f;

                listener.onCompassStateChanged(bearing, pitch, roll);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface CompassListener {
        /**
         * Called when obtained rotation values
         * @param bearing bearing in 0:360 format
         * @param pitch pitch in -90:90 format
         * @param roll roll in -180:180 format
         */
        void onCompassStateChanged(float bearing, float pitch, float roll);
    }
}