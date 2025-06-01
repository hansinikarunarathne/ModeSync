package com.example.smartmodeswitcher.sensors;

public class ContextDetector {

    public static String detect(float[] accelValues, float lightValue, float proximityValue) {
        boolean isInPocket = proximityValue == 0 && lightValue < 5;
        boolean isOnDesk = (Math.abs(accelValues[0]) < 1 &&
                Math.abs(accelValues[1]) < 1 &&
                Math.abs(accelValues[2] - 9.8f) < 1);
        boolean isInHand = !isInPocket && lightValue > 20;

        if (isInPocket) {
            return "In Pocket";
        } else if (isOnDesk) {
            return "On Desk";
        } else if (isInHand) {
            return "In Hand";
        } else {
            return "Unknown";
        }
    }
}
