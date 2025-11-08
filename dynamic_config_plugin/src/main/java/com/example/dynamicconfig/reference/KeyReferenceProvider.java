package com.example.dynamicconfig.reference;

import com.example.dynamicconfig.util.ConfigPropertyResolver;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * 键引用提供者，用于 @DynamicConfig 注解的 key 参数
 */
public class KeyReferenceProvider extends PsiReferenceProvider {
    
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (!(element instanceof PsiLiteralExpression)) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
        Object value = literalExpression.getValue();
        
        if (!(value instanceof String)) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        String keyValue = (String) value;
        
        // 获取对应的 name 参数值
        String propertyName = extractPropertyName(element);
        if (propertyName == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        Project project = element.getProject();
        ConfigPropertyResolver resolver = new ConfigPropertyResolver(project);
        
        return new PsiReference[] { new KeyReference(literalExpression, keyValue, propertyName, resolver) };
    }
    
    /**
     * 从注解中提取 name 参数的值
     */
    private String extractPropertyName(@NotNull PsiElement element) {
        // 获取注解元素
        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
        if (annotation == null) {
            return null;
        }
        
        // 检查注解是否是 @DynamicConfig
        if (!annotation.getQualifiedName().equals("com.example.dynamicconfig.annotation.DynamicConfig")) {
            return null;
        }
        
        // 获取 name 参数
        PsiAnnotationMemberValue nameValue = annotation.findAttributeValue("name");
        if (nameValue instanceof PsiLiteralExpression) {
            Object value = ((PsiLiteralExpression) nameValue).getValue();
            if (value instanceof String) {
                String nameStr = (String) value;
                // 处理 ${xxx} 格式的表达式
                if (nameStr.startsWith("$") && nameStr.endsWith("}")) {
                    // 处理 ${xxx} 或 ${xxx:default} 格式
                    int startIndex = nameStr.indexOf('{');
                    int endIndex = nameStr.lastIndexOf('}');
                    if (startIndex > 0 && endIndex > startIndex) {
                        nameStr = nameStr.substring(startIndex + 1, endIndex);
                        // 处理默认值
                        int colonIndex = nameStr.indexOf(':');
                        if (colonIndex > 0) {
                            nameStr = nameStr.substring(0, colonIndex);
                        }
                    }
                }
                return nameStr;
            }
        }
        
        return null;
    }
}