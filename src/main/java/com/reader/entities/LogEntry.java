package com.reader.entities;

import com.reader.Main;

public class LogEntry implements Comparable<LogEntry>{

    private String	id;
    private String	state;
    private String	type;
    private String	host;
    private long	timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LogEntry() {}

    public LogEntry(String id, String state, String type, String host, long timestamp) {
        super();
        this.id = id;
        this.state = state;
        this.type = type;
        this.host = host;
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LogEntry)) {
            return false;
        }
        LogEntry entry = (LogEntry) obj;
        return id.equals(entry.getId()) && state.equals(entry.getState());
    }

    @Override
    public String toString() {
        return "LogEntry [id=" + id + ", state=" + state + ", type=" + type + ", host=" + host + ", timestamp="
                + timestamp + "]";
    }

    @Override
    public int compareTo(LogEntry o) {
        if(timestamp > o.timestamp) {
            return 1;
        } else {
            if(timestamp < o.timestamp) {
                return -1;
            }
        }

        if(id.equals(o.getId()) && state.equals(o.getState()))
            return 0;
        else
            return -1;
    }

    public String toStringForSQL() {
        return "('" + id + "'," + timestamp + "," + (type == null ? null : "'" + type + "'") + "," + (host == null ? null : "'" + host + "'") + ","
                + (timestamp > Main.ALERT_THRESHOLD && timestamp < 100000 ? true : false) + ")";
    }

}
