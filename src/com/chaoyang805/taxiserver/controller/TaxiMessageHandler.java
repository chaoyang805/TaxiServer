package com.chaoyang805.taxiserver.controller;

import com.chaoyang805.taxiserver.model.Destination;
import com.chaoyang805.taxiserver.model.Driver;
import com.chaoyang805.taxiserver.model.Message;
import com.chaoyang805.taxiserver.model.Passenger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chaoyang805 on 2015/11/9.
 */
public class TaxiMessageHandler extends IoHandlerAdapter {

    private Map<String, Passenger> mPassengers = new HashMap<>();
    private Map<String, Driver> mDrivers = new HashMap<>();

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        System.out.println("sessionCreated");
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        System.out.println("messageReceived " + (String) message);
        String msg = (String) message;
        String[] results = msg.split("://");
        String requestType = results[0];
        String detail = results[1];
        switch (requestType) {
            //乘客登录的请求
            case "passenger_login":
                handlePassengerLogin(detail, session);
                break;
            //乘客更新位置信息的请求
            case "passenger_update_location":
                handlePassengerUpdateLocation(detail);
                break;
            //乘客发送约车信息的请求
            case "passenger_call_taxi":
                handlePassengerCallTaxi(detail);
                break;
            //乘客取消约车请求
            case "passenger_cancel_call":
                handlePassengerCancelCall(detail);
                break;
            //司机的登录请求
            case "driver_login":
                handleDriverLogin(detail, session);
                break;
            //司机的位置更新请求
            case "driver_update_location":
                handleDriverUpdateLocation(detail);
                break;
            //司机的接单请求
            case "driver_accept":
                handleDriverAccept(detail);
                break;
            case "disconnect":
                handleSessionDisconnect(detail);
                break;
            default:
                System.out.println("unknown requestType:" + requestType);
                break;
        }
    }

    private void handleSessionDisconnect(String detail) {
        Message message = new Message(detail);
        String driverPhoneNumber = message.getDriverPhoneNumber();
        String passengerPhoneNumber = message.getPassengerPhoneNumber();
        if (driverPhoneNumber == null) {
            Passenger passenger = mPassengers.remove(passengerPhoneNumber);
            //通知这个乘客周围的司机 他已经掉线
            for (Driver driver : mDrivers.values()) {
                if (isNearBy(passenger, driver) && passenger.isWaitingOrder()) {
                    Message msg = new Message.MessageBuilder()
                            .setRequestType("passenger_offline")
                            .setPassengerName(message.getPassengerName())
                            .setPassengerPhoneNumber(passengerPhoneNumber)
                            .build();
                    driver.getSession().write(msg.toString());
                }
            }
        } else if (passengerPhoneNumber == null) {
            //通知这个司机附近的乘客，他已经掉线
            Driver driver = mDrivers.remove(driverPhoneNumber);
            for (Passenger passenger : mPassengers.values()){
                if (isNearBy(passenger,driver)){
                    Message msg = new Message.MessageBuilder()
                            .setRequestType("driver_offline")
                            .setDriverName(message.getDriverName())
                            .setDriverPhoneNumber(driverPhoneNumber)
                            .build();
                    passenger.getSession().write(msg.toString());
                }
            }
        }
    }

    /**
     * 处理司机的接单请求
     *
     * @param detail
     */
    private void handleDriverAccept(String detail) {
        Message message = new Message(detail);
        //将司机的接单请求发送给相应的乘客
        Passenger passenger = mPassengers.get(message.getPassengerPhoneNumber());
        Message acceptMsg = new Message.MessageBuilder()
                .setRequestType("driver_accept")
                .setDriverName(message.getDriverName())
                .setDriverPhoneNumber(message.getDriverPhoneNumber())
                .setLocation(message.getLocation())
                .build();
        passenger.getSession().write(acceptMsg.toString());
        //将乘客被接单的信息发送给附近的司机，通知司机端移除这一个乘客的信息
        Message takenMsg = new Message.MessageBuilder()
                .setRequestType("passenger_taken")
                .setPassengerName(message.getPassengerName())
                .setPassengerPhoneNumber(message.getPassengerPhoneNumber())
                .build();
        for (Driver driver : mDrivers.values()) {
            if (isNearBy(passenger, driver)) {
                driver.getSession().write(takenMsg.toString());
            }
        }
        //将已经被接单的乘客从Map中移除
        mPassengers.remove(passenger.getPhoneNumber());

    }

    private void handleDriverUpdateLocation(String detail) {
        System.out.println("handlePassengerUpdateLocation");
        Message message = new Message(detail);
        double[] location = message.getLocation();
        //缓存司机的位置信息
        Driver driver = mDrivers.get(message.getDriverPhoneNumber());
        if (driver != null) {
            driver.setLocation(location);
            mDrivers.put(driver.getPhoneNumber(), driver);
            //TODO 将location更新到数据库

            //计算该司机附近2km内的乘客，并发送司机信息到乘客端
            for (Passenger passenger : mPassengers.values()) {
                if (isNearBy(passenger, driver)) {
                    //向乘客发送2km附近司机的位置信息
                    sendDriver2Passenger(passenger, driver);
                    //向司机发送当前乘客的最新位置信息,条件是乘客正在等待约车
                    sendPassenger2Driver(passenger, driver);
                }
            }
        } else {
            System.out.println("driver等于null，肯定是哪里出错了");
        }
    }

    private void handleDriverLogin(String detail, IoSession session) {
        Message message = new Message(detail);
        String name = message.getDriverName();
        String phoneNumber = message.getDriverPhoneNumber();
        Driver driver = new Driver(name, phoneNumber);
        driver.setSession(session);
        //TODO 保存到数据库
        mDrivers.put(driver.getPhoneNumber(), driver);
    }

    /**
     * 执行乘客取消约车的请求，将乘客的isWaiting设为false,
     * 给附近的司机发消息：passenger_cancel_call://
     *
     * @param detail
     */
    private void handlePassengerCancelCall(String detail) {
        Message msg = new Message(detail);
        Passenger passenger = mPassengers.get(msg.getPassengerPhoneNumber());
        passenger.setIsWaitingOrder(false);
        //向附近的司机发送乘客取消约车的信息
        for (Driver driver : mDrivers.values()) {
            if (isNearBy(passenger, driver)) {
                Message cancelMsg = new Message.MessageBuilder()
                        .setRequestType("passenger_cancel_call")
                        .setPassengerName(passenger.getName())
                        .setPassengerPhoneNumber(passenger.getPhoneNumber())
                        .build();
                driver.getSession().write(cancelMsg.toString());
            }
        }
    }

    /**
     * 执行乘客的等待约车的请求
     *
     * @param detail
     */
    private void handlePassengerCallTaxi(String detail) {
        Message message = new Message(detail);
        String phoneNumber = message.getPassengerPhoneNumber();
        Passenger passenger = mPassengers.get(phoneNumber);
        passenger.setIsWaitingOrder(true);
        Destination destination = message.getDestination();
        passenger.setDestination(destination);
        for (Driver driver : mDrivers.values()) {
            if (isNearBy(passenger, driver)) {
                sendPassenger2Driver(passenger, driver);
            }
        }

    }

    /**
     * 执行乘客更新位置的请求，
     * 将附近司机的信息返回，
     * 如果乘客正在约车，将该乘客的信息发送给司机。
     *
     * @param detail
     */
    private void handlePassengerUpdateLocation(String detail) {
        System.out.println("handlePassengerUpdateLocation");
        Message message = new Message(detail);
        double[] location = message.getLocation();
        //缓存乘客的信息
        Passenger passenger = mPassengers.get(message.getPassengerPhoneNumber());
        if (passenger != null) {
            passenger.setLocation(location);
            mPassengers.put(passenger.getPhoneNumber(), passenger);
            //TODO 将location更新到数据库

            //计算该乘客附近2km内的司机，并发送司机信息到乘客端
            for (Driver driver : mDrivers.values()) {
                if (isNearBy(passenger, driver)) {
                    System.out.println("isNearBy");
                    //向乘客发送2km附近司机的位置信息
                    sendDriver2Passenger(passenger, driver);

                    //向司机发送当前乘客的最新位置信息,条件是乘客正在等待约车
                    sendPassenger2Driver(passenger, driver);
                }
            }
        } else {
            System.out.println("passenger等于null，肯定是哪里出错了");
        }
    }

    private void sendDriver2Passenger(Passenger passenger, Driver driver) {
        if (driver.isHasOrdered()) {
            return;
        }
        Message updateDriver = new Message.MessageBuilder()
                .setRequestType("update_driver")
                .setDriverName(driver.getName())
                .setDriverPhoneNumber(driver.getPhoneNumber())
                .setLocation(driver.getLocation())
                .build();
        passenger.getSession().write(updateDriver.toString());
        System.out.println("update_driver: passengerName:" + passenger.getName() + " " + updateDriver.toString());
    }

    /**
     * 发送正在约车的乘客信息到司机
     *
     * @param passenger
     * @param driver
     */
    private void sendPassenger2Driver(Passenger passenger, Driver driver) {
        if (passenger.isWaitingOrder()) {
            Message updatePassenger = new Message.MessageBuilder()
                    .setRequestType("update_passenger")
                    .setPassengerName(passenger.getName())
                    .setPassengerPhoneNumber(passenger.getPhoneNumber())
                    .setLocation(passenger.getLocation())
                    .setDestination(passenger.getDestination())
                    .build();
            driver.getSession().write(updatePassenger.toString());
        }
    }

    private boolean isNearBy(Passenger passenger, Driver driver) {
        return getDistance(passenger, driver) <= 2000;
    }

    /**
     * 执行乘客的登录请求
     *
     * @param detail
     * @param session 与当前客户端对应的session对象
     */
    private void handlePassengerLogin(String detail, IoSession session) {
        Message message = new Message(detail);
        String name = message.getPassengerName();
        String phoneNumber = message.getPassengerPhoneNumber();
        Passenger passenger = new Passenger(name, phoneNumber);
        passenger.setSession(session);
        //TODO 保存到数据库
        mPassengers.put(passenger.getPhoneNumber(), passenger);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        System.out.println("sessionClosed");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        super.sessionIdle(session, status);
        System.out.println("session idle");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        System.out.println("exceptionCaught");
    }

    private static final double EARTH_RADIUS = 6378137;

    private double toRad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 计算乘客和司机之间的距离，单位为米
     *
     * @param passenger
     * @param driver
     * @return
     */
    public double getDistance(Passenger passenger, Driver driver) {
        //乘客的经纬度
        double pLat = passenger.getLocation()[0];
        double pLng = passenger.getLocation()[1];
        //司机的经纬度
        double dLat = driver.getLocation()[0];
        double dLng = driver.getLocation()[1];

        double pRadLat = toRad(pLat);
        double dRadLat = toRad(dLat);

        double a = toRad(pLat) - toRad(dLat);
        double b = toRad(pLng) - toRad(dLng);
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(pRadLat) * Math.cos(dRadLat) * Math.pow(Math.sin(b / 2), 2)));
        distance = distance * EARTH_RADIUS;
        distance = Math.round(distance * 10000) / 10000;
        System.out.println("distance=" + distance);
        return distance;
    }
}
