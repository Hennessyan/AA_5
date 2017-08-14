package com.llu17.youngq.sqlite_gps;

import java.util.TimerTask;

/**
 * Created by youngq on 17/5/9.
 */

public class Loop extends TimerTask {

    VariableManager mListener;

    Loop(VariableManager mListener){
        this.mListener = mListener;
    }
    @Override
    public void run() {
        mListener.doYourWork();
    }

}
