package com.brs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/brs_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUser;

    @Value("${spring.datasource.password:root}")
    private String mysqlPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        boolean mysqlAvailable = isPortAvailable("localhost", 3306, 1200);

        if (mysqlAvailable) {
            try {
                log.info("MySQL detected on port 3306. Connecting to MySQL database...");
                DataSource mysqlDs = DataSourceBuilder.create()
                        .url(mysqlUrl)
                        .username(mysqlUser)
                        .password(mysqlPassword)
                        .driverClassName("com.mysql.cj.jdbc.Driver")
                        .build();

                // Test connection
                try (var conn = mysqlDs.getConnection()) {
                    log.info("Successfully connected to MySQL database: {}", conn.getMetaData().getDatabaseProductName());
                    return mysqlDs;
                }
            } catch (Exception e) {
                log.warn("MySQL connection failed ({}), falling back to embedded database.", e.getMessage());
            }
        } else {
            log.warn("MySQL is not running on localhost:3306. Automatically activating embedded database fallback.");
        }

        log.info("Starting with Embedded H2 Database (MySQL compatibility mode) so you can run without starting MySQL service.");
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:brs_db;DB_CLOSE_DELAY=-1;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver")
                .build();
    }

    private boolean isPortAvailable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
