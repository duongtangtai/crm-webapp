package cybersoft.java18.jdbc;

import cybersoft.java18.exception.DatabaseNotFoundException;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQLConnection {
    public static final String URL = "jdbc:mysql://localhost:3308/crm_app";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "1234";
    private MySQLConnection() {
        throw new IllegalStateException("Utility Class");
    }
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            throw new DatabaseNotFoundException("error while accessing to database");
        }
    }
}
