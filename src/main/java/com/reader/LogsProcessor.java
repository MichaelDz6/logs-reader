package com.reader;

import com.reader.database.DatabaseManager;
import com.reader.entities.LogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LogsProcessor implements Runnable {
    public static final Logger logger = LogManager.getLogger(LogsProcessor.class);

    private static final String MERGE_QUERY = "MERGE INTO EVENTS USING (VALUES ?)"
            + " AS vals(id,duration,type,host,alert) ON EVENTS.id = vals.id"
            + " WHEN MATCHED THEN UPDATE SET "
            + " EVENTS.duration = ABS(EVENTS.duration - vals.duration),EVENTS.type=vals.type,EVENTS.host=vals.host,"
            + " EVENTS.alert=(CASE WHEN ABS(EVENTS.duration - vals.duration) > 4 THEN true ELSE false END)"
            + " WHEN NOT MATCHED THEN INSERT VALUES vals.id, vals.duration,vals.type,vals.host,vals.alert";

    private static final String INSERT_QUERY = "INSERT INTO EVENTS VALUES ?";



    private Map<String, LogEntry> mergeBatch;
    private List<LogEntry> insertBatch;



    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        Iterator<LogEntry> iterator = mergeBatch != null ? mergeBatch.values().iterator() : insertBatch.iterator();
        String query = mergeBatch != null ? MERGE_QUERY : INSERT_QUERY;

        while (iterator.hasNext()) {
            builder.append(iterator.next().toStringForSQL());
            if (iterator.hasNext()) {
                builder.append(",");
            }
        }

        synchronized (DatabaseManager.dbConnection) {
            try (Statement stmt = DatabaseManager.dbConnection.createStatement();) {
                stmt.executeUpdate(query.replace("?", builder.toString()));
            } catch (SQLException e) {
                logger.error("An exception occurred while executing a statement ", e);
                return;
            }

        }
    }

    public LogsProcessor setMergeBatch(Map<String, LogEntry> mergeBatch){
        this.mergeBatch = mergeBatch;
        return this;
    }

    public LogsProcessor setInsertBatch(List<LogEntry> insertBatch){
        this.insertBatch = insertBatch;
        return this;
    }

}
