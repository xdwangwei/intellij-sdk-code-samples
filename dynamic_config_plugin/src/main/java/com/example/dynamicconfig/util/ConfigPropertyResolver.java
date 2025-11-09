package com.example.dynamicconfig.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置属性解析器，负责解析项目中的配置文件并提供配置项查找功能
 */
public class ConfigPropertyResolver {
    
    private static final Logger LOG = Logger.getInstance(ConfigPropertyResolver.class);
    private final Project project;
    private final Map<String, List<ConfigProperty>> propertyCache = new ConcurrentHashMap<>();
    
    public ConfigPropertyResolver(Project project) {
        this.project = project;
        LOG.debug("ConfigPropertyResolver: 创建实例, project=" + project.getName());
        refreshCache();
    }
    
    /**
     * 根据配置键查找所有匹配的配置项
     */
    public List<ConfigProperty> findPropertiesByKey(@NotNull String key) {
        List<ConfigProperty> properties = propertyCache.getOrDefault(key, new ArrayList<>());
        LOG.debug("ConfigPropertyResolver.findPropertiesByKey: key=" + key + ", 找到 " + properties.size() + " 个匹配项");
        return properties;
    }
    
    /**
     * 获取所有配置键
     */
    public Collection<String> getAllPropertyKeys() {
        return propertyCache.keySet();
    }
    
    /**
     * 刷新配置缓存
     */
    public void refreshCache() {
        LOG.info("ConfigPropertyResolver.refreshCache: 开始刷新缓存");
        propertyCache.clear();
        
        try {
            // 查找所有YAML配置文件
            // 使用 FileTypeIndex.getFiles 来获取所有 YAML 文件
            // YAMLFileType.YML 是 YAML 插件提供的文件类型
            LOG.debug("ConfigPropertyResolver: 查找 YAML 文件");
            Collection<VirtualFile> yamlFiles = FileTypeIndex.getFiles(
                YAMLFileType.YML, 
                GlobalSearchScope.projectScope(project)
            );
            LOG.info("ConfigPropertyResolver: 找到 " + yamlFiles.size() + " 个 YAML 文件");
            
            int configFileCount = 0;
            for (VirtualFile file : yamlFiles) {
                LOG.debug("ConfigPropertyResolver: 检查文件=" + file.getName());
                if (isConfigFile(file)) {
                    LOG.info("ConfigPropertyResolver: 解析配置文件=" + file.getName());
                    parseConfigFile(file);
                    configFileCount++;
                }
            }
            LOG.info("ConfigPropertyResolver: 共解析 " + configFileCount + " 个配置文件");
            LOG.info("ConfigPropertyResolver: 缓存中的配置键数量=" + propertyCache.size());
        } catch (NoClassDefFoundError e) {
            LOG.error("ConfigPropertyResolver: YAML 插件未找到", e);
            // 如果 YAML 插件未加载或不可用，静默失败
            // 这样插件仍然可以在没有 YAML 支持的情况下工作
        } catch (Exception e) {
            LOG.error("ConfigPropertyResolver: 刷新缓存时出错", e);
        }
        
        // TODO: 添加对.properties文件的支持
    }
    
    /**
     * 判断是否为配置文件
     */
    private boolean isConfigFile(@NotNull VirtualFile file) {
        String fileName = file.getName();
        return fileName.startsWith("application") && 
               (fileName.endsWith(".yml") || fileName.endsWith(".yaml"));
    }
    
    /**
     * 解析配置文件
     */
    private void parseConfigFile(@NotNull VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) return;
        
        // 简化的YAML解析逻辑，支持基本的嵌套结构
        String content = psiFile.getText();
        String[] lines = content.split("\n");
        
        String currentPrefix = "";
        int currentIndent = 0;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmedLine = line.trim();
            
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) continue;
            
            // 计算缩进级别
            int indent = 0;
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == ' ') {
                    indent++;
                } else {
                    break;
                }
            }
            
            // 处理缩进变化
            if (indent < currentIndent) {
                // 减少缩进，回退到上一级
                while (currentIndent > indent) {
                    int lastDot = currentPrefix.lastIndexOf('.');
                    if (lastDot > 0) {
                        currentPrefix = currentPrefix.substring(0, lastDot);
                    } else {
                        currentPrefix = "";
                    }
                    currentIndent -= 2; // 假设每级缩进为2个空格
                }
            }
            
            int colonIndex = trimmedLine.indexOf(':');
            if (colonIndex > 0) {
                String key = trimmedLine.substring(0, colonIndex).trim();
                String value = trimmedLine.substring(colonIndex + 1).trim();
                
                // 构建完整键名
                String fullKey = currentPrefix.isEmpty() ? key : currentPrefix + "." + key;
                
                // 移除引号
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                
                // 如果有值，则添加到缓存
                if (!value.isEmpty()) {
                    ConfigProperty property = new ConfigProperty(fullKey, value, psiFile, i + 1);
                    propertyCache.computeIfAbsent(fullKey, k -> new ArrayList<>()).add(property);
                }
                
                // 更新当前前缀和缩进
                if (value.isEmpty()) {
                    // 这是一个嵌套对象
                    currentPrefix = currentPrefix.isEmpty() ? key : currentPrefix + "." + key;
                    currentIndent = indent;
                }
            } else if (trimmedLine.startsWith("- ")) {
                // 处理数组项
                String arrayValue = trimmedLine.substring(2).trim();
                
                // 移除引号
                if (arrayValue.startsWith("\"") && arrayValue.endsWith("\"")) {
                    arrayValue = arrayValue.substring(1, arrayValue.length() - 1);
                }
                
                // 为数组项创建键名
                String arrayKey = currentPrefix + "[0]"; // 简化处理，只处理第一个元素
                ConfigProperty property = new ConfigProperty(arrayKey, arrayValue, psiFile, i + 1);
                propertyCache.computeIfAbsent(arrayKey, k -> new ArrayList<>()).add(property);
            }
        }
    }
    
    /**
     * 配置属性类，表示一个配置项
     */
    public static class ConfigProperty {
        private final String key;
        private final String value;
        private final PsiFile file;
        private final int lineNumber;
        
        public ConfigProperty(@NotNull String key, @NotNull String value, @NotNull PsiFile file, int lineNumber) {
            this.key = key;
            this.value = value;
            this.file = file;
            this.lineNumber = lineNumber;
        }
        
        public String getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }
        
        public PsiFile getFile() {
            return file;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public PsiElement getPsiElement() {
            // 尝试找到包含该 key 的 PSI 元素
            // 首先尝试通过行号定位
            if (lineNumber > 0) {
                try {
                    String fileText = file.getText();
                    String[] lines = fileText.split("\n");
                    if (lineNumber <= lines.length) {
                        String line = lines[lineNumber - 1];
                        // 在行中查找 key
                        int keyIndex = line.indexOf(key + ":");
                        if (keyIndex >= 0) {
                            // 计算在文件中的偏移量
                            int offset = 0;
                            for (int i = 0; i < lineNumber - 1; i++) {
                                offset += lines[i].length() + 1; // +1 for newline
                            }
                            offset += keyIndex;
                            PsiElement element = file.findElementAt(offset);
                            if (element != null) {
                                return element;
                            }
                        }
                    }
                } catch (Exception e) {
                    // 如果出错，使用 fallback 方法
                }
            }
            
            // Fallback: 在整个文件中查找 key
            int index = file.getText().indexOf(key + ":");
            if (index >= 0) {
                PsiElement element = file.findElementAt(index);
                if (element != null) {
                    return element;
                }
            }
            
            // 如果都找不到，返回文件本身
            return file;
        }
    }
}