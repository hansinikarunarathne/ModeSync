package com.example.smartmodeswitcher.sensors;

import com.example.smartmodeswitcher.models.ContextResult;

import java.util.LinkedList;
import java.util.Queue;

public class ContextDetector {


    private static final int WINDOW_SIZE = 40; // 5 seconds at 200ms/sample

    private static final float STATIONARY_THRESHOLD = 0.01f;
    public static final float ACCELEROMETER_THRESHOLD = 9.7f;
    public static final float PROXIMITY_THRESHOLD = 0.5f;
    public static final float LIGHT_SENSOR_THRESHOLD = 5f;
    private static String lastContext = "Unknown";

    private static final Queue<Float> magnitudeWindow = new LinkedList<>();


    public static ContextResult detect(float[] accelValues, float lightValue, float proximityValue, float[] gyroValues) {
        float ax = accelValues[0];
        float ay = accelValues[1];
        float az = accelValues[2];
        float magnitude = (float) Math.sqrt(ax * ax + ay * ay + az * az);

        // Maintain sliding window
        if (magnitudeWindow.size() >= WINDOW_SIZE) {
            magnitudeWindow.poll();
        }
        magnitudeWindow.offer(magnitude);

        if (magnitudeWindow.size() < WINDOW_SIZE) {
            return new ContextResult(lastContext, lastContext); // Not enough data yet
        }

        float mean = mean(magnitudeWindow);
        float var = variance(magnitudeWindow, mean);

        boolean isStationary = var < STATIONARY_THRESHOLD;
        boolean isWalking = var >= STATIONARY_THRESHOLD;

        // Desk detection: flat orientation (face-up or face-down)
        boolean isDesk = Math.abs(az) > ACCELEROMETER_THRESHOLD && isStationary;

        // Pocket detection: proximity near and light very low
        boolean isPocket = lightValue < LIGHT_SENSOR_THRESHOLD || proximityValue == 0;

        // Hand detection: not flat, proximity not near (e.g., holding in hand)
        boolean isHand = Math.abs(az) < ACCELEROMETER_THRESHOLD && proximityValue > PROXIMITY_THRESHOLD;

        // Determine posture context
        String stableContext = lastContext;
        if (isPocket) {
            stableContext = "In Pocket";
        } else if (isDesk) {
            stableContext = "On Desk";
        } else if (isHand) {
            stableContext = "In Hand";
        } else {
            stableContext = "Unknown";
        }

        String motionContext = lastContext;
        if (isStationary) {
            motionContext = "Stationary";
        } else if (isWalking) {
            motionContext = "Moving";
        }

        return new ContextResult(stableContext, motionContext);
    }

    private static float mean(Queue<Float> values) {
        float sum = 0f;
        for (float v : values) sum += v;
        return sum / values.size();
    }

    private static float variance(Queue<Float> values, float mean) {
        float sumSq = 0f;
        for (float v : values) {
            float diff = v - mean;
            sumSq += diff * diff;
        }
        return sumSq / values.size();
    }
}