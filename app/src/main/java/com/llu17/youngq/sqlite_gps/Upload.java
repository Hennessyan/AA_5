package com.llu17.youngq.sqlite_gps;

import android.content.ContentValues;
import android.database.SQLException;
import android.util.Log;

import com.llu17.youngq.sqlite_gps.data.GpsContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import static com.llu17.youngq.sqlite_gps.CollectorService.id;
import static com.llu17.youngq.sqlite_gps.CollectorService.mDb;

/**
 * Created by youngq on 17/2/9.
 */

public class Upload extends TimerTask {
    //将upload method放在start中进行更新记录时,传递数据要使用数组,不能使用普通变量去传或者接收
    //否则获得的值始终为默认的初始值0

    private double[] gps_last = new double[]{0.0, 0.0};
    private long dupCount = 0;  //record number of duplication

    private int count = 0;
    private double[] nums1,nums2,nums3,nums4;
    private int[] nums5;
    private double[] nums6, nums7;
    private boolean[] nums8;    //mark bus stop
    private int step = 0;
    private int tag = 0;
    private int mark = 0;       //mark bus stop

    public Upload(double[] array1, double[] array2, double[] array3, double[] array4, int[] array5, double[] array6, double[] array7, boolean[] array8){
        nums1 = array1;
        nums2 = array2;
        nums3 = array3;
        nums4 = array4;
        nums5 = array5;
        nums6 = array6;
        nums7 = array7;
        nums8 = array8;
    }


    @Override
    public void run() {
        mark = 0;
        long temp_time = System.currentTimeMillis();
//        if(checkTimeInRange(temp_time)) {
//            if (gps_last[0] == nums4[0] && gps_last[1] == nums4[1]) {
//                dupCount++;
//                Log.e("upload counting : ", "" + dupCount);
//            } else {
//                gps_last[0] = nums4[0];
//                gps_last[1] = nums4[1];
//                dupCount = 0;
//            }
//        }
//        else{
//            dupCount = 0;
//        }
//
//        if(dupCount < 60) {
            count++;
//            long temp_time = System.currentTimeMillis();
            if(nums8[0]){
                mark = 1;
                nums8[0] = false;
            }
            Log.e("timestamp: " + temp_time, " mark: " + mark);
            ContentValues cv_acce = new ContentValues();
            cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_ID, id);
            cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_TAG, tag);
            cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_TIMESTAMP, temp_time);
            cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_X, nums1[0]);
            cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_Y, nums1[1]);
            cv_acce.put(GpsContract.AccelerometerEntry.COLUMN_Z, nums1[2]);

            ContentValues cv_gyro = new ContentValues();
            cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_ID, id);
            cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_TAG, tag);
            cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_TIMESTAMP, temp_time);
            cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_X, nums2[0]);
            cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_Y, nums2[1]);
            cv_gyro.put(GpsContract.GyroscopeEntry.COLUMN_Z, nums2[2]);

            step = (int) nums3[1];
            ContentValues cv_step = new ContentValues();
            cv_step.put(GpsContract.StepEntry.COLUMN_ID, id);
            cv_step.put(GpsContract.StepEntry.COLUMN_TAG, tag);
            cv_step.put(GpsContract.StepEntry.COLUMN_TIMESTAMP, temp_time);
            cv_step.put(GpsContract.StepEntry.COLUMN_COUNT, step);

            ContentValues cv_gps = new ContentValues();
            cv_gps.put(GpsContract.GpsEntry.COLUMN_ID, id);
            cv_gps.put(GpsContract.GpsEntry.COLUMN_TAG, tag);
            cv_gps.put(GpsContract.GpsEntry.COLUMN_TIMESTAMP, temp_time);
            cv_gps.put(GpsContract.GpsEntry.COLUMN_LATITUDE, nums4[0]);
            cv_gps.put(GpsContract.GpsEntry.COLUMN_LONGITUDE, nums4[1]);
            cv_gps.put(GpsContract.GpsEntry.COLUMN_BEARING, nums7[0]);
            cv_gps.put(GpsContract.GpsEntry.COLUMN_SPEED, nums7[1]);
            cv_gps.put(GpsContract.GpsEntry.COLUMN_FLAG, mark);

            ContentValues cv_motion = new ContentValues();
            cv_motion.put(GpsContract.MotionStateEntry.COLUMN_ID, id);
            cv_motion.put(GpsContract.MotionStateEntry.COLUMN_TAG, tag);
            cv_motion.put(GpsContract.MotionStateEntry.COLUMN_TIMESTAMP, temp_time);
            cv_motion.put(GpsContract.MotionStateEntry.COLUMN_STATE, nums5[0]);

            ContentValues cv_mage = new ContentValues();
            cv_mage.put(GpsContract.MagnetometerEntry.COLUMN_ID, id);
            cv_mage.put(GpsContract.MagnetometerEntry.COLUMN_TAG, tag);
            cv_mage.put(GpsContract.MagnetometerEntry.COLUMN_TIMESTAMP, temp_time);
            cv_mage.put(GpsContract.MagnetometerEntry.COLUMN_X, nums6[0]);
            cv_mage.put(GpsContract.MagnetometerEntry.COLUMN_Y, nums6[1]);
            cv_mage.put(GpsContract.MagnetometerEntry.COLUMN_Z, nums6[2]);
            try {
                mDb.beginTransaction();
                mDb.insert(GpsContract.AccelerometerEntry.TABLE_NAME, null, cv_acce);
                mDb.insert(GpsContract.GyroscopeEntry.TABLE_NAME, null, cv_gyro);
                mDb.insert(GpsContract.StepEntry.TABLE_NAME, null, cv_step);
                mDb.insert(GpsContract.GpsEntry.TABLE_NAME, null, cv_gps);
                mDb.insert(GpsContract.MotionStateEntry.TABLE_NAME, null, cv_motion);
                mDb.insert(GpsContract.MagnetometerEntry.TABLE_NAME, null, cv_mage);
                mDb.setTransactionSuccessful();
                Log.e("===insert===", "success!" + count);
            } catch (SQLException e) {
                //too bad :(
            } finally {
                mDb.endTransaction();
            }
        }
//    }

    private boolean checkTimeInRange(long time){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date cur1 = new Date(time);
        String str = formatter.format(cur1);

        Date cur = null;
        Date daystart = null, dayend = null;    //used to test
        Date nightstart1 = null, nightend1 = null;
        Date nightstart2 = null, nightend2 = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        try {
            cur = sdf.parse(str);
            daystart = sdf.parse("21:40:00");
            dayend = sdf.parse("21:45:00");

            nightstart1 = sdf.parse("23:00:00");
            nightend1 = sdf.parse("23:59:59");
            nightstart2 = sdf.parse("00:00:00");
            nightend2 = sdf.parse("06:30:00");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if((cur.equals(daystart) || cur.after(daystart)) && (cur.equals(dayend) || cur.before(dayend))){
            Log.e("test success: ", "!!!!!!!");

        }else if(((cur.equals(nightstart1) || cur.after(nightstart1)) && (cur.equals(nightend1) || cur.before(nightend1)))
                ||((cur.equals(nightstart2) || cur.after(nightstart2)) && (cur.equals(nightend2) || cur.before(nightend2)))){

            return true;
        }
        return false;
    }
    
}

