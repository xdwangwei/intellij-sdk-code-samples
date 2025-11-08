# Dynamic Config Plugin

## 功能概述

Dynamic Config Plugin 是一个 IntelliJ IDEA 插件，为 Spring/Spring Boot 项目中的自定义 `@DynamicConfig` 注解提供增强的导航功能。

### 主要功能

1. **配置属性导航**：
   - 对于 `@DynamicConfig(name = "${xxx}", key = "yyy")` 注解的字段
   - 按住 Command 键，鼠标移至 "xxx" 时显示超链接
   - 点击超链接可定位到对应的配置文件（application-xxx.yml）
   - 如果存在多个匹配的配置文件，会弹出选择对话框

2. **浏览器导航**：
   - 按住 Command 键，鼠标移至 "yyy" 时显示超链接
   - 点击超链接会根据 xxx 对应的配置项数量构建 URL
   - 单个配置项：直接拼接 URL 并打开浏览器访问 baidu.com?xxx-yyy
   - 多个配置项：弹出选择对话框，选择后拼接 URL 并打开浏览器

## 技术实现

### 核心组件

1. **注解定义**：
   - `@DynamicConfig` 注解，包含 `name` 和 `key` 两个属性

2. **引用系统**：
   - `DynamicConfigReferenceContributor`：引用贡献者，注册引用提供者
   - `PropertyNameReference` 和 `PropertyNameReferenceProvider`：处理 name 参数的引用
   - `KeyReference` 和 `KeyReferenceProvider`：处理 key 参数的引用

3. **配置解析**：
   - `ConfigPropertyResolver`：解析项目中的 YAML 配置文件，查找配置属性

4. **导航工具**：
   - `UrlBuilder`：构建导航 URL
   - `ExternalBrowserNavigation`：打开外部浏览器

## 使用示例

### 1. 配置文件示例

```yaml
# application-dev.yml
app:
  name: "Development App"
  version: "1.0.0"

database:
  connection:
    url: "jdbc:mysql://localhost:3306/dev_db"
    username: "dev_user"
    password: "dev_password"

servers:
  - host: "dev-server1.example.com"
    port: 8080
  - host: "dev-server2.example.com"
    port: 8081

feature:
  flags:
    new-ui: true
    advanced-search: false

security:
  oauth:
    client-id: "dev-client-id"
    client-secret: "dev-client-secret"

server:
  port: 8080
```

### 2. Java 代码示例

```java
public class AppConfig {
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
}
```

## 安装和使用

1. 构建插件：
   ```bash
   # 设置Java环境变量（需要Java 17或更高版本）
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   
   # 构建插件（跳过instrumentCode任务以避免路径问题）
   ./gradlew buildPlugin -x instrumentCode
   ```

   注意：如果在构建过程中遇到 `/Packages does not exist` 错误，请使用 `-x instrumentCode` 参数跳过instrumentCode任务。

2. 安装插件：
   - 在 IntelliJ IDEA 中打开 `Settings` -> `Plugins`
   - 点击齿轮图标，选择 `Install Plugin from Disk`
   - 选择构建的插件包

3. 使用插件：
   - 在 Spring/Spring Boot 项目中使用 `@DynamicConfig` 注解
   - 按住 Command 键，鼠标移至注解参数值上
   - 点击显示的超链接进行导航

## 开发说明

### 项目结构

```
dynamic_config_plugin/
├── build.gradle.kts                    # 构建配置
├── src/main/resources/META-INF/
│   └── plugin.xml                      # 插件配置
└── src/main/java/com/example/dynamicconfig/
    ├── annotation/
    │   └── DynamicConfig.java          # 自定义注解定义
    ├── reference/
    │   ├── DynamicConfigReferenceContributor.java
    │   ├── KeyReference.java
    │   ├── KeyReferenceProvider.java
    │   ├── PropertyNameReference.java
    │   └── PropertyNameReferenceProvider.java
    ├── util/
    │   ├── ConfigPropertyResolver.java
    │   └── UrlBuilder.java
    ├── navigation/
    │   └── ExternalBrowserNavigation.java
    └── test/
        └── DynamicConfigTest.java       # 测试用例
```

### 扩展功能

1. 支持更多配置文件格式（如 .properties）
2. 添加配置属性的自动补全功能
3. 支持更复杂的配置表达式解析
4. 添加配置属性的验证功能

## 许可证

本插件基于 Apache 2.0 许可证开源。