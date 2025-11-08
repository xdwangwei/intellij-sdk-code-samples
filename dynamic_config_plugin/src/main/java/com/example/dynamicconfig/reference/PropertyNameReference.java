package com.example.dynamicconfig.reference;

import com.example.dynamicconfig.util.ConfigPropertyResolver;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ide.util.EditSourceUtil;
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
    
    private final String propertyKey;
    private final ConfigPropertyResolver resolver;
    
    public PropertyNameReference(@NotNull PsiLiteralExpression element, @NotNull String propertyKey, 
                                @NotNull ConfigPropertyResolver resolver) {
        // 设置正确的TextRange，排除引号
        super(element, calculateTextRange(element));
        this.propertyKey = propertyKey;
        this.resolver = resolver;
    }
    
    /**
     * 计算正确的文本范围，排除字符串的引号
     */
    private static TextRange calculateTextRange(@NotNull PsiLiteralExpression element) {
        String text = element.getText();
        if (text.startsWith("\"") && text.endsWith("\"")) {
            return new TextRange(1, text.length() - 1);
        }
        return new TextRange(0, text.length());
    }
    
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        String value = getElement().getText();
        
        // 去除引号
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        
        // 处理${xxx}格式的表达式
        if (value.startsWith("${") && value.endsWith("}")) {
            value = value.substring(2, value.length() - 1);
        }
        
        // 处理默认值，如${xxx:default}
        int colonIndex = value.indexOf(':');
        if (colonIndex > 0) {
            value = value.substring(0, colonIndex);
        }
        
        List<ConfigPropertyResolver.ConfigProperty> properties = resolver.findPropertiesByKey(value);
        if (properties.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }
        
        return properties.stream()
            .map(prop -> new PsiElementResolveResult(prop.getPsiElement()))
            .toArray(ResolveResult[]::new);
    }
    
    public @Nullable PsiElement resolve() {
        ResolveResult[] results = multiResolve(false);
        return results.length == 1 ? results[0].getElement() : null;
    }
    
    public boolean isSoft() {
        return false;
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
        Project project = getElement().getProject();
        List<ConfigPropertyResolver.ConfigProperty> properties = resolver.findPropertiesByKey(propertyKey);
        
        if (properties.isEmpty()) {
            return;
        }
        
        if (properties.size() == 1) {
            // 只有一个匹配项，直接跳转
            ConfigPropertyResolver.ConfigProperty property = properties.get(0);
            navigateToProperty(project, property, requestFocus);
        } else {
            // 多个匹配项，显示选择对话框
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