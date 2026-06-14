package com.flexzone.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String defaultUrl;

    @Value("${spring.datasource.username}")
    private String defaultUsername;

    @Value("${spring.datasource.password}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        String mysqlAddonUri = System.getenv("MYSQL_ADDON_URI");
        String connectionUrl = defaultUrl;
        String username = defaultUsername;
        String password = defaultPassword;
        String driverToUse = driverClassName;

        if (mysqlAddonUri != null && !mysqlAddonUri.trim().isEmpty()) {
            ParsedUri parsed = parseConnectionUri(mysqlAddonUri);
            if (parsed != null) {
                connectionUrl = parsed.jdbcUrl;
                username = parsed.username;
                password = parsed.password;
            }
        } else if (connectionUrl != null && (connectionUrl.startsWith("mysql://") || connectionUrl.contains("@"))) {
            ParsedUri parsed = parseConnectionUri(connectionUrl);
            if (parsed != null) {
                connectionUrl = parsed.jdbcUrl;
                username = parsed.username;
                password = parsed.password;
            }
        }

        // Auto-detect PostgreSQL urls to select correct driver
        if (connectionUrl != null && connectionUrl.startsWith("jdbc:postgresql://")) {
            driverToUse = "org.postgresql.Driver";
        }

        System.out.println("Configuring DataSource: URL=" + connectionUrl + ", Username=" + username);

        // Test connection to the primary database
        try {
            Class.forName(driverToUse);
            // 2-second timeout to test connection fast
            DriverManager.setLoginTimeout(2);
            try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
                System.out.println(">>> Primary database connection successful! Using " + connectionUrl);
            }
        } catch (Exception e) {
            System.err.println(">>> Failed to connect to primary database: " + e.getMessage());
            System.out.println(">>> FALLING BACK to local persistent H2 database (flexzone_db.mv.db)");
            connectionUrl = "jdbc:h2:file:./flexzone_db;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;NON_KEYWORDS=KEY,VALUE";
            username = "sa";
            password = "";
            driverToUse = "org.h2.Driver";
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverToUse);
        config.setJdbcUrl(connectionUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(20000);
        
        return new HikariDataSource(config);
    }

    private static ParsedUri parseConnectionUri(String uriStr) {
        try {
            String working = uriStr.trim();
            if (working.startsWith("jdbc:mysql://")) {
                working = working.substring("jdbc:mysql://".length());
            } else if (working.startsWith("mysql://")) {
                working = working.substring("mysql://".length());
            }

            int atIdx = working.indexOf('@');
            String userInfo = null;
            String hostPortDb = working;
            if (atIdx != -1) {
                userInfo = working.substring(0, atIdx);
                hostPortDb = working.substring(atIdx + 1);
            }

            String username = null;
            String password = null;
            if (userInfo != null) {
                int colonIdx = userInfo.indexOf(':');
                if (colonIdx != -1) {
                    username = userInfo.substring(0, colonIdx);
                    password = userInfo.substring(colonIdx + 1);
                } else {
                    username = userInfo;
                }
            }

            int queryIdx = hostPortDb.indexOf('?');
            String mainPart = queryIdx != -1 ? hostPortDb.substring(0, queryIdx) : hostPortDb;
            String queryParams = queryIdx != -1 ? hostPortDb.substring(queryIdx) : "";

            int slashIdx = mainPart.indexOf('/');
            String hostPort = mainPart;
            String database = "";
            if (slashIdx != -1) {
                hostPort = mainPart.substring(0, slashIdx);
                database = mainPart.substring(slashIdx + 1);
            }

            if (queryParams.isEmpty()) {
                queryParams = "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            }

            String jdbcUrl = "jdbc:mysql://" + hostPort + "/" + database + queryParams;
            return new ParsedUri(jdbcUrl, username, password);
        } catch (Exception e) {
            System.err.println("Failed to parse database connection URI: " + uriStr + ". Error: " + e.getMessage());
            return null;
        }
    }

    private static class ParsedUri {
        final String jdbcUrl;
        final String username;
        final String password;

        ParsedUri(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }
    }
}
