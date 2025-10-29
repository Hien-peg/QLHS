package com.sgu.qlhs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Read DB settings from system property (-Ddb.name=...) or environment variable
    // QLHS_DB
    private static final String DEFAULT_DB = "QLHS_New";
    private static final String DB_NAME = System.getProperty("db.name",
            System.getenv("QLHS_DB") == null ? DEFAULT_DB : System.getenv("QLHS_DB"));
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?serverTimezone=UTC&useSSL=false";
    // User
    private static final String USER = System.getProperty("db.user",
            System.getenv("QLHS_USER") == null ? "root" : System.getenv("QLHS_USER"));
    // Password
    private static final String PASSWORD = System.getProperty("db.pass",
            System.getenv("QLHS_PASS") == null ? "9851343a" : System.getenv("QLHS_PASS"));

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Register JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Open connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối cơ sở dữ liệu thành công! URL=" + URL + " user=" + USER);
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            System.err.println("SQLState=" + e.getSQLState() + " ErrorCode=" + e.getErrorCode());
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng kết nối: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
