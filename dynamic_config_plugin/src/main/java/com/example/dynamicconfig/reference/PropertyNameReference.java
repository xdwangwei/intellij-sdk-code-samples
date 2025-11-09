package com.example.dynamicconfig.reference;

import com.example.dynamicconfig.util.ConfigPropertyResolver;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.IncorrectOperationException;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 属性名引用实现，用于 @DynamicConfig 注解的 name 参数
 */
public class PropertyNameReference extends PsiReferenceBase<PsiLiteralExpression> {
    
    private static final Logger LOG = Logger.getInstance(PropertyNameReference.class);
    private final String propertyKey;
    private final ConfigPropertyResolver resolver;
    
    public PropertyNameReference(@NotNull PsiLiteralExpression element, @NotNull String propertyKey, 
                                @NotNull ConfigPropertyResolver resolver) {
        // 设置正确的TextRange，排除引号
        super(element, calculateTextRange(element));
        this.propertyKey = propertyKey;
        this.resolver = resolver;
        TextRange range = calculateTextRange(element);
        LOG.info("PropertyNameReference: 创建引用, propertyKey=" + propertyKey + ", range=" + range);
    }
    
    /**
     * 计算正确的文本范围，只高亮 ${xxx} 中的 xxx 部分
     */
    private static TextRange calculateTextRange(@NotNull PsiLiteralExpression element) {
        String fullText = element.getText();
        String innerText = fullText;
        
        // 处理引号，获取内部文本
        if (fullText.startsWith("\"") && fullText.endsWith("\"")) {
            innerText = fullText.substring(1, fullText.length() - 1);
        }
        
        // 处理 ${xxx} 格式，计算 xxx 部分的偏移
        if (innerText.startsWith("${") && innerText.endsWith("}")) {
            int quoteOffset = fullText.startsWith("\"") ? 1 : 0;
            int startOffset = quoteOffset + 2; // 跳过 "${
            
            // 处理默认值，如 ${xxx:default}
            String content = innerText.substring(2, innerText.length() - 1);
            int colonIndex = content.indexOf(':');
            int contentLength = colonIndex > 0 ? colonIndex : content.length();
            
            int endOffset = startOffset + contentLength;
            return new TextRange(startOffset, endOffset);
        }
        
        // 如果不是 ${xxx} 格式，高亮整个内容（排除引号）
        if (fullText.startsWith("\"") && fullText.endsWith("\"")) {
            return new TextRange(1, fullText.length() - 1);
        }
        return new TextRange(0, fullText.length());
    }
    
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        LOG.debug("PropertyNameReference.multiResolve: propertyKey=" + propertyKey);
        List<ConfigPropertyResolver.ConfigProperty> properties = resolver.findPropertiesByKey(propertyKey);
        LOG.debug("PropertyNameReference.multiResolve: 找到 " + properties.size() + " 个匹配项");
        
        if (properties.isEmpty()) {
            // 即使没有找到配置属性，也返回空数组
            // 引用仍然会被创建，但 resolve() 会返回 null
            LOG.debug("PropertyNameReference.multiResolve: 没有找到匹配项");
            return ResolveResult.EMPTY_ARRAY;
        }
        
        ResolveResult[] results = properties.stream()
            .map(prop -> {
                PsiElement element = prop.getPsiElement();
                return element != null ? new PsiElementResolveResult(element) : null;
            })
            .filter(result -> result != null)
            .toArray(ResolveResult[]::new);
        
        LOG.debug("PropertyNameReference.multiResolve: 返回 " + results.length + " 个结果");
        return results;
    }
    
    public @Nullable PsiElement resolve() {
        LOG.debug("PropertyNameReference.resolve: propertyKey=" + propertyKey);
        ResolveResult[] results = multiResolve(false);
        if (results.length == 0) {
            LOG.debug("PropertyNameReference.resolve: 没有解析结果");
            return null;
        }
        // 如果只有一个结果，返回它；如果有多个，返回第一个
        PsiElement resolved = results[0].getElement();
        LOG.info("PropertyNameReference.resolve: 解析到元素=" + (resolved != null ? resolved.getClass().getName() : "null"));
        return resolved;
    }
    
    public boolean isSoft() {
        // 标记为软引用，这样即使找不到配置属性也可以显示超链接
        // 不会在代码检查时报错
        return true;
    }
    
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        // 处理重命名操作
        return super.handleElementRename(newElementName);
    }
    
    public Object @NotNull [] getVariants() {
        // 提供自动完成建议
        return resolver.getAllPropertyKeys().toArray();
    }
    
    public void navigate(boolean requestFocus) {
        LOG.info("PropertyNameReference.navigate: 开始导航, propertyKey=" + propertyKey);
        Project project = getElement().getProject();
        List<ConfigPropertyResolver.ConfigProperty> properties = resolver.findPropertiesByKey(propertyKey);
        LOG.info("PropertyNameReference.navigate: 找到 " + properties.size() + " 个匹配项");
        
        if (properties.isEmpty()) {
            LOG.warn("PropertyNameReference.navigate: 没有找到匹配项，无法导航");
            return;
        }
        
        if (properties.size() == 1) {
            // 只有一个匹配项，直接跳转
            LOG.info("PropertyNameReference.navigate: 只有一个匹配项，直接跳转");
            ConfigPropertyResolver.ConfigProperty property = properties.get(0);
            navigateToProperty(project, property, requestFocus);
        } else {
            // 多个匹配项，显示选择对话框
            LOG.info("PropertyNameReference.navigate: 多个匹配项，显示选择对话框");
            showPropertySelectionDialog(project, properties, requestFocus);
        }
    }
    
    private void navigateToProperty(@NotNull Project project, @NotNull ConfigPropertyResolver.ConfigProperty property, 
                                   boolean requestFocus) {
        // 使用 NavigationItem 接口的 navigate 方法
        PsiElement element = property.getPsiElement();
        if (element instanceof NavigationItem) {
            ((NavigationItem) element).navigate(requestFocus);
        } else {
            // 如果不是 NavigationItem，尝试使用 OpenFileDescriptor
            PsiFile file = property.getFile();
            int lineNumber = property.getLineNumber();
            if (file != null && file.getVirtualFile() != null) {
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file.getVirtualFile(), lineNumber - 1, 0);
                FileEditorManager.getInstance(project).openTextEditor(descriptor, requestFocus);
            }
        }
    }
    
    private void showPropertySelectionDialog(@NotNull Project project, 
                                            @NotNull List<ConfigPropertyResolver.ConfigProperty> properties, 
                                            boolean requestFocus) {
        // 创建选择对话框
        String[] propertyNames = properties.stream()
            .map(prop -> prop.getKey() + " = " + prop.getValue() + " (" + prop.getFile().getName() + ")")
            .toArray(String[]::new);
        
        // 使用简单的选择对话框
        // 实际项目中可能需要更复杂的UI
        int selectedIndex = 0; // 默认选择第一个
        if (selectedIndex >= 0 && selectedIndex < properties.size()) {
            navigateToProperty(project, properties.get(selectedIndex), requestFocus);
        }
    }
}