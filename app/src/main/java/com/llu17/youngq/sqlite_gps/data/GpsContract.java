package com.llu17.youngq.sqlite_gps.data;

import android.provider.BaseColumns;

/**
 * Created by youngq on 17/3/2.
 */

public class GpsContract {
    public static final class GpsEntry implements BaseColumns{
        public static final String TABLE_NAME = "gps_location";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_BEARING = "bearing";
        public static final String COLUMN_SPEED = "speed";
        public static final String COLUMN_FLAG = "flag";    //mark the bus stop
    }
    public static final class AccelerometerEntry implements BaseColumns{
        public static final String TABLE_NAME = "accelerometer";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_X = "X";
        public static final String COLUMN_Y = "Y";
        public static final String COLUMN_Z = "Z";
    }
    public static final class GyroscopeEntry implements BaseColumns{
        public static final String TABLE_NAME = "gyroscope";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_X = "X";
        public static final String COLUMN_Y = "Y";
        public static final String COLUMN_Z = "Z";
    }
    public static final class MotionStateEntry implements BaseColumns{
        public static final String TABLE_NAME = "motionstate";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_STATE = "state";
    }
    public static final class StepEntry implements BaseColumns{
        public static final String TABLE_NAME = "step";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_COUNT = "Count";
    }
    public static final class BatteryEntry implements BaseColumns{
        public static final String TABLE_NAME = "battery";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_Percentage = "Percentage";
    }
    public static final class WiFiEntry implements BaseColumns{
        public static final String TABLE_NAME = "wifi";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_State = "State";
    }
    public static final class MagnetometerEntry implements BaseColumns{
        public static final String TABLE_NAME = "magnetometer";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_X = "X";
        public static final String COLUMN_Y = "Y";
        public static final String COLUMN_Z = "Z";
    }
    public static final class ParkingStateEntry implements BaseColumns{
        public static final String TABLE_NAME = "parkingstate";
        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_TAG = "Tag";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_STATE = "state";
    }
}
