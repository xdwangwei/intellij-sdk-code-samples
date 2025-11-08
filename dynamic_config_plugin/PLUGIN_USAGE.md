# Dynamic Config Plugin 使用指南

## 插件功能

Dynamic Config Plugin 是一个 IntelliJ IDEA 插件，为 Spring 项目中的 @DynamicConfig 注解提供配置属性导航功能。

## 安装方法

1. 下载 `build/distributions/dynamic_config_plugin-1.0.0.zip` 文件
2. 在 IntelliJ IDEA 中，打开 `Settings/Preferences` → `Plugins`
3. 点击齿轮图标 → `Install Plugin from Disk...`
4. 选择下载的 zip 文件并安装
5. 重启 IntelliJ IDEA

## 测试步骤

1. 创建一个新的 Spring 项目或打开现有项目
2. 在项目的 `src/main/resources` 目录下创建 `application.yml` 文件
3. 添加以下配置内容：

```yaml
app:
  name: "DynamicConfigApp"
  version: "2.0.0"
  
spring:
  datasource:
    url: "jdbc:mysql://localhost:3306/testdb"
    username: "admin"
    password: "secret"
    
server:
  port: 8080
```

4. 在项目中创建一个 Java 类，使用 @DynamicConfig 注解：

```java
import com.example.dynamicconfig.annotation.DynamicConfig;

public class TestConfig {
    
    @DynamicConfig(name = "${app.name}")
    private String appName;
    
    @DynamicConfig(name = "${spring.datasource.url}")
    private String datasourceUrl;
    
    @DynamicConfig(name = "${server.port}")
    private int serverPort;
    
    // Getters and Setters
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    // ... 其他 getter 和 setter 方法
}
```

5. 测试插件功能：
   - 按住 Ctrl/Cmd 并将鼠标悬停在注解中的字符串值上
   - 点击字符串值，应该能够跳转到配置文件中对应的属性
   - 如果有多个匹配项，会显示选择对话框

## 功能说明

1. **配置属性导航**：从 @DynamicConfig 注解的 name 属性值跳转到配置文件中对应的属性
2. **浏览器导航**：从 @DynamicConfig 注解的 key 属性值打开外部链接
3. **自动完成建议**：在输入配置属性名称时提供建议
4. **重命名支持**：重命名配置属性时，会更新所有引用

## 支持的配置格式

- YAML 文件 (.yml, .yaml)
- 计划支持 Properties 文件 (.properties)

## 注意事项

1. 确保项目中有 Spring 相关依赖
2. 配置文件应位于 `src/main/resources` 目录下
3. 插件目前支持基本的嵌套配置结构

## 故障排除

如果插件没有生效：

1. 检查是否正确安装并重启了 IntelliJ IDEA
2. 确认项目中包含了 Spring 相关依赖
3. 确认配置文件位置和格式正确
4. 尝试重新构建项目索引：`File` → `Invalidate Caches / Restart...` → `Invalidate and Restart`