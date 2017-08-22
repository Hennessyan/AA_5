package com.llu17.youngq.sqlite_gps;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.llu17.youngq.sqlite_gps.data.GpsContract;
import com.llu17.youngq.sqlite_gps.data.GpsDbHelper;
import com.llu17.youngq.sqlite_gps.table.ACCELEROMETER;
import com.llu17.youngq.sqlite_gps.table.BATTERY;
import com.llu17.youngq.sqlite_gps.table.GPS;
import com.llu17.youngq.sqlite_gps.table.GYROSCOPE;
import com.llu17.youngq.sqlite_gps.table.MAGNETOMETER;
import com.llu17.youngq.sqlite_gps.table.MOTIONSTATE;
import com.llu17.youngq.sqlite_gps.table.PARKINGSTATE;
import com.llu17.youngq.sqlite_gps.table.STEP;
import com.llu17.youngq.sqlite_gps.table.WIFI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static com.llu17.youngq.sqlite_gps.UploadService.dbHelper1;
import static com.llu17.youngq.sqlite_gps.UploadService.flag;
import static com.llu17.youngq.sqlite_gps.UploadService.wifistate;

/**
 * Created by youngq on 17/5/1.
 */

public class VariableManager {

    private SQLiteDatabase db1, db2, db3, db4, db5, db6, db7, db8, db9;
    private JSONObject acce_object,gyro_object,gps_object,motion_object,step_object,battery_object,wifi_object,magne_object,park_object;
    private JSONArray AcceJsonArray,GyroJsonArray,GpsJsonArray,MotionJsonArray,StepJsonArray,BatteryJsonArray,WiFiJsonArray,MagneJsonArray,ParkJsonArray;
    private int sizeOfUpload = 100;

    public static ArrayList<GPS> gpses;
    public static ArrayList<ACCELEROMETER> acces;
    public static ArrayList<GYROSCOPE> gyros;
    public static ArrayList<MOTIONSTATE> motions;
    public static ArrayList<STEP> steps;
    public static ArrayList<BATTERY> batteries;
    public static ArrayList<WIFI> wifis;
    public static ArrayList<MAGNETOMETER> magnetometers;
    public static ArrayList<PARKINGSTATE> parkinglots;

    private static CountDownLatch latch = null;


    public interface Listener {
        public void onStateChange(boolean[] state);
    }

    private Listener mListener = null;
    public void registerListener (Listener listener) {
        mListener = listener;
    }
    public void unregisterListener (Listener listener) {
        mListener = null;
    }
    private boolean[] myBoolean = new boolean[9];
    public void doYourWork() {
        Log.e("There is WiFi","I am here!");
        final String gps_url = "http://cs.binghamton.edu/~smartpark/lulu_test/gps.php";
        final String acce_url = "http://cs.binghamton.edu/~smartpark/lulu_test/accelerometer.php";
        final String gyro_url = "http://cs.binghamton.edu/~smartpark/lulu_test/gyroscope.php";
        final String step_url = "http://cs.binghamton.edu/~smartpark/lulu_test/step.php";
        final String motion_url = "http://cs.binghamton.edu/~smartpark/lulu_test/motionstate.php";
        final String wifi_url = "http://cs.binghamton.edu/~smartpark/lulu_test/wifi.php";
        final String battery_url = "http://cs.binghamton.edu/~smartpark/lulu_test/battery.php";
        final String magne_url = "http://cs.binghamton.edu/~smartpark/lulu_test/magnetometer.php";
        final String park_url = "http://cs.binghamton.edu/~smartpark/lulu_test/parkingstate.php";
        final int[] result = new int[9];

        boolean label = true;

        while(label) {
            for(int i = 0; i < 9; i++) {
                myBoolean[i] = false;
                result[i] = 0;
            }

            if(wifistate[0] == 1){
                gpses = find_all_gps();
                acces = find_all_acce();
                gyros = find_all_gyro();
                motions = find_all_motion();
                steps = find_all_step();
                batteries = find_all_battery();
                wifis = find_all_wifi();
                magnetometers = find_all_magne();
                parkinglots = find_all_park();

                latch = new CountDownLatch(9);
                Thread t1 = new Thread() {
                    public void run() {
                        if(gpses != null)
                            result[0] = post_data(gps_url, changeGpsDateToJson());
                        latch.countDown();
                    }
                };
                t1.start();
                Thread t2 = new Thread() {
                    public void run() {
                        if(acces != null)
                            result[1] = post_data(acce_url, changeAcceDateToJson());
                        latch.countDown();
                    }
                };
                t2.start();
                Thread t3 = new Thread() {
                    public void run() {
                        if(gyros != null)
                            result[2] = post_data(gyro_url, changeGyroDateToJson());
                        latch.countDown();
                    }
                };
                t3.start();
                Thread t4 = new Thread() {
                    public void run() {
                        if(steps != null)
                            result[3] = post_data(step_url, changeStepDateToJson());
                        latch.countDown();
                    }
                };
                t4.start();
                Thread t5 = new Thread() {
                    public void run() {
                        if(motions != null)
                            result[4] = post_data(motion_url, changeMotionDateToJson());
                        latch.countDown();
                    }
                };
                t5.start();
                Thread t6 = new Thread() {
                    public void run() {
                        if(wifis != null)
                            result[5] = post_data(wifi_url, changeWiFiDateToJson());
                        latch.countDown();
                    }
                };
                t6.start();
                Thread t7 = new Thread() {
                    public void run() {
                        if(batteries != null)
                            result[6] = post_data(battery_url, changeBatteryDateToJson());
                        latch.countDown();
                    }
                };
                t7.start();
                Thread t8 = new Thread() {
                    public void run() {
                        if(magnetometers != null)
                            result[7] = post_data(magne_url, changeMagneDateToJson());
                        latch.countDown();
                    }
                };
                t8.start();
                Thread t9 = new Thread() {
                    public void run() {
                        if(parkinglots != null)
                            result[8] = post_data(park_url, changeParkingDateToJson());
                        latch.countDown();
                    }
                };
                t9.start();

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                Log.e("result[0]","!!!!!"+result[0]);
//                Log.e("result[1]","!!!!!"+result[1]);
//                Log.e("result[2]","!!!!!"+result[2]);
//                Log.e("result[3]","!!!!!"+result[3]);
//                Log.e("result[4]","!!!!!"+result[4]);
//                Log.e("result[5]","!!!!!"+result[5]);
//                Log.e("result[6]","!!!!!"+result[6]);
//                Log.e("result[7]","!!!!!"+result[7]);
                for(int i = 0; i < 9; i++) {
                    if (result[i] == 200)
                        myBoolean[i] = true;
                    else
                        myBoolean[i] = false;
                }
            }
            else{
                Log.e("no wifi, ", "no upload!!!!!!!!!!!!!!!!!!");
            }

            if (mListener != null)
                mListener.onStateChange(myBoolean);
            //数据少的时候用它来保证不会一下就把所有的数据upload到mysql
            //从而可以通过开关wifi来检查是否代码可以正常运行
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(flag == true)
                label = false;
        }
        Log.e("Upload Success","!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private ArrayList<GPS> find_all_gps(){
//        dbHelper = new GpsDbHelper(this);
        db1 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, latitude, longitude, bearing, speed , flag from gps_location where Tag = 0 limit " + sizeOfUpload + ";";
        try {
            c = db1.rawQuery(s, null);
            Log.e("cursor count gps: ", "" + c.getCount());
            if (c != null && c.getCount() > 0) {
                ArrayList<GPS> gpslist = new ArrayList<>();
                GPS gps;
                while (c.moveToNext()) {
                    gps = new GPS();
                    gps.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_ID)));
                    gps.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_TIMESTAMP)));
                    gps.setLatitude(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_LATITUDE)));
                    gps.setLongitude(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_LONGITUDE)));
                    gps.setBearing(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_BEARING)));
                    gps.setSpeed(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_SPEED)));
                    gps.setFlag(c.getInt(c.getColumnIndexOrThrow(GpsContract.GpsEntry.COLUMN_FLAG)));
                    gpslist.add(gps);
                }
                return gpslist;
            } else {
                Log.e("i am here", "hello11111111");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally {
            c.close();
            db1.close();
            Log.e("i am here2", "hello11111111");
        }
        Log.e("i am here3", "hello11111111");
        return null;
    }
    private ArrayList<ACCELEROMETER> find_all_acce(){
//        dbHelper = new GpsDbHelper(this);
        db2 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, X, Y, Z from accelerometer where Tag = 0 limit " + sizeOfUpload + ";";
        try {
            c = db2.rawQuery(s, null);
            Log.e("cursor count acce: ", "" + c.getCount());
            if (c != null && c.getCount() > 0) {
                ArrayList<ACCELEROMETER> accelist = new ArrayList<>();
                ACCELEROMETER acce;
                while (c.moveToNext()) {
                    acce = new ACCELEROMETER();
                    acce.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_ID)));
                    acce.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_TIMESTAMP)));
                    acce.setX(c.getDouble(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_X)));
                    acce.setY(c.getDouble(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_Y)));
                    acce.setZ(c.getDouble(c.getColumnIndexOrThrow(GpsContract.AccelerometerEntry.COLUMN_Z)));
                    accelist.add(acce);
                }
                return accelist;
            } else {
                Log.e("i am here", "hello2222222222");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally{
            c.close();
            db2.close();
            Log.e("i am here2", "hello2222222222");
        }
        Log.e("i am here3", "hello2222222222");
        return null;
    }
    private ArrayList<GYROSCOPE> find_all_gyro(){
//        dbHelper = new GpsDbHelper(this);
        db3 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, X, Y, Z from gyroscope where Tag = 0 limit " + sizeOfUpload + ";";
        try {
            c = db3.rawQuery(s, null);
            if (c != null && c.getCount() > 0) {
                ArrayList<GYROSCOPE> gyrolist = new ArrayList<>();
                GYROSCOPE gyro;
                while (c.moveToNext()) {
                    gyro = new GYROSCOPE();
                    gyro.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_ID)));
                    gyro.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_TIMESTAMP)));
                    gyro.setX(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_X)));
                    gyro.setY(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_Y)));
                    gyro.setZ(c.getDouble(c.getColumnIndexOrThrow(GpsContract.GyroscopeEntry.COLUMN_Z)));
                    gyrolist.add(gyro);
                }
                return gyrolist;
            } else {
                Log.e("i am here", "hello333333333");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally {
            c.close();
            db3.close();
        }
        return null;
    }
    private ArrayList<MOTIONSTATE> find_all_motion(){
//        dbHelper = new GpsDbHelper(this);
        db4 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, state from motionstate where Tag = 0 limit " + sizeOfUpload + ";";
        try {
            c = db4.rawQuery(s, null);
            if (c != null && c.getCount() > 0) {
                ArrayList<MOTIONSTATE> motionlist = new ArrayList<>();
                MOTIONSTATE motion;
                while (c.moveToNext()) {
                    motion = new MOTIONSTATE();
                    motion.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.MotionStateEntry.COLUMN_ID)));
                    motion.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.MotionStateEntry.COLUMN_TIMESTAMP)));
                    motion.setState(c.getInt(c.getColumnIndexOrThrow(GpsContract.MotionStateEntry.COLUMN_STATE)));
                    motionlist.add(motion);
                }
                return motionlist;
            } else {
                Log.e("i am here", "hello44444444");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally {
            c.close();
            db4.close();
        }
        return null;
    }
    private ArrayList<STEP> find_all_step(){
//        dbHelper = new GpsDbHelper(this);
        db5 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, Count from step where Tag = 0 limit " + sizeOfUpload + ";";
        try {
            c = db5.rawQuery(s, null);
            if (c != null && c.getCount() > 0) {
                ArrayList<STEP> steplist = new ArrayList<>();
                STEP step;
                while (c.moveToNext()) {
                    step = new STEP();
                    step.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.StepEntry.COLUMN_ID)));
                    step.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.StepEntry.COLUMN_TIMESTAMP)));
                    step.setCount(c.getInt(c.getColumnIndexOrThrow(GpsContract.StepEntry.COLUMN_COUNT)));
                    steplist.add(step);
                }
                return steplist;
            } else {
                Log.e("i am here", "hello55555555");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally{
            c.close();
            db5.close();
        }
        return null;
    }
    private ArrayList<BATTERY> find_all_battery(){
//        dbHelper = new GpsDbHelper(this);
        db6 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, Percentage from battery where Tag = 0 limit " + sizeOfUpload + ";";
        try {
            c = db6.rawQuery(s, null);
            if (c != null && c.getCount() > 0) {
                ArrayList<BATTERY> batterylist = new ArrayList<>();
                BATTERY battery;
                while (c.moveToNext()) {
                    battery = new BATTERY();
                    battery.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.BatteryEntry.COLUMN_ID)));
                    battery.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.BatteryEntry.COLUMN_TIMESTAMP)));
                    battery.setPercentage(c.getInt(c.getColumnIndexOrThrow(GpsContract.BatteryEntry.COLUMN_Percentage)));
                    batterylist.add(battery);
                }
                return batterylist;
            } else {
                Log.e("i am here", "hello66666666");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally {
            c.close();
            db6.close();
        }
        return null;
    }
    private ArrayList<WIFI> find_all_wifi(){
//        dbHelper = new GpsDbHelper(this);
        db7 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, State from wifi where Tag = 0 limit " + sizeOfUpload + ";";
        try {
            c = db7.rawQuery(s, null);
            if (c != null && c.getCount() > 0) {
                ArrayList<WIFI> wifilist = new ArrayList<>();
                WIFI wifi;
                while (c.moveToNext()) {
                    wifi = new WIFI();
                    wifi.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.WiFiEntry.COLUMN_ID)));
                    wifi.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.WiFiEntry.COLUMN_TIMESTAMP)));
                    wifi.setState(c.getInt(c.getColumnIndexOrThrow(GpsContract.WiFiEntry.COLUMN_State)));
                    wifilist.add(wifi);
                }
                return wifilist;
            } else {
                Log.e("i am here", "hello77777777");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally {
            c.close();
            db7.close();
        }
        return null;
    }
    private ArrayList<MAGNETOMETER> find_all_magne(){
//        dbHelper = new GpsDbHelper(this);
        db8 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, X, Y, Z from magnetometer where Tag = 0 limit " + sizeOfUpload + ";";
        try {
            c = db8.rawQuery(s, null);
            Log.e("cursor count magne: ", "" + c.getCount());
            if (c != null && c.getCount() > 0) {
                ArrayList<MAGNETOMETER> magnelist = new ArrayList<>();
                MAGNETOMETER magne;
                while (c.moveToNext()) {
                    magne = new MAGNETOMETER();
                    magne.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.MagnetometerEntry.COLUMN_ID)));
                    magne.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.MagnetometerEntry.COLUMN_TIMESTAMP)));
                    magne.setX(c.getDouble(c.getColumnIndexOrThrow(GpsContract.MagnetometerEntry.COLUMN_X)));
                    magne.setY(c.getDouble(c.getColumnIndexOrThrow(GpsContract.MagnetometerEntry.COLUMN_Y)));
                    magne.setZ(c.getDouble(c.getColumnIndexOrThrow(GpsContract.MagnetometerEntry.COLUMN_Z)));
                    magnelist.add(magne);
                }
                return magnelist;
            } else {
                Log.e("i am here", "hello88888888");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally{
            c.close();
            db8.close();
            Log.e("i am here2", "hello88888888");
        }
        Log.e("i am here3", "hello88888888");
        return null;
    }
    private ArrayList<PARKINGSTATE> find_all_park(){
//        dbHelper = new GpsDbHelper(this);
        db9 = dbHelper1.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, state from parkingstate where Tag = 0 limit 200;";
        try {
            c = db9.rawQuery(s, null);
            if (c != null && c.getCount() > 0) {
                ArrayList<PARKINGSTATE> parklist = new ArrayList<>();
                PARKINGSTATE park;
                while (c.moveToNext()) {
                    park = new PARKINGSTATE();
                    park.setId(c.getString(c.getColumnIndexOrThrow(GpsContract.ParkingStateEntry.COLUMN_ID)));
                    park.setTimestamp(c.getLong(c.getColumnIndexOrThrow(GpsContract.ParkingStateEntry.COLUMN_TIMESTAMP)));
                    park.setState(c.getInt(c.getColumnIndexOrThrow(GpsContract.ParkingStateEntry.COLUMN_STATE)));
                    parklist.add(park);
                }
                return parklist;
            } else {
                Log.e("i am here", "hello99999999");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally {
            c.close();
            db9.close();
        }
        return null;
    }

    private JSONArray changeGpsDateToJson() {
        if(gpses.size() == sizeOfUpload) {
            GpsJsonArray = null;
            GpsJsonArray = new JSONArray();
            for (int i = 0; i < gpses.size(); i++) {
                gps_object = new JSONObject();
                try {
                    gps_object.put("UserID", gpses.get(i).getId());
                    gps_object.put("Timestamp", gpses.get(i).getTimestamp());
                    gps_object.put("Latitude", gpses.get(i).getLatitude());
                    gps_object.put("Longitude", gpses.get(i).getLongitude());
                    gps_object.put("Bearing", gpses.get(i).getBearing());
                    gps_object.put("Speed", gpses.get(i).getSpeed());
                    gps_object.put("Flag", gpses.get(i).getFlag());
                    GpsJsonArray.put(gps_object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return GpsJsonArray;
        }
        else
            return null;
    }
    private JSONArray changeAcceDateToJson() {
        if(acces.size() == sizeOfUpload) {
            AcceJsonArray = null;
            AcceJsonArray = new JSONArray();
            for (int i = 0; i < acces.size(); i++) {
                acce_object = new JSONObject();
                try {
                    acce_object.put("UserID", acces.get(i).getId());
                    acce_object.put("Timestamp", acces.get(i).getTimestamp());
                    acce_object.put("X", acces.get(i).getX());
                    acce_object.put("Y", acces.get(i).getY());
                    acce_object.put("Z", acces.get(i).getZ());
                    AcceJsonArray.put(acce_object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return AcceJsonArray;
        }
        else
            return null;
    }
    private JSONArray changeGyroDateToJson() {
        if(gyros.size() == sizeOfUpload) {
            GyroJsonArray = null;
            GyroJsonArray = new JSONArray();
            for (int i = 0; i < gyros.size(); i++) {
                gyro_object = new JSONObject();
                try {
                    gyro_object.put("UserID", gyros.get(i).getId());
                    gyro_object.put("Timestamp", gyros.get(i).getTimestamp());
                    gyro_object.put("X", gyros.get(i).getX());
                    gyro_object.put("Y", gyros.get(i).getY());
                    gyro_object.put("Z", gyros.get(i).getZ());
                    GyroJsonArray.put(gyro_object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return GyroJsonArray;
        }
        else
            return null;
    }
    private JSONArray changeMotionDateToJson() {
        if(motions.size() == sizeOfUpload) {
            MotionJsonArray = null;
            MotionJsonArray = new JSONArray();
            for (int i = 0; i < motions.size(); i++) {
                motion_object = new JSONObject();
                try {
                    motion_object.put("UserID", motions.get(i).getId());
                    motion_object.put("Timestamp", motions.get(i).getTimestamp());
                    motion_object.put("State", motions.get(i).getState());
                    MotionJsonArray.put(motion_object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return MotionJsonArray;
        }
        else
            return null;
    }
    private JSONArray changeStepDateToJson() {
        if(steps.size() == sizeOfUpload) {
            StepJsonArray = null;
            StepJsonArray = new JSONArray();
            for (int i = 0; i < steps.size(); i++) {
                step_object = new JSONObject();
                try {
                    step_object.put("UserID", steps.get(i).getId());
                    step_object.put("Timestamp", steps.get(i).getTimestamp());
                    step_object.put("Count", steps.get(i).getCount());
                    StepJsonArray.put(step_object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return StepJsonArray;
        }
        else
            return null;
    }
    private JSONArray changeBatteryDateToJson() {
        if(batteries.size() == sizeOfUpload) {
            BatteryJsonArray = null;
            BatteryJsonArray = new JSONArray();
            for (int i = 0; i < batteries.size(); i++) {
                battery_object = new JSONObject();
                try {
                    battery_object.put("UserID", batteries.get(i).getId());
                    battery_object.put("Timestamp", batteries.get(i).getTimestamp());
                    battery_object.put("Percentage", batteries.get(i).getPercentage());
                    BatteryJsonArray.put(battery_object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return BatteryJsonArray;
        }
        else
            return null;
    }
    private JSONArray changeWiFiDateToJson() {
        if(wifis.size() == sizeOfUpload) {
            WiFiJsonArray = null;
            WiFiJsonArray = new JSONArray();
            for (int i = 0; i < wifis.size(); i++) {
                wifi_object = new JSONObject();
                try {
                    wifi_object.put("UserID", wifis.get(i).getId());
                    wifi_object.put("Timestamp", wifis.get(i).getTimestamp());
                    wifi_object.put("State", wifis.get(i).getState());
                    WiFiJsonArray.put(wifi_object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return WiFiJsonArray;
        }
        else
            return null;
    }
    private JSONArray changeMagneDateToJson() {
        if(magnetometers.size() == sizeOfUpload) {
            MagneJsonArray = null;
            MagneJsonArray = new JSONArray();
            for (int i = 0; i < magnetometers.size(); i++) {
                magne_object = new JSONObject();
                try {
                    magne_object.put("UserID", magnetometers.get(i).getId());
                    magne_object.put("Timestamp", magnetometers.get(i).getTimestamp());
                    magne_object.put("X", magnetometers.get(i).getX());
                    magne_object.put("Y", magnetometers.get(i).getY());
                    magne_object.put("Z", magnetometers.get(i).getZ());
                    MagneJsonArray.put(magne_object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return MagneJsonArray;
        }
        else
            return null;
    }
    private JSONArray changeParkingDateToJson() {
        ParkJsonArray=null;
        ParkJsonArray = new JSONArray();
        for (int i = 0; i < parkinglots.size(); i++) {
            park_object = new JSONObject();
            try {
                park_object.put("UserID", parkinglots.get(i).getId());
                park_object.put("Timestamp", parkinglots.get(i).getTimestamp());
                park_object.put("State", parkinglots.get(i).getState());
                ParkJsonArray.put(park_object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ParkJsonArray;
    }

    private int post_data(String url, JSONArray json){
        if(json != null) {
            int StatusCode = 0;
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext httpContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);

            try {

                StringEntity se = new StringEntity(json.toString());

                httpPost.setEntity(se);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");


                HttpResponse response = httpClient.execute(httpPost, httpContext); //execute your request and parse response
                HttpEntity entity = response.getEntity();

                String jsonString = EntityUtils.toString(entity); //if response in JSON format
                Log.e("response: ", jsonString);

                StatusCode = response.getStatusLine().getStatusCode();
                Log.e("status code: ", "" + StatusCode);
            } catch (Exception e) {
//            e.printStackTrace();
                Log.e("no wifi exception: ", e.toString());
            }
            return StatusCode;
        }
        else
            return 0;
    }
}
