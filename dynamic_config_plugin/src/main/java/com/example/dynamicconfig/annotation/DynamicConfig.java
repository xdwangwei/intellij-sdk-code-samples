package com.example.dynamicconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态配置注解，用于标记需要动态配置的字段
 * 
 * @author Example
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicConfig {
    
    /**
     * 配置项名称，支持Spring配置表达式，如 "${app.name}"
     */
    String name();
    
    /**
     * 配置键，用于构建外部链接
     */
    String key();
}