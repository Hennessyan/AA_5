package com.llu17.youngq.sqlite_gps.table;

/**
 * Created by youngq on 17/8/12.
 */

public class PARKINGSTATE {
    private String id;
    private long timestamp;
    private int state;

    public PARKINGSTATE(String id, long timestamp, int state) {
        this.id = id;
        this.timestamp = timestamp;
        this.state = state;
    }

    public PARKINGSTATE() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "PARKINGSTATE: {" + " id: " + id + " timestamp: " + timestamp + " state: "
                + state + " }";
    }
}