package com.chaoyang805.taxiserver.model;

/**
 * Created by chaoyang805 on 2015/11/10.
 */
public class Driver extends User {

    /**
     * 司机已经接单
     */
    protected boolean mHasOrdered = false;

    public Driver(String name, String phoneNumber) {
        super(name, phoneNumber);
    }

    public void setHasOrdered(boolean hasOrdered) {
        mHasOrdered = hasOrdered;
    }

    public boolean isHasOrdered() {
        return mHasOrdered;
    }
}
