# 故障排除指南

## 插件功能不生效的问题排查

### 1. 检查插件是否已安装并启用

1. 打开 IntelliJ IDEA
2. 进入 `Settings` → `Plugins`
3. 确认 "Dynamic Config Navigator" 插件已安装且已启用
4. 如果未启用，请启用并重启 IDE

### 2. 检查注解类是否在项目中

插件需要能够找到 `@DynamicConfig` 注解类。请确保：

1. 注解类 `com.example.dynamicconfig.annotation.DynamicConfig` 在项目的类路径中
2. 或者注解类已经被编译到项目的 `target/classes` 或 `build/classes` 目录中
3. 如果使用 Maven/Gradle，确保注解类被正确打包

### 3. 检查代码使用方式

确保代码使用方式正确：

```java
import com.example.dynamicconfig.annotation.DynamicConfig;

public class TestConfig {
    @DynamicConfig(name = "${server.port}", key = "gcc")
    private String port;
}
```

**重要提示**：
- `name` 参数必须是 `${xxx}` 格式
- 注解必须使用完整的类名或已导入

### 4. 检查配置文件

确保项目中有配置文件：

1. 创建 `src/main/resources/application.yml` 文件
2. 添加配置项，例如：
   ```yaml
   server:
     port: 8080
   ```

### 5. 重新索引项目

有时项目索引可能有问题，尝试：

1. `File` → `Invalidate Caches / Restart...`
2. 选择 `Invalidate and Restart`
3. 等待重新索引完成

### 6. 检查 IDE 日志

查看 IDE 日志以获取错误信息：

1. `Help` → `Show Log in Explorer` (或 `Finder`)
2. 查看最新的日志文件
3. 搜索 "DynamicConfig" 或 "dynamicconfig" 相关的错误

### 7. 验证插件是否正确加载

1. 打开 `Help` → `Diagnostic Tools` → `Plugin Verification`
2. 检查是否有错误或警告

### 8. 测试步骤

1. 创建一个测试类：
   ```java
   import com.example.dynamicconfig.annotation.DynamicConfig;
   
   public class Test {
       @DynamicConfig(name = "${server.port}", key = "test")
       private String port;
   }
   ```

2. 创建配置文件 `src/main/resources/application.yml`：
   ```yaml
   server:
     port: 8080
   ```

3. 在代码中，按住 `Command` 键（Mac）或 `Ctrl` 键（Windows/Linux）
4. 鼠标移至 `"${server.port}"` 中的 `server.port` 部分
5. 应该能看到超链接，点击可跳转到配置文件

### 9. 常见问题

#### 问题：按住 Command 键时没有显示超链接

**可能原因**：
- 注解类不在类路径中
- 注解的 qualifiedName 检查失败
- 引用提供者没有被调用

**解决方案**：
- 确保注解类在项目中
- 检查注解是否使用了完整的包名
- 查看 IDE 日志中的错误信息

#### 问题：点击超链接没有反应

**可能原因**：
- 配置文件不存在
- 配置项不存在
- TextRange 计算不正确

**解决方案**：
- 确保配置文件存在
- 确保配置项名称正确
- 检查配置文件格式是否正确

### 10. 调试模式

如果需要调试，可以：

1. 在 `PropertyNameReferenceProvider.getReferencesByElement` 方法中添加日志
2. 使用 IntelliJ IDEA 的调试功能
3. 检查引用是否被创建

### 11. 联系支持

如果以上方法都无法解决问题，请：

1. 收集 IDE 日志
2. 记录复现步骤
3. 提供项目结构信息

