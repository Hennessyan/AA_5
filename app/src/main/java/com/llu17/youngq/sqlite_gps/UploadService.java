package com.llu17.youngq.sqlite_gps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.llu17.youngq.sqlite_gps.data.GpsContract;
import com.llu17.youngq.sqlite_gps.data.GpsDbHelper;
import com.llu17.youngq.sqlite_gps.table.ACCELEROMETER;
import com.llu17.youngq.sqlite_gps.table.BATTERY;
import com.llu17.youngq.sqlite_gps.table.GPS;
import com.llu17.youngq.sqlite_gps.table.GYROSCOPE;
import com.llu17.youngq.sqlite_gps.table.MOTIONSTATE;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

import static android.app.job.JobInfo.getMinPeriodMillis;
import static com.llu17.youngq.sqlite_gps.Util.session;
import static com.llu17.youngq.sqlite_gps.VariableManager.acces;
import static com.llu17.youngq.sqlite_gps.VariableManager.batteries;
import static com.llu17.youngq.sqlite_gps.VariableManager.gpses;
import static com.llu17.youngq.sqlite_gps.VariableManager.gyros;
import static com.llu17.youngq.sqlite_gps.VariableManager.magnetometers;
import static com.llu17.youngq.sqlite_gps.VariableManager.motions;
import static com.llu17.youngq.sqlite_gps.VariableManager.steps;
import static com.llu17.youngq.sqlite_gps.VariableManager.wifis;


/**
 * Created by youngq on 17/3/28.
 */

public class UploadService extends Service implements VariableManager.Listener{

    private PowerManager.WakeLock wakeLock = null;

    private GpsDbHelper dbHelper;
    private SQLiteDatabase db1;

    public static boolean flag;
    public static  GpsDbHelper dbHelper1;
    private Timer timer;
    VariableManager mListener;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        acquireWakeLock();
        registerReceiver(this.mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Log.e("wifistate: ", ""+wifistate[0]);

        mListener = new VariableManager();
        mListener.registerListener(this);
        dbHelper = new GpsDbHelper(this);
        flag = false;
        dbHelper1 = new GpsDbHelper(getApplicationContext());

        timer = new Timer();
        timer.schedule(new Loop(mListener), 0);

        Log.e("service","start");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        unregisterReceiver(mConnReceiver);
        mListener.unregisterListener(this);
        flag = true;
        timer.cancel();
        Log.e("service","destroy");
    }

    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }
    //释放设备电源锁
    private void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private static CountDownLatch latch = null;

    /*===WiFi State===*/
//    NetworkInfo wifiCheck;
    public static int[] wifistate = new int[1];
