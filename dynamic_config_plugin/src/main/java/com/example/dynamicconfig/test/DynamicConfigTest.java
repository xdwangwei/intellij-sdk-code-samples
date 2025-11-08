package com.example.dynamicconfig.test;

import com.example.dynamicconfig.annotation.DynamicConfig;

/**
 * 测试用例类，用于演示 DynamicConfig 注解的使用
 */
public class DynamicConfigTest {
    
    // 基本用法：name 参数引用配置文件中的属性
    @DynamicConfig(name = "${app.name}", key = "appName")
    private String appName;
    
    // 基本用法：name 参数引用嵌套属性
    @DynamicConfig(name = "${database.connection.url}", key = "dbUrl")
    private String databaseUrl;
    
    // 基本用法：name 参数引用数组属性
    @DynamicConfig(name = "${servers[0].host}", key = "serverHost")
    private String serverHost;
    
    // 复杂用法：name 参数引用复杂表达式
    @DynamicConfig(name = "${feature.flags.new-ui}", key = "newUIFlag")
    private boolean newUIEnabled;
    
    // 多个相同属性名的不同键
    @DynamicConfig(name = "${app.name}", key = "displayName")
    private String displayName;
    
    // 嵌套配置引用
    @DynamicConfig(name = "${security.oauth.client-id}", key = "oauthClientId")
    private String clientId;
    
    // 端口配置引用
    @DynamicConfig(name = "${server.port}", key = "serverPort")
    private int port;
    
    // 获取配置值的方法示例
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
    
    public String getServerHost() {
        return serverHost;
    }
    
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }
    
    public boolean isNewUIEnabled() {
        return newUIEnabled;
    }
    
    public void setNewUIEnabled(boolean newUIEnabled) {
        this.newUIEnabled = newUIEnabled;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
}