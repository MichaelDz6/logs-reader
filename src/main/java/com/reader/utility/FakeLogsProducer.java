package com.reader.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reader.entities.LogEntry;

public class FakeLogsProducer {

    private static final int BUFFER_SIZE = 52428800;
    private final Random rand = new Random();
    int counter = 0;
    int size = 0;

    public void createFakeLogs(String filePath, long fileSizeMb) throws IOException {
        long fileSizeBytes = fileSizeMb * 1024L * 1024L;
        File file = new File(filePath);
        file.createNewFile();

        Set<LogEntry> logs = new TreeSet<>();
//		Set<LogEntry> logs = new HashSet<>();
        ObjectMapper om = new ObjectMapper();

        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter writer = new BufferedWriter(fileWriter, BUFFER_SIZE);){


            while(file.length() < fileSizeBytes) {
                fillLogs(logs, 1000);

                for(Iterator<LogEntry> iterator = logs.iterator(); iterator.hasNext() && file.length() < fileSizeBytes; ) {
                    writer.write(om.writeValueAsString(iterator.next()));
                    writer.newLine();
                    counter++;
                }
            }
        }
        System.out.println("Logs created");
        System.out.println("File size: " + file.length());
        System.out.println("File size mb: " + file.length() / 1024 / 1024);
    }

    private void fillLogs(Set<LogEntry> logs, int n) {
        logs.clear();
        for (int i = 0; i < n; i++) {

            String id = UUID.randomUUID().toString();
            long timestamp = Instant.now().toEpochMilli();
            String type = rand.nextDouble() < 0.3 ? rand.nextInt(50) + "" : null;
            String host = rand.nextDouble() < 0.3 ? rand.nextInt(9000) + "" : null;

            logs.add(new LogEntry(id, "STARTED", type, host, timestamp));
            logs.add(new LogEntry(id, "FINISHED", type, host, timestamp + rand.nextInt(6) + 1));
        }
    }



}
