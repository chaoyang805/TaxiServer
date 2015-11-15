package com.chaoyang805.taxiserver.database;


import com.chaoyang805.taxiserver.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaoyang805 on 2015/11/15.
 */
public class MySqlHelper implements MysqlDAO {

    private Statement mStatement;
    private Connection mConnection;
    private String mUrl = "jdbc:mysql://localhost:3306/calltaxi";

    private MySqlHelper() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("成功加载MySQL驱动");

        } catch (ClassNotFoundException e) {
            System.out.println("找不到MySQL驱动");
            e.printStackTrace();
        }
    }

    private static MySqlHelper mHelper = null;

    public static MySqlHelper getInstance() {
        if (mHelper == null) {
            mHelper = new MySqlHelper();
        }
        return mHelper;
    }

    @Override
    public int insertUser(User user) {
        int count = -1;
        try {
            mConnection = DriverManager.getConnection(mUrl, "root", "");
            mStatement = mConnection.createStatement();
            count = mStatement.executeUpdate("INSERT INTO users (name,phonenum,lat,lng) VALUES (" +
                    "'" + user.getName() + "'" + "," +
                    "'" + user.getPhoneNumber() + "'" + "," +
                    user.getLocation()[0] + "," +
                    user.getLocation()[1] + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                mStatement.close();
                mConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public int deleteUser(User user) {
        int count = -1;
        try {
            mConnection = DriverManager.getConnection(mUrl, "root", "");
            mStatement = mConnection.createStatement();
            count = mStatement.executeUpdate("DELETE FROM users WHERE phonenum = " + "'" + user.getPhoneNumber() + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                mStatement.close();
                mConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public int updateUserLocation(User user) {
        int count = -1;
        try {
            mConnection = DriverManager.getConnection(mUrl, "root", "");
            mStatement = mConnection.createStatement();
            count = mStatement.executeUpdate("UPDATE users SET " +
                    "name=" + "'" + user.getName() + "'," + "lat=" + user.getLocation()[0] + "," +
                    "lng=" + user.getLocation()[1] + " WHERE phonenum = " + "'" + user.getPhoneNumber() + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                mStatement.close();
                mConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public int updateUserName(User user) {
        int count = -1;
        try {
            mConnection = DriverManager.getConnection(mUrl, "root", "");
            mStatement = mConnection.createStatement();
            count = mStatement.executeUpdate("UPDATE users SET " +
                    "name=" + "'" + user.getName() + "'" + " WHERE phonenum = " + "'" + user.getPhoneNumber() + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                mStatement.close();
                mConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public User queryUserByPhone(String phone) {
        ResultSet rs;
        try {
            mConnection = DriverManager.getConnection(mUrl, "root", "");
            mStatement = mConnection.createStatement();
            rs = mStatement.executeQuery("SELECT * FROM users WHERE phonenum = " + "'" + phone + "'");
            if (rs.first()) {
                String name = rs.getString("name");
                String phoneNum = rs.getString("phonenum");
                User user = new User(name, phoneNum);
                double[] location = new double[]{rs.getDouble("lat"), rs.getDouble("lng")};
                user.setLocation(location);
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                mStatement.close();
                mConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<User> queryAllUser() {
        ResultSet rs;
        List<User> users = new ArrayList<>();
        String name;
        String phoneNum;
        try {
            mConnection = DriverManager.getConnection(mUrl, "root", "");
            mStatement = mConnection.createStatement();
            rs = mStatement.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                name = rs.getString("name");
                phoneNum = rs.getString("phonenum");
                User user = new User(name, phoneNum);
                double[] location = new double[]{rs.getDouble("lat"), rs.getDouble("lng")};
                user.setLocation(location);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                mStatement.close();
                mConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
