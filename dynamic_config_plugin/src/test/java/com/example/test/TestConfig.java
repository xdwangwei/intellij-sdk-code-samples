package com.example.test;

import com.example.dynamicconfig.annotation.DynamicConfig;

/**
 * 测试类，用于验证 DynamicConfig 插件功能
 */
public class TestConfig {
    
    @DynamicConfig(name = "${app.name}", key = "appName")
    private String appName;
    
    @DynamicConfig(name = "${database.connection.url}", key = "dbUrl")
    private String databaseUrl;
    
    @DynamicConfig(name = "${server.port}", key = "serverPort")
    private int port;
    
    // Getter和Setter方法
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getDatabaseUrl() {
        return databaseUrl;
    }
    
    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
}