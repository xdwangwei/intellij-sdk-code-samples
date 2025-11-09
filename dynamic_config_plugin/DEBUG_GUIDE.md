# 调试指南 - 使用日志定位问题

## 概述

我已经在插件的关键位置添加了详细的日志，可以帮助我们定位问题所在。

## 如何查看日志

### 方法 1: 通过 IDE 菜单查看

1. 打开 IntelliJ IDEA
2. 进入 `Help` → `Show Log in Explorer` (Mac) 或 `Help` → `Show Log in Files` (Windows/Linux)
3. 打开最新的日志文件（通常是 `idea.log`）

### 方法 2: 直接查看日志目录

**Mac:**
```
~/Library/Logs/JetBrains/IntelliJIdea<version>/
```

**Windows:**
```
%USERPROFILE%\.IntelliJIdea<version>\system\log\
```

**Linux:**
```
~/.cache/JetBrains/IntelliJIdea<version>/log/
```

## 日志关键词

在日志文件中搜索以下关键词来查找相关日志：

### 1. 插件加载日志
搜索：`DynamicConfigReferenceContributor`
- 应该看到 "开始注册引用提供者"
- 应该看到 "已注册 PropertyNameReferenceProvider"
- 应该看到 "已注册 KeyReferenceProvider"

### 2. 引用提供者调用日志
搜索：`PropertyNameReferenceProvider`
- 应该看到 "被调用" 日志
- 应该看到字面量值的日志
- 应该看到注解检查结果的日志

### 3. 注解检查日志
搜索：`isInDynamicConfigAnnotation`
- 应该看到注解文本
- 应该看到 qualifiedName
- 应该看到参数名称

### 4. 配置文件解析日志
搜索：`ConfigPropertyResolver`
- 应该看到 "开始刷新缓存"
- 应该看到找到的 YAML 文件数量
- 应该看到解析的配置文件数量
- 应该看到缓存中的配置键数量

### 5. 引用创建和解析日志
搜索：`PropertyNameReference`
- 应该看到 "创建引用" 日志
- 应该看到 "解析到元素" 日志
- 应该看到导航日志

## 调试步骤

### 步骤 1: 检查插件是否加载

1. 重启 IntelliJ IDEA
2. 打开日志文件
3. 搜索 `DynamicConfigReferenceContributor`
4. **预期结果**: 应该看到注册日志

**如果没有看到日志**:
- 插件可能没有正确安装
- 插件可能没有启用
- 检查 `Settings` → `Plugins` → `Dynamic Config Navigator` 是否启用

### 步骤 2: 检查引用提供者是否被调用

1. 在代码中使用 `@DynamicConfig` 注解
2. 将鼠标移至注解参数上（按住 Command/Ctrl 键）
3. 查看日志文件
4. 搜索 `PropertyNameReferenceProvider.getReferencesByElement`
5. **预期结果**: 应该看到 "被调用" 日志

**如果没有看到日志**:
- 模式匹配可能不正确
- 引用提供者可能没有被触发
- 检查代码中是否使用了正确的注解格式

### 步骤 3: 检查注解识别

1. 查看日志中的 `isInDynamicConfigAnnotation` 相关日志
2. **关键信息**:
   - `qualifiedName`: 应该包含 "DynamicConfig"
   - `参数名称`: 应该是 "name"
   - `是否在 name 参数中`: 应该是 true

**如果 qualifiedName 为 null**:
- 注解类可能不在类路径中
- 注解可能还没有被编译
- 检查注解类是否在项目中

**如果 qualifiedName 不正确**:
- 注解的完整限定名可能不匹配
- 检查注解类的包名是否正确

### 步骤 4: 检查配置文件解析

1. 查看日志中的 `ConfigPropertyResolver` 相关日志
2. **关键信息**:
   - `找到 X 个 YAML 文件`: 应该 > 0
   - `解析配置文件`: 应该看到配置文件名称
   - `缓存中的配置键数量`: 应该 > 0

**如果没有找到 YAML 文件**:
- 项目中可能没有 YAML 文件
- YAML 插件可能没有启用
- 检查 `Settings` → `Plugins` → `YAML` 是否启用

**如果配置文件没有被解析**:
- 配置文件名称可能不符合规则（需要以 `application` 开头）
- 检查配置文件是否在项目根目录或 `src/main/resources` 目录中

### 步骤 5: 检查引用解析

1. 查看日志中的 `PropertyNameReference.resolve` 相关日志
2. **关键信息**:
   - `找到 X 个匹配项`: 应该 > 0（如果配置存在）
   - `解析到元素`: 应该不为 null（如果配置存在）

**如果没有找到匹配项**:
- 配置键名称可能不匹配
- 检查配置文件中的键名是否与代码中的键名一致
- 检查 YAML 文件的格式是否正确

## 常见问题排查

### 问题 1: 没有看到任何日志

**可能原因**:
1. 日志级别设置过高（DEBUG 日志可能被过滤）
2. 插件没有正确加载
3. 日志文件位置不正确

**解决方案**:
1. 在日志文件中搜索 `INFO` 级别的日志（使用 `LOG.info` 的日志）
2. 检查插件是否启用
3. 确认日志文件路径正确

### 问题 2: 看到 "被调用" 日志，但没有创建引用

**可能原因**:
1. 注解检查失败
2. 格式检查失败（不是 `${xxx}` 格式）
3. 值不是字符串类型

**解决方案**:
1. 查看 `isInDynamicConfigAnnotation` 的日志，检查哪里失败
2. 检查代码中的注解格式是否正确
3. 检查参数值是否是字符串字面量

### 问题 3: 看到引用创建日志，但点击没有反应

**可能原因**:
1. 配置文件没有被找到
2. 配置键不匹配
3. 导航逻辑有问题

**解决方案**:
1. 查看 `ConfigPropertyResolver` 的日志，检查配置文件是否被解析
2. 查看 `PropertyNameReference.resolve` 的日志，检查是否找到匹配项
3. 查看 `PropertyNameReference.navigate` 的日志，检查导航是否被调用

## 日志级别说明

- **INFO**: 重要事件，如插件加载、引用创建、配置文件解析
- **DEBUG**: 详细信息，如方法调用、参数值、检查结果
- **WARN**: 警告信息，如找不到匹配项
- **ERROR**: 错误信息，如异常堆栈

## 下一步

根据日志信息，我们可以精确定位问题所在。请：

1. 安装新版本的插件
2. 重启 IntelliJ IDEA
3. 尝试使用插件功能
4. 查看日志文件
5. 将相关日志信息发送给我，以便进一步分析

## 新插件包位置

```
/Users/vivi/Workspace/git/github/intellij-sdk-code-samples/dynamic_config_plugin/build/distributions/dynamic_config_plugin-1.0.0.zip
```

