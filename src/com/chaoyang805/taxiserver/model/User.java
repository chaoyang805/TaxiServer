package com.chaoyang805.taxiserver.model;

import org.apache.mina.core.session.IoSession;

/**
 * Created by chaoyang805 on 2015/11/10.
 */
public class User {
    protected String mName;
    protected String mPhoneNumber;
    protected double[] mLocation = new double[2];
    protected IoSession mSession;

    public User(String name,String phoneNumber){
        mName = name;
        mPhoneNumber = phoneNumber;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public double[] getLocation() {
        return mLocation;
    }

    public void setLocation(double[] location) {
        mLocation = location;
    }

    public IoSession getSession() {
        return mSession;
    }

    public void setSession(IoSession session) {
        mSession = session;
    }
}
