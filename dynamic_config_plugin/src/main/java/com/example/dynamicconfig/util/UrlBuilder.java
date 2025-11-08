package com.example.dynamicconfig.util;

/**
 * URL 构建工具类，用于构建导航 URL
 */
public class UrlBuilder {
    
    private static final String BASE_URL = "https://baidu.com";
    
    /**
     * 构建导航 URL
     * 
     * @param propertyName 属性名
     * @param keyValue 键值
     * @return 构建的 URL
     */
    public static String buildUrl(String propertyName, String keyValue) {
        if (propertyName == null || propertyName.isEmpty()) {
            return BASE_URL;
        }
        
        if (keyValue == null || keyValue.isEmpty()) {
            return BASE_URL + "?" + propertyName;
        }
        
        return BASE_URL + "?" + propertyName + "-" + keyValue;
    }
    
    /**
     * 构建导航 URL（带路径）
     * 
     * @param propertyName 属性名
     * @param keyValue 键值
     * @param path URL 路径
     * @return 构建的 URL
     */
    public static String buildUrl(String propertyName, String keyValue, String path) {
        StringBuilder url = new StringBuilder(BASE_URL);
        
        if (path != null && !path.isEmpty()) {
            if (!path.startsWith("/")) {
                url.append("/");
            }
            url.append(path);
        }
        
        url.append("?");
        
        if (propertyName != null && !propertyName.isEmpty()) {
            url.append(propertyName);
            
            if (keyValue != null && !keyValue.isEmpty()) {
                url.append("-").append(keyValue);
            }
        } else if (keyValue != null && !keyValue.isEmpty()) {
            url.append(keyValue);
        }
        
        return url.toString();
    }
}