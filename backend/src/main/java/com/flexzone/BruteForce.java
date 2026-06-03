package com.flexzone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BruteForce {
    public static void main(String[] args) {
        String host = "bw91k5yvi9zoqz3si326-mysql.services.clever-cloud.com";
        String database = "bw91k5yvi9zoqz3si326";
        String url = "jdbc:mysql://" + host + ":3306/" + database + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        
        String[] users = {
            "uwn6eas4ot1wu2b0",
            "uwn6eas4otlwu2b0",
            "uwn6eas40t1wu2b0",
            "uwn6eas40tlwu2b0"
        };
        
        char[] choices = {'I', 'l', '1', 'i'};
        char[] bChoices = {'B', 'b'};
        char[] kChoices = {'k', 'K'};
        char[] uChoices = {'u', 'v'};
        char[] gChoices = {'g', '9', 'q'};
        
        List<String> passwords = new ArrayList<>();
        
        // Generate password variations
        for (char b : bChoices) {
            for (char p1 : choices) {
                for (char p2 : choices) {
                    for (char p3 : choices) {
                        for (char k : kChoices) {
                            for (char u : uChoices) {
                                for (char g : gChoices) {
                                    // Bn7jh23 [p1] u9N [p2] A [p3] JHM [u] [g] [k]
                                    String pwd = "" + b + "n7jh23" + p1 + "u9N" + p2 + "A" + p3 + "JHM" + u + g + k;
                                    passwords.add(pwd);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        System.out.println("Generated " + (users.length * passwords.size()) + " combinations to test.");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
            return;
        }
        
        int count = 0;
        for (String user : users) {
            for (String pwd : passwords) {
                count++;
                if (count % 100 == 0) {
                    System.out.println("Tested " + count + " combinations...");
                }
                
                try (Connection conn = DriverManager.getConnection(url, user, pwd)) {
                    System.out.println("\nSUCCESS!!! Connected successfully!");
                    System.out.println("User: " + user);
                    System.out.println("Password: " + pwd);
                    return;
                } catch (SQLException e) {
                    // Ignore access denied (1045)
                    if (e.getErrorCode() != 1045) {
                        System.out.println("Other error for " + user + " / " + pwd + ": " + e.getMessage());
                        // Sleep a bit and retry if it's a rate limit or connection limit error
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
        System.out.println("Finished! No working credentials found.");
    }
}
