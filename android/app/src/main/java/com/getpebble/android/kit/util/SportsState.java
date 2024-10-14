package com.getpebble.android.kit.util;

import android.content.Context;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;

/**
 * This container class includes new values of time, distance, and pace or speed. To send the values
 * to the watch use `synchronize`. This class is mutable, so consumers can update the values and
 * synchronize the same instance again. Modifying the values of this class will not send the values.
 * The consumer needs to invoke `synchronize` every time the UI needs to be updated.
 */
public class SportsState {
    private int timeInSec = 0;
    private float distance = 0;
    private Integer paceInSec = null;
    private Double speed = null;
    private Byte heartBPM = null;
    private String customLabel = null;
    private String customValue = null;
    private SportsState previousState = null;

    /**
     * Creates a formatted time string from a total seconds value, formatted as
     * "h:mm:ss".
     * <p>
     * For example, supplying the value 3930.0f seconds will return @"1:05:30".
     *
     * @param totalSeconds The number of seconds from which to create the time string.
     * @return the formatted time as "h:mm:ss"
     */
    private static String convertSecondsToString(int totalSeconds) {
        if (totalSeconds < 0) {
            return null;
        }
        int hours = totalSeconds / 3600;
        int remainder = totalSeconds - (hours * 3600);
        int minutes = remainder / 60;
        int seconds = remainder - (minutes * 60);
        if (hours > 0) {
            return String.format("%2d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Creates a formatted decimal string with one decimal number.
     * <p>
     * For example, supplying the value 13.42f will return @"13.4".
     *
     * @param distance The decimal number to format as a string.
     * @return the formatted decimal number
     */
    private static String convertDistanceToString(float distance) {
        return String.format("%.1f", distance);
    }

    /**
     * The current time in seconds.
     *
     * @return the current activity duration in seconds
     */
    public int getTimeInSec() {
        return timeInSec;
    }

    /**
     * Set the current time in seconds.
     * <p>
     * The possible range is currently limited from 0 to 35999,
     * inclusive (9h 59min 59sec). Values larger or smaller than the limits will
     * be transformed into the maximum or minimum, respectively.
     * <p>
     * It will be presented as a duration string in the UI. Hours, minutes and
     * seconds will be separated by colons. The hours value will only appear if
     * the value is more than 1 hour.
     *
     * @param timeInSec The current time to set, in seconds.
     */
    public void setTimeInSec(int timeInSec) {
        this.timeInSec = Math.max(0, Math.min(timeInSec, 35999));
    }

    /**
     * The current distance in kilometers or miles.
     *
     * @return the current distance
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Set the current distance in kilometers or miles.
     * <p>
     * The possible range is currently limited from 0 to 99.9, inclusive. Values
     * larger or smaller than the limits will be transformed into the maximum or
     * minimum, respectively.
     * <p>
     * It will be presented as a decimal number in the UI. The decimal part will be
     * rounded to one digit.
     * <p>
     * The unit of distance is dependent on the current unit setting.
     *
     * @param distance The current distance to set, in kilometers or miles.
     */
    public void setDistance(float distance) {
        this.distance = Math.max(0, Math.min(distance, 99.9f));
    }

    /**
     * The current pace in seconds per kilometer or seconds per mile.
     *
     * @return the current pace
     */
    public int getPaceInSec() {
        if (this.paceInSec == null) {
            return 0;
        }
        return paceInSec;
    }

    /**
     * Set the current pace in seconds per kilometer or seconds per mile.
     * <p>
     * The possible range is currently limited from 0 to 3599,
     * inclusive (59min 59sec). Values larger or smaller than the limits will be
     * transformed into the maximum or minimum, respectively.
     * <p>
     * It will be presented as a duration string in the UI. Minutes and seconds will
     * be separated by colons.
     * <p>
     * Currently pace and speed cannot be presented at the same time. Setting speed
     * will discard the value set through pace.
     *
     * @param paceInSec The pace to set, in seconds per kilometer or seconds per mile.
     */
    public void setPaceInSec(int paceInSec) {
        this.speed = null;
        this.paceInSec = Math.max(0, Math.min(paceInSec, 3599));
    }

    /**
     * The current speed in kilometers per hour or miles per hour.
     *
     * @return the current speed
     */
    public float getSpeed() {
        if (this.speed == null) {
            return 0;
        }
        return speed.floatValue();
    }

    /**
     * Set the current speed in kilometers per hour or miles per hour.
     * <p>
     * The possible range is currently limited from 0 to 99.9, inclusive. Values
     * larger or smaller than the limits will be transformed into the maximum or
     * minimum, respectively.
     * <p>
     * It will be presented as a decimal number in the UI. The decimal part will be
     * rounded to one digit.
     * <p>
     * Currently pace and speed cannot be presented at the same time. Setting pace
     * will discard the value set through speed.
     *
     * @param speed The current speed to set, in kilometers per hour or miles per hour.
     */
    public void setSpeed(float speed) {
        this.paceInSec = null;
        this.speed = Math.max(0, Math.min(speed, 99.9));
    }

    /**
     * The current heart rate in beats per minute.
     * <p>
     * If the heart rate has never been set before, this property will return zero.
     *
     * @return the current heart rate
     */
    public byte getHeartBPM() {
        if (this.heartBPM == null) {
            return 0;
        }
        return heartBPM;
    }

    /**
     * Set the current heart rate in beats per minute.
     * <p>
     * Currently there is no way to stop sending heart rate values if one heart rate
     * value was sent. The last value will be shown in the UI.
     *
     * @param heartBPM The current heart rate to set, in beats per minute.
     */
    public void setHeartBPM(byte heartBPM) {
        this.heartBPM = heartBPM;
    }

    /**
     * The custom label to show in the sports UI.
     *
     * @return the custom label
     */
    public String getCustomLabel() {
        return customLabel;
    }

    /**
     * Set the custom label to show in the sports UI.
     * <p>
     * The maximum number of characters is ~10, but this maximum is not enforced.
     * The label will be sent in upper case to the watch.
     * <p>
     * To be sent, both customLabel and customValue have to be set to non-null values.
     *
     * @param customLabel The custom label to set.
     */
    public void setCustomLabel(String customLabel) {
        if (customLabel == null) {
            this.customLabel = "";
        } else {
            this.customLabel = customLabel.toUpperCase();
        }
    }

    /**
     * The custom value to show in the sports UI.
     *
     * @return the custom value
     */
    public String getCustomValue() {
        return customValue;
    }

    /**
     * Set the custom value to show in the sports UI.
     * <p>
     * The maximum number of characters is ~8, but the maximum is not enforced.
     * <p>
     * To be sent, both customValue and customLabel have to be set to non-null values.
     *
     * @param customValue The custom value to set.
     */
    public void setCustomValue(String customValue) {
        if (customValue == null) {
            this.customValue = "";
        } else {
            this.customValue = customValue;
        }
    }

    /**
     * Synchronizes the current state of the Sports App to the connected watch.
     * <p>
     * The method tries to send the minimal set of changes since the last time the
     * method was used, to try to minimize communication with the watch.
     *
     * @param context The context used to send the broadcast.
     */
    public void synchronize(final Context context) {
        SportsState previousState = this.previousState;
        boolean firstMessage = false;
        if (previousState == null) {
            previousState = this.previousState = new SportsState();
            firstMessage = true;
        }

        PebbleDictionary message = new PebbleDictionary();
        if (getTimeInSec() != previousState.getTimeInSec() || firstMessage) {
            previousState.setTimeInSec(getTimeInSec());
            message.addString(Constants.SPORTS_TIME_KEY, convertSecondsToString(getTimeInSec()));
        }
        if (getDistance() != previousState.getDistance() || firstMessage) {
            previousState.setDistance(getDistance());
            message.addString(Constants.SPORTS_DISTANCE_KEY, convertDistanceToString(getDistance()));
        }
        if (this.paceInSec != null) {
            message.addUint8(Constants.SPORTS_LABEL_KEY, (byte) Constants.SPORTS_DATA_PACE);
            if (getPaceInSec() != previousState.getPaceInSec()) {
                previousState.setPaceInSec(getPaceInSec());
                message.addString(Constants.SPORTS_DATA_KEY, convertSecondsToString(getPaceInSec()));
            }
        }
        if (this.speed != null) {
            message.addUint8(Constants.SPORTS_LABEL_KEY, (byte) Constants.SPORTS_DATA_SPEED);
            if (getSpeed() != previousState.getSpeed()) {
                previousState.setSpeed(getSpeed());
                message.addString(Constants.SPORTS_DATA_KEY, convertDistanceToString(getSpeed()));
            }
        }
        if (this.heartBPM != null) {
            if (getHeartBPM() != previousState.getHeartBPM()) {
                previousState.setHeartBPM(getHeartBPM());
                message.addUint8(Constants.SPORTS_HR_BPM_KEY, getHeartBPM());
            }
        }
        if (getCustomLabel() != null && getCustomValue() != null) {
            if (!getCustomLabel().equals(previousState.getCustomLabel())) {
                previousState.setCustomLabel(getCustomLabel());
                message.addString(Constants.SPORTS_CUSTOM_LABEL_KEY, getCustomLabel());
            }
            if (!getCustomValue().equals(previousState.getCustomValue())) {
                previousState.setCustomValue(getCustomValue());
                message.addString(Constants.SPORTS_CUSTOM_VALUE_KEY, getCustomValue());
            }
        }

        PebbleKit.sendDataToPebble(context, Constants.SPORTS_UUID, message);
    }
}