//    private int[] result = new int[7];
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkInfo currentNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            if(currentNetworkInfo.isConnected()) {
                wifistate[0] = 1;
                Log.e("WiFi is Connected", "!!!!!" + wifistate[0]);
            }

            if(!currentNetworkInfo.isConnected()){
                wifistate[0] = 0;
                Log.e("WiFi is not Connected","!!!!!"+wifistate[0]);
            }
        }
    };

    @Override
    public void onStateChange(boolean[] state) {
        if (state[0]) {
            latch = new CountDownLatch(1);
            Thread t1 = new Thread() {
                public void run() {
                    db1 = dbHelper.getWritableDatabase();
                    try {
                        if (db1 != null) {
                            //db1.execSQL("update gps_location set Tag = 1 where timestamp between ? and ?", new Object[]{gpses.get(0).getTimestamp(), gpses.get(gpses.size() - 1).getTimestamp()});
                            db1.execSQL("delete from gps_location where timestamp between ? and ?", new Object[]{gpses.get(0).getTimestamp(), gpses.get(gpses.size() - 1).getTimestamp()});
                        } else {
                            Log.e("db1~~~~~~", "null");
                        }
                    }
                    catch(Exception e){
                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                        Log.e("exception: ", e.getMessage());
                    }
                    finally {
                        db1.close();
                    }
                    latch.countDown();
                }
            };
            t1.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("lalala", "********");
        } else {
            Log.e("hahahahahahahahahahahha","2222222222");
        }
        if (state[1]) {
            latch = new CountDownLatch(1);
            Thread t1 = new Thread() {
                public void run() {
                    db1 = dbHelper.getWritableDatabase();
                    try {
                        if (db1 != null) {
                            //db1.execSQL("update accelerometer set Tag = 1 where timestamp between ? and ?", new Object[]{acces.get(0).getTimestamp(), acces.get(acces.size() - 1).getTimestamp()});
                            db1.execSQL("delete from accelerometer where timestamp between ? and ?", new Object[]{acces.get(0).getTimestamp(), acces.get(acces.size() - 1).getTimestamp()});
                        } else {
                            Log.e("db1~~~~~~", "null");
                        }
                    }
                    catch(Exception e){
                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                        Log.e("exception: ", e.getMessage());
                    }
                    finally {
                        db1.close();
                    }
                    latch.countDown();
                }
            };
            t1.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("lalala", "********");
        } else {
            Log.e("hahahahahahahahahahahha","2222222222");
        }
        if (state[2]) {
            latch = new CountDownLatch(1);
            Thread t1 = new Thread() {
                public void run() {
                    db1 = dbHelper.getWritableDatabase();
                    try {
                        if (db1 != null) {
                            //db1.execSQL("update gyroscope set Tag = 1 where timestamp between ? and ?", new Object[]{gyros.get(0).getTimestamp(), gyros.get(gyros.size() - 1).getTimestamp()});
                            db1.execSQL("delete from gyroscope where timestamp between ? and ?", new Object[]{gyros.get(0).getTimestamp(), gyros.get(gyros.size() - 1).getTimestamp()});
                        } else {
                            Log.e("db1~~~~~~", "null");
                        }
                    }
                    catch(Exception e){
                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                        Log.e("exception: ", e.getMessage());
                    }
                    finally {
                        db1.close();
                    }
                    latch.countDown();
                }
            };
            t1.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("lalala", "********");
        } else {
            Log.e("hahahahahahahahahahahha","2222222222");
        }
        if (state[3]) {
            latch = new CountDownLatch(1);
            Thread t1 = new Thread() {
                public void run() {
                    db1 = dbHelper.getWritableDatabase();
                    try {
                        if (db1 != null) {
                            //db1.execSQL("update step set Tag = 1 where timestamp between ? and ?", new Object[]{steps.get(0).getTimestamp(), steps.get(steps.size() - 1).getTimestamp()});
                            db1.execSQL("delete from step where timestamp between ? and ?", new Object[]{steps.get(0).getTimestamp(), steps.get(steps.size() - 1).getTimestamp()});
                        } else {
                            Log.e("db1~~~~~~", "null");
                        }
                    }
                    catch(Exception e){
                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                        Log.e("exception: ", e.getMessage());
                    }
                    finally {
                        db1.close();
                    }
                    latch.countDown();
                }
            };
            t1.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("lalala", "********");
        } else {
            Log.e("hahahahahahahahahahahha","2222222222");
        }
        if (state[4]) {
            latch = new CountDownLatch(1);
            Thread t1 = new Thread() {
                public void run() {
                    db1 = dbHelper.getWritableDatabase();
                    try {
                        if (db1 != null) {
                            //db1.execSQL("update motionstate set Tag = 1 where timestamp between ? and ?", new Object[]{motions.get(0).getTimestamp(), motions.get(motions.size() - 1).getTimestamp()});
                            db1.execSQL("delete from motionstate where timestamp between ? and ?", new Object[]{motions.get(0).getTimestamp(), motions.get(motions.size() - 1).getTimestamp()});
                        } else {
                            Log.e("db1~~~~~~", "null");
                        }
                    }
                    catch(Exception e){
                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                        Log.e("exception: ", e.getMessage());
                    }
                    finally {
                        db1.close();
                    }
                    latch.countDown();
                }
            };
            t1.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("lalala", "********");
        } else {
            Log.e("hahahahahahahahahahahha","2222222222");
        }
        if (state[5]) {
            latch = new CountDownLatch(1);
            Thread t1 = new Thread() {
                public void run() {
                    db1 = dbHelper.getWritableDatabase();
                    try {
                        if (db1 != null) {
                            //db1.execSQL("update wifi set Tag = 1 where timestamp between ? and ?", new Object[]{wifis.get(0).getTimestamp(), wifis.get(wifis.size() - 1).getTimestamp()});
                            db1.execSQL("delete from wifi where timestamp between ? and ?", new Object[]{wifis.get(0).getTimestamp(), wifis.get(wifis.size() - 1).getTimestamp()});
                        } else {
                            Log.e("db1~~~~~~", "null");
                        }
                    }
                    catch(Exception e){
                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                        Log.e("exception: ", e.getMessage());
                    }
                    finally {
                        db1.close();
                    }
                    latch.countDown();
                }
            };
            t1.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("lalala", "********");
        } else {
            Log.e("hahahahahahahahahahahha","2222222222");
        }
        if (state[6]) {
            latch = new CountDownLatch(1);
            Thread t1 = new Thread() {
                public void run() {
                    db1 = dbHelper.getWritableDatabase();
                    try {
                        if (db1 != null) {
                            //db1.execSQL("update battery set Tag = 1 where timestamp between ? and ?", new Object[]{batteries.get(0).getTimestamp(), batteries.get(batteries.size() - 1).getTimestamp()});
                            db1.execSQL("delete from battery where timestamp between ? and ?", new Object[]{batteries.get(0).getTimestamp(), batteries.get(batteries.size() - 1).getTimestamp()});
                        } else {
                            Log.e("db1~~~~~~", "null");
                        }
                    }
                    catch(Exception e){
                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                        Log.e("exception: ", e.getMessage());
                    }
                    finally {
                        db1.close();
                    }
                    latch.countDown();
                }
            };
            t1.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("lalala", "********");
        } else {
            Log.e("hahahahahahahahahahahha","2222222222");
        }
        if (state[7]) {
            latch = new CountDownLatch(1);
            Thread t1 = new Thread() {
                public void run() {
                    db1 = dbHelper.getWritableDatabase();
                    try {
                        if (db1 != null) {
                            //db1.execSQL("update accelerometer set Tag = 1 where timestamp between ? and ?", new Object[]{acces.get(0).getTimestamp(), acces.get(acces.size() - 1).getTimestamp()});
                            db1.execSQL("delete from magnetometer where timestamp between ? and ?", new Object[]{magnetometers.get(0).getTimestamp(), magnetometers.get(magnetometers.size() - 1).getTimestamp()});
                        } else {
                            Log.e("db1~~~~~~", "null");
                        }
                    }
                    catch(Exception e){
                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                        Log.e("exception: ", e.getMessage());
                    }
                    finally {
                        db1.close();
                    }
                    latch.countDown();
                }
            };
            t1.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("lalala", "********");
        } else {
            Log.e("hahahahahahahahahahahha","2222222222");
        }
    }
}
