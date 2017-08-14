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
import android.widget.Toast;

import com.llu17.youngq.sqlite_gps.data.GpsContract;
import com.llu17.youngq.sqlite_gps.data.GpsDbHelper;
import com.llu17.youngq.sqlite_gps.table.PARKINGSTATE;

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

import static com.llu17.youngq.sqlite_gps.MainActivity.upload_state_pl;

/**
 * Created by youngq on 17/8/13.
 */

public class UploadServicePL extends Service {

    private PowerManager.WakeLock wakeLock = null;

    private GpsDbHelper dbHelper;
    private SQLiteDatabase db,db1;


    private String park_url = "http://cs.binghamton.edu/~smartpark/lulu_test/parkingstate.php";
    private ArrayList<PARKINGSTATE> parkinglots;

    private JSONObject park_object;
    private JSONArray ParkJsonArray;


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
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        if(wifistate[0] == 1)
            upload_state_pl.setVisibility(android.view.View.GONE);
        if(wifistate[0] == 0) {
            unregisterReceiver(mConnReceiver);
            Log.e("on destroy","unregister");
        }
        Log.e("service","destroy");
    }


    private ArrayList<PARKINGSTATE> find_all_park(){
        dbHelper = new GpsDbHelper(this);
        db = dbHelper.getReadableDatabase();
        Cursor c = null;
        String s = "select Id, timestamp, state from parkingstate where Tag = 0 limit 200;";
        try {
            c = db.rawQuery(s, null);
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
                Log.e("i am here", "hello44444444");
            }
        }
        catch(Exception e){
            Log.e("exception: ", e.getMessage());
        }
        finally {
            c.close();
            db.close();
        }
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
            Log.e("response: ",jsonString);

            StatusCode = response.getStatusLine().getStatusCode();
            Log.e("status code: ", "" + StatusCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StatusCode;
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
    private int[] wifistate = new int[1];
    private int[] result = new int[1];
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo wifiNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(wifiNetworkInfo != null) {
                if (wifiNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    Log.e("wifi", "on");
                    wifistate[0] = 1;
                    boolean label = true;

                    while(label){
                        parkinglots = find_all_park();
                        if(parkinglots != null) {
                            latch = new CountDownLatch(1);
                            Thread t1 = new Thread() {
                                public void run() {
                                    result[0] = post_data(park_url, changeParkingDateToJson());
                                    latch.countDown();
                                }
                            };
                            t1.start();


                            try {
                                latch.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (result[0] == 200 ) {         //5*200+200= 1200
                            latch = new CountDownLatch(1);
                            Thread t1 = new Thread() {
                                public void run() {

                                    db1 = dbHelper.getWritableDatabase();
                                    try {
                                        if (db1 != null) {

                                            db1.execSQL("delete from parkingstate where timestamp between ? and ?", new Object[]{parkinglots.get(0).getTimestamp(), parkinglots.get(parkinglots.size() - 1).getTimestamp()});

                                        } else {
                                            Log.e("db1~~~~~~", "null");
                                        }
                                    } catch (Exception e) {
                                        Log.e("here~~~~~~~~~~~~~~", "stop upload");
                                        Log.e("exception: ", e.getMessage());
                                    } finally {
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
                            result[0] = 0;
                        }


                        if(parkinglots == null){
                            label = false;
                        }
                    }
                    upload_state_pl.setVisibility(android.view.View.VISIBLE);
                    unregisterReceiver(mConnReceiver);
                } else {
                    wifistate[0] = 0;
                    Log.e("wifi", "off");
                }
            }
            else{
                wifistate[0] = 0;
                Log.e("wifi", "off111");
            }
        }
    };

}


