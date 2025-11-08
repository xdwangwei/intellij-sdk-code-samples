package com.example.test;

import com.example.dynamicconfig.annotation.DynamicConfig;

/**
 * 测试DynamicConfig注解的完整功能
 */
public class CompleteTestConfig {
    
    // 测试简单的配置属性引用
    @DynamicConfig(name = "${app.name}")
    private String appName;
    
    // 测试嵌套配置属性引用
    @DynamicConfig(name = "${spring.datasource.url}")
    private String datasourceUrl;
    
    // 测试带默认值的配置属性引用
    @DynamicConfig(name = "${app.version:1.0.0}")
    private String appVersion;
    
    // 测试服务器配置引用
    @DynamicConfig(name = "${server.port}")
    private int serverPort;
    
    // 测试带key属性的配置引用
    @DynamicConfig(name = "${app.name}", key = "app-name-docs")
    private String appNameWithKey;
    
    // 测试嵌套配置和key的组合
    @DynamicConfig(name = "${spring.datasource.username}", key = "db-username-docs")
    private String dbUsername;
    
    // Getters and Setters
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getDatasourceUrl() {
        return datasourceUrl;
    }
    
    public void setDatasourceUrl(String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    
    public String getAppNameWithKey() {
        return appNameWithKey;
    }
    
    public void setAppNameWithKey(String appNameWithKey) {
        this.appNameWithKey = appNameWithKey;
    }
    
    public String getDbUsername() {
        return dbUsername;
    }
    
    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }
}