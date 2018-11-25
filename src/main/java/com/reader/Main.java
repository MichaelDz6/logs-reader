package com.reader;

import com.reader.database.DatabaseManager;
import com.reader.utility.LogsConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Main {

    static {
        LogsConfiguration.setupLogging();
    }

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static final int ALERT_THRESHOLD = 4;

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Missing logs file path argument, please provide logs file path as an argument while startting the application. Shutting down VM.");
            return;
        }
        String filePath = args[0];
        File file = new File(filePath);

        if (!file.isFile()) {
            logger.error("File not found: " + filePath + ". Shutting down VM.");
            return;
        }

        logger.info("Preparing database.");
        DatabaseManager.prepareDatabase();
        LogsReader logsReader = new LogsReader(file);
        logger.info("Starting logs processing.");
        logsReader.processLogs();
    }

}