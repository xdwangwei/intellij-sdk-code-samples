package com.example.dynamicconfig.reference;

import com.example.dynamicconfig.navigation.ExternalBrowserNavigation;
import com.example.dynamicconfig.util.ConfigPropertyResolver;
import com.example.dynamicconfig.util.UrlBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 键引用实现，用于 @DynamicConfig 注解的 key 参数
 */
public class KeyReference extends PsiReferenceBase<PsiLiteralExpression> {
    
    private final String keyValue;
    private final String propertyName;
    private final ConfigPropertyResolver resolver;
    
    public KeyReference(@NotNull PsiLiteralExpression element, @NotNull String keyValue, 
                       @NotNull String propertyName, @NotNull ConfigPropertyResolver resolver) {
        // 设置正确的TextRange，排除引号
        super(element, calculateTextRange(element));
        this.keyValue = keyValue;
        this.propertyName = propertyName;
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
        // 对于 key 参数，我们不解析到实际的 PSI 元素，而是返回空数组
        // 因为我们的导航行为是打开浏览器，而不是跳转到代码
        return new ResolveResult[0];
    }
    
    public @Nullable PsiElement resolve() {
        // 对于 key 参数，我们不解析到实际的 PSI 元素
        return null;
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
        return new Object[0]; // 暂不提供建议
    }
    
    public void navigate(boolean requestFocus) {
        Project project = getElement().getProject();
        List<ConfigPropertyResolver.ConfigProperty> properties = resolver.findPropertiesByKey(propertyName);
        
        if (properties.isEmpty()) {
            return;
        }
        
        if (properties.size() == 1) {
            // 只有一个匹配项，直接构建 URL
            ConfigPropertyResolver.ConfigProperty property = properties.get(0);
            String url = UrlBuilder.buildUrl(property.getKey(), keyValue);
            ExternalBrowserNavigation.openBrowser(url);
        } else {
            // 多个匹配项，显示选择对话框
            showPropertySelectionDialog(project, properties, keyValue);
        }
    }
    
    private void showPropertySelectionDialog(@NotNull Project project, 
                                            @NotNull List<ConfigPropertyResolver.ConfigProperty> properties,
                                            @NotNull String keyValue) {
        // 创建选择对话框
        String[] propertyNames = properties.stream()
            .map(prop -> prop.getKey() + " = " + prop.getValue() + " (" + prop.getFile().getName() + ")")
            .toArray(String[]::new);
        
        // 使用简单的选择对话框
        // 实际项目中可能需要更复杂的UI
        int selectedIndex = 0; // 默认选择第一个
        if (selectedIndex >= 0 && selectedIndex < properties.size()) {
            ConfigPropertyResolver.ConfigProperty property = properties.get(selectedIndex);
            String url = UrlBuilder.buildUrl(property.getKey(), keyValue);
            ExternalBrowserNavigation.openBrowser(url);
        }
    }
}