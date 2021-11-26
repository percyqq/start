package org.learn.binlogconnector.bean;

import lombok.Data;

@Data
public class DatabaseConfig {

    protected String hostname;

    protected Integer port;

    /**
     * schema
     */
    protected String database;

    protected String username;
    protected String password;

    protected String jdbcUrlParam;

    protected DatabaseConfig() {
    }

    public DatabaseConfig(String hostname, Integer port, String database, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String toJDBCurl() {
        return "jdbc:mysql://" + hostname + ":" + port + "/" + database;
    }


    public String unique() {
        return new StringBuilder(hostname).append(":").append(port).append("/").append(database).toString();
    }
}
