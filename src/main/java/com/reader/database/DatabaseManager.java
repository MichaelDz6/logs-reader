package com.reader.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {

    public static final Logger logger = LogManager.getLogger(DatabaseManager.class);

    public static final String JDBC_CONNECTION = "jdbc:hsqldb:hsql://localhost/Test;user=SA;password=";
    public static final String JDBC_DRIVER_NAME = "org.hsqldb.jdbc.JDBCDriver";

    public static Process dbProcess = null;

    public static Connection dbConnection;

    public static void prepareDatabase() {

        if (!DatabaseManager.testConnection()) {
            DatabaseManager.startDatabase();
            if (!DatabaseManager.testConnection()) {
                logger.error("Database is still down, shutting down VM, try again or start the database by yourself.");
                System.exit(1);
            }
        }

        try {
            dbConnection = getConnection();
        } catch (ClassNotFoundException e) {
            logger.error(
                    "An exception occurred while establishing db connection, shutting down VM, try again or start the database by yourself.");
            System.exit(1);
        } catch (SQLException e) {
            logger.error(
                    "An exception occurred while establishing db connection, shutting down VM, try again or start the database by yourself.");
            System.exit(1);
        }

        DatabaseManager.createTable();
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER_NAME);
        return DriverManager.getConnection(JDBC_CONNECTION);
    }

    public static boolean testConnection() {
        try {
            getConnection();
        } catch (SQLException e) {
            return false;
        } catch (ClassNotFoundException e) {
            logger.error("An exception occurred while checking DB connection ", e);
        }

        return true;
    }

    private static void startDatabase() {
        logger.info("Starting HSQLDB database");
        try {
            String HSQLDBDirectory = " ../resources/main/hsqldb/lib/hsqldb.jar";
            dbProcess = Runtime.getRuntime()
                    .exec("java -cp " + HSQLDBDirectory + " org.hsqldb.server.Server --database.0 file:"
                            + "DB/mydb --dbname.0 Test");
        } catch (IOException e) {
            logger.error("Can't start DB, shutting down VM, try again or start the database by yourself. ", e);
            System.exit(1);
        }
        // Close DB before VM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down DB");
            dbProcess.destroy();
        }));

        // Intercept DB logs
        DatabaseManager.DBLogsInterceptor dbLogger = new DatabaseManager.DBLogsInterceptor();
        dbLogger.setDaemon(true);
        dbLogger.start();

        try {
            // Wait for DB startup
            logger.info("Waiting 10 sec for DB startup");
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            logger.error("Waiting for DB interrupted ", e);
        }
    }

    static void createTable() {
        try (Statement stmt = dbConnection.createStatement();) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS EVENTS (ID VARCHAR(100) NOT NULL, DURATION BIGINT NOT NULL, TYPE VARCHAR(100), HOST VARCHAR(100), ALERT BOOLEAN)");

        } catch (SQLException e) {
            logger.error("An exception occurred while creating new table ", e);
        }
    }

    private static class DBLogsInterceptor extends Thread {

        @Override
        public void run() {
            try (InputStreamReader isr = new InputStreamReader(dbProcess.getInputStream());
                 BufferedReader br = new BufferedReader(isr);) {

                String line;
                while ((line = br.readLine()) != null) {
                    logger.info(line);
                }
            } catch (IOException e1) {
                logger.error("An exception occurred while reading DB logs");
            }
        }

    }

}
