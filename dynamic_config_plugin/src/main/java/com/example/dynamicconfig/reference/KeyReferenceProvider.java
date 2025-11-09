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
    
    private static final String DYNAMIC_CONFIG_ANNOTATION = "com.example.dynamicconfig.annotation.DynamicConfig";
    
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (!(element instanceof PsiLiteralExpression)) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        // 验证是否在 @DynamicConfig 注解中
        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
        if (annotation == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        // 检查注解名称（支持完整限定名和简单名称）
        String qualifiedName = annotation.getQualifiedName();
        if (qualifiedName == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        // 支持完整限定名和简单名称（如果使用了导入）
        boolean isDynamicConfig = DYNAMIC_CONFIG_ANNOTATION.equals(qualifiedName) 
            || qualifiedName.endsWith(".DynamicConfig") 
            || "DynamicConfig".equals(qualifiedName);
        
        if (!isDynamicConfig) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        // 验证是否是 key 参数
        PsiNameValuePair nameValuePair = PsiTreeUtil.getParentOfType(element, PsiNameValuePair.class);
        if (nameValuePair == null || !"key".equals(nameValuePair.getName())) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
        Object value = literalExpression.getValue();
        
        if (!(value instanceof String)) {
            return PsiReference.EMPTY_ARRAY;
        }
        
        String keyValue = (String) value;
        
        // 获取对应的 name 参数值
        String propertyName = extractPropertyName(annotation);
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
    private String extractPropertyName(@NotNull PsiAnnotation annotation) {
        // 获取 name 参数
        PsiAnnotationMemberValue nameValue = annotation.findAttributeValue("name");
        if (nameValue instanceof PsiLiteralExpression) {
            Object value = ((PsiLiteralExpression) nameValue).getValue();
            if (value instanceof String) {
                String nameStr = (String) value;
                // 处理 ${xxx} 格式的表达式
                if (nameStr.startsWith("${") && nameStr.endsWith("}")) {
                    // 处理 ${xxx} 或 ${xxx:default} 格式
                    nameStr = nameStr.substring(2, nameStr.length() - 1);
                    // 处理默认值
                    int colonIndex = nameStr.indexOf(':');
                    if (colonIndex > 0) {
                        nameStr = nameStr.substring(0, colonIndex);
                    }
                    return nameStr;
                }
            }
        }
        
        return null;
    }
}