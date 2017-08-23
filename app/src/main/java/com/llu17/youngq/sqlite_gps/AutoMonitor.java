package com.llu17.youngq.sqlite_gps;

import android.os.Message;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import static com.llu17.youngq.sqlite_gps.MainActivity.mHandler;


/**
 * Created by youngq on 17/8/10.
 */

public class AutoMonitor extends TimerTask {
    private int flag = 0;
    @Override
    public void run() {
        long cur_time = System.currentTimeMillis();
        flag = checkTimeInRange(cur_time);
        Message message = mHandler.obtainMessage();
        message.arg1 = flag;
        mHandler.sendMessage(message);
    }

    private int checkTimeInRange(long time){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date cur1 = new Date(time);
        String str = formatter.format(cur1);

        Date cur = null;
        Date daystart = null, dayend = null;
        Date daystart1 = null, dayend1 = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        try {
            cur = sdf.parse(str);
//            daystart = sdf.parse("01:00:00");
//            dayend = sdf.parse("06:00:00");
            daystart = sdf.parse("13:30:00");
            dayend = sdf.parse("13:35:00");

            daystart1 = sdf.parse("13:40:00");
            dayend1 = sdf.parse("13:45:00");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(cur.equals(daystart) || cur.equals(daystart1)){
            return 1;
        }else if(cur.equals(dayend) || cur.equals(dayend1)){
            return 2;
        }
        return -1;
    }
}
