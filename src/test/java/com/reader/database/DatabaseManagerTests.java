package com.reader.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class DatabaseManagerTests {

    public static Connection connection;

    @Test
    public void testDatabasePreparation() {
        assertDoesNotThrow(() -> DatabaseManager.prepareDatabase());
        assertTrue(DatabaseManager.testConnection());
    }

    @Test
    public void checkIfTableExists() {
        assertDoesNotThrow(() -> {
            connection = DatabaseManager.getConnection();
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getTables(null, null, "EVENTS", null);
            assertTrue(rs.next());
        });


    }


    @AfterAll
    public static void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
