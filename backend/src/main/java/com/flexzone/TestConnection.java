package com.flexzone;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class TestConnection {
    public static void main(String[] args) {
        String host = "bw91k5yvi9zoqz3si326-mysql.services.clever-cloud.com";
        String database = "bw91k5yvi9zoqz3si326";
        String user = "uwn6eas4otlwu2b0";
        String password = "Bn7jh23Iu9NIAIJHMVgk";
        String url = "jdbc:mysql://" + host + ":3306/" + database + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String generatedHash = encoder.encode("admin123");
        System.out.println("Generated BCrypt Hash for 'admin123': " + generatedHash);

        System.out.println("Checking active connections...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SHOW PROCESSLIST")) {
                
                java.util.List<Integer> idsToKill = new java.util.ArrayList<>();
                long myId = -1;
                
                // Get current connection ID
                try (java.sql.Statement idStmt = conn.createStatement();
                     java.sql.ResultSet idRs = idStmt.executeQuery("SELECT CONNECTION_ID()")) {
                    if (idRs.next()) {
                        myId = idRs.getLong(1);
                    }
                }
                
                System.out.println("My Connection ID: " + myId);
                System.out.printf("%-10s %-20s %-20s %-10s %-10s %-10s %-20s\n", "Id", "User", "Host", "db", "Command", "Time", "State");
                while (rs.next()) {
                    int id = rs.getInt("Id");
                    String dbUser = rs.getString("User");
                    String dbHost = rs.getString("Host");
                    String db = rs.getString("db");
                    String command = rs.getString("Command");
                    int time = rs.getInt("Time");
                    String state = rs.getString("State");
                    
                    System.out.printf("%-10d %-20s %-20s %-10s %-10s %-10d %-20s\n", id, dbUser, dbHost, db, command, time, state);
                    
                    if (id != myId && "Sleep".equalsIgnoreCase(command)) {
                        idsToKill.add(id);
                    }
                }
                
                for (int id : idsToKill) {
                    System.out.println("Attempting to kill connection: " + id);
                    try (java.sql.Statement killStmt = conn.createStatement()) {
                        killStmt.execute("KILL " + id);
                        System.out.println("Successfully killed connection " + id);
                    } catch (Exception e) {
                        System.err.println("Failed to kill connection " + id + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Resetting admin password in database...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement pstmt = conn.prepareStatement(
                         "UPDATE users SET password = ? WHERE username = 'admin'")) {
                
                pstmt.setString(1, generatedHash);
                int rows = pstmt.executeUpdate();
                System.out.println("Updated " + rows + " row(s).");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
