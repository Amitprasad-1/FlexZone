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
