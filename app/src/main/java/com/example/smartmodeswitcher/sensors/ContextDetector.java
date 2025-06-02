package com.example.smartmodeswitcher.sensors;

import java.util.LinkedList;
import java.util.Queue;

public class ContextDetector {


    private static final int WINDOW_SIZE = 40; // 5 seconds at 200ms/sample
    private static final float VEHICLE_MIN = 10.5f;
    private static final float VEHICLE_MAX = 13.0f;
    private static final float WALK_MIN = 10.5f;
    private static final float WALK_MAX = 17.0f;
    private static final float RUN_MIN = 17.0f;
    private static String lastContext = "Unknown";
    private static int stableCount = 0;
    private static final int STABLE_THRESHOLD = 5; // Require 5 consecutive detections

    private static final float SAMPLE_INTERVAL_MS = 200f;

    private static final Queue<Float> magnitudeWindow = new LinkedList<>();


    public static String detect(float[] accelValues, float lightValue, float proximityValue) {
        float ax = accelValues[0];
        float ay = accelValues[1];
        float az = accelValues[2];
        float magnitude = (float) Math.sqrt(ax * ax + ay * ay + az * az);

        // Maintain sliding window
        if (magnitudeWindow.size() >= WINDOW_SIZE) {
            magnitudeWindow.poll();
        }
        magnitudeWindow.offer(magnitude);

        // Count how many values are in vehicle range
        int inVehicleCount = 0, walkingCount = 0, runningCount = 0;
        for (float m : magnitudeWindow) {
            if (m > VEHICLE_MIN && m < VEHICLE_MAX) inVehicleCount++;
            if (m > WALK_MIN && m < WALK_MAX) walkingCount++;
            if (m > RUN_MIN) runningCount++;
        }

        boolean isRunning = magnitudeWindow.size() == WINDOW_SIZE && runningCount > WINDOW_SIZE * 0.6;
        boolean isWalking = magnitudeWindow.size() == WINDOW_SIZE && walkingCount > WINDOW_SIZE * 0.6;
        boolean isInVehicle = magnitudeWindow.size() == WINDOW_SIZE && inVehicleCount > WINDOW_SIZE * 0.6;
        boolean isInPocket = proximityValue == 0 && lightValue < 20;
        boolean isOnDesk = (Math.abs(ax) < 1 && Math.abs(ay) < 1 && Math.abs(az - 9.8f) < 1);
        boolean isInHand = !isInPocket && lightValue > 20;

        String currentContext;
        if (isRunning) {
            currentContext = "Running";
        } else if (isWalking) {
            currentContext = "Walking";
        } else if (isInVehicle) {
            currentContext = "In Vehicle";
        } else if (isInPocket) {
            currentContext = "In Pocket";
        } else if (isOnDesk) {
            currentContext = "On Desk";
        } else if (isInHand) {
            currentContext = "In Hand";
        } else {
            currentContext = "Unknown";
        }

        if (currentContext.equals(lastContext)) {
            stableCount++;
        } else {
            stableCount = 1;
            lastContext = currentContext;
        }

        if (stableCount >= STABLE_THRESHOLD) {
            return currentContext;
        } else {
            return lastContext;
        }
    }
}
