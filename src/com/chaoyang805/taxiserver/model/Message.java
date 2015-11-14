package com.chaoyang805.taxiserver.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chaoyang805 on 2015/11/9.
 */
public class Message {

    private String mMessageStr;

    private JSONObject mJsonMsg;

    public Message() {

    }

    public Message(String jsonMsg) {
        try {
            mJsonMsg = new JSONObject(jsonMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getDriverName() {
        String name = null;
        try {
            name = mJsonMsg.getString("driverName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    public String getDriverPhoneNumber() {
        String phoneNumber = null;
        try {
            phoneNumber = mJsonMsg.getString("driverPhoneNumber");
        } catch (JSONException e) {
            e.printStackTrace();
        }
            return phoneNumber;
    }
    public String getPassengerName(){
        String name = null;
        try{
            name = mJsonMsg.getString("passengerName");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return name;
    }

    public String getPassengerPhoneNumber(){
        String phoneNumber = null;
        try {
            phoneNumber = mJsonMsg.getString("passengerPhoneNumber");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return phoneNumber;
    }

    public double[] getLocation() {
        double[] latLng = new double[2];
        try {
            JSONObject location = mJsonMsg.getJSONObject("location");
            latLng[0] = location.getDouble("lat");
            latLng[1] = location.getDouble("lng");
            return latLng;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Destination getDestination() {
        JSONObject destJson = null;
        Destination destination = null;
        try {
            destJson = mJsonMsg.getJSONObject("destination");
            destination = new Destination();
            destination.setDetailAdress(destJson.getString("detailAdress"));
            destination.setLocation(new double[]{destJson.getDouble("destLat"),
                    destJson.getDouble("destLng")});
            destination.setDistance(destJson.getDouble("distance"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return destination;
    }

    public void setJsonMsg(JSONObject jsonMsg) {
        mJsonMsg = jsonMsg;
    }

    public void setMessageStr(String messageStr) {
        mMessageStr = messageStr;
    }

    @Override
    public String toString() {
        return mMessageStr + mJsonMsg.toString();
    }


    public static class MessageBuilder {
        private final JSONObject J;
        private String type;

        public MessageBuilder() {
            J = new JSONObject();
        }

        public MessageBuilder setDriverName(String name) {
            try {
                J.put("driverName", name);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public MessageBuilder setDriverPhoneNumber(String phoneNumber) {
            try {
                J.put("driverPhoneNumber", phoneNumber);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public MessageBuilder setPassengerName(String name){
            try {
                J.put("passengerName", name);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        public MessageBuilder setPassengerPhoneNumber(String phoneNumber){
            try {
                J.put("passengerPhoneNumber", phoneNumber);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public MessageBuilder setLocation(double[] location) {
            JSONObject locationJson = new JSONObject();
            try {
                locationJson.put("lat", location[0]);
                locationJson.put("lng", location[1]);
                J.put("location", locationJson);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public MessageBuilder setDestination(Destination destination) {
            try {
                JSONObject destJson = new JSONObject();
                destJson.put("detailAdress", destination.getDetailAdress());
                destJson.put("destLat",destination.getLocation()[0]);
                destJson.put("destLng",destination.getLocation()[1]);
                destJson.put("distance", destination.getDistance());
                J.put("destination", destJson);
                return this;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public MessageBuilder setRequestType(String requestType) {
            this.type = requestType;
            return this;
        }

        public Message build() {
            final Message message = new Message();
            message.setJsonMsg(J);
            message.setMessageStr(type + "://");
            return message;
        }
    }
}
