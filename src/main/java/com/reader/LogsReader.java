package com.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reader.entities.LogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LogsReader {
    public static final Logger logger = LogManager.getLogger(LogsReader.class);

    private File file;

    private final long BUFFER_SIZE = 1;
    private final int BATCH_SIZE = 100_000;
    private final int BATCHES_QUEUE_SIZE = 4;
    private final int EXECUTOR_MAX_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors() - 1;

    private final ThreadPoolExecutor executor;

    public LogsReader(File file) {
        this.file = file;
        executor = new ThreadPoolExecutor(1, EXECUTOR_MAX_NUMBER_OF_THREADS, 10L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(BATCHES_QUEUE_SIZE));
        executor.setRejectedExecutionHandler((Runnable task, ThreadPoolExecutor executor) -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted while waiting, ", e);
            }
            executor.execute(task);
        });
    }


    public void processLogs() {

        ObjectMapper om = new ObjectMapper();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            logger.error("An exception occurred while setting BufferReader: ", e);
            return;
        }

        Map<String, LogEntry> mergeBatch = new HashMap<>(BATCH_SIZE);
        List<LogEntry> insertBatch = new ArrayList<>(BATCH_SIZE);
        LogEntry currentEntry, oldEntry;
        try {
            while (true) {

                while (reader.ready()) {
                    currentEntry = om.readValue(reader.readLine(), LogEntry.class);

                    oldEntry = mergeBatch.remove(currentEntry.getId());

                    if (oldEntry != null) {
                        currentEntry.setTimestamp(Math.abs(currentEntry.getTimestamp() - oldEntry.getTimestamp()));
                        insertBatch.add(currentEntry);
                    } else {
                        mergeBatch.put(currentEntry.getId(), currentEntry);
                    }

                    if (mergeBatch.size() >= BATCH_SIZE) {
                        processMergeBatch(mergeBatch);
                        mergeBatch = new HashMap<>();
                    }

                    if (insertBatch.size() >= BATCH_SIZE) {
                        processInsertBatch(insertBatch);
                        insertBatch = new ArrayList<>();
                    }
                }

                //Process batches while waiting for new data
                if (!mergeBatch.isEmpty()) {
                    processMergeBatch(mergeBatch);
                    mergeBatch = new HashMap<>();
                }
                if (!insertBatch.isEmpty()) {
                    processInsertBatch(insertBatch);
                    insertBatch = new ArrayList<>();
                }

                //Waiting for new events in the logs file
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));

            }
        } catch (IOException | InterruptedException e) {
            logger.error("An exception occurred while reading logs: ", e);
            return;
        }
    }

    private void processMergeBatch(Map<String, LogEntry> logs) {
        executor.execute(new LogsProcessor().setMergeBatch(logs));
    }

    private void processInsertBatch(List<LogEntry> logs) {
        executor.execute(new LogsProcessor().setInsertBatch(logs));
    }

}