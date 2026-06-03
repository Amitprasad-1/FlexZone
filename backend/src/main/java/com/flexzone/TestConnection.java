package com.flexzone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        String host = "bw91k5yvi9zoqz3si326-mysql.services.clever-cloud.com";
        String database = "bw91k5yvi9zoqz3si326";
        
        String[] users = {"uwn6eas4ot1wu2b0", "uwn6eas4otlwu2b0"};
        String[] passwords = {"Bn7jh23Iu9NIA1JHMvgk", "Bn7jh23lu9NIA1JHMvgk"};
        
        for (String user : users) {
            for (String password : passwords) {
                String url = "jdbc:mysql://" + host + ":3306/" + database + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                System.out.println("Testing Java JDBC: User=" + user + ", Password=" + password);
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    try (Connection conn = DriverManager.getConnection(url, user, password)) {
                        System.out.println("SUCCESS!!! Connected successfully with User=" + user + ", Password=" + password);
                        return;
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Driver class not found: " + e.getMessage());
                } catch (SQLException e) {
                    System.err.println("SQL Exception: " + e.getMessage());
                }
            }
        }
    }
}
