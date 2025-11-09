package com.example.dynamicconfig.reference;

import com.example.dynamicconfig.util.ConfigPropertyResolver;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * 属性名引用提供者，用于 @DynamicConfig 注解的 name 参数
 */
public class PropertyNameReferenceProvider extends PsiReferenceProvider {
    
    private static final Logger LOG = Logger.getInstance(PropertyNameReferenceProvider.class);
    private static final String DYNAMIC_CONFIG_ANNOTATION = "com.example.dynamicconfig.annotation.DynamicConfig";
    
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        LOG.debug("PropertyNameReferenceProvider.getReferencesByElement: 被调用, element=" + element.getClass().getName());
        
        if (!(element instanceof PsiLiteralExpression)) {
            LOG.debug("PropertyNameReferenceProvider: 元素不是 PsiLiteralExpression，跳过");
            return PsiReference.EMPTY_ARRAY;
        }
        
        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
        Object value = literalExpression.getValue();
        LOG.debug("PropertyNameReferenceProvider: 字面量值=" + value + ", 类型=" + (value != null ? value.getClass().getName() : "null"));
        
        if (!(value instanceof String)) {
            LOG.debug("PropertyNameReferenceProvider: 值不是字符串，跳过");
            return PsiReference.EMPTY_ARRAY;
        }
        
        String propertyKey = (String) value;
        LOG.debug("PropertyNameReferenceProvider: 字符串值=" + propertyKey);
        
        // 首先检查是否是 ${xxx} 格式
        if (!propertyKey.startsWith("${") || !propertyKey.endsWith("}")) {
            LOG.debug("PropertyNameReferenceProvider: 不是 ${xxx} 格式，跳过");
            return PsiReference.EMPTY_ARRAY;
        }
        
        LOG.debug("PropertyNameReferenceProvider: 是 ${xxx} 格式，检查注解");
        
        // 验证是否在 @DynamicConfig 注解的 name 参数中
        boolean inAnnotation = isInDynamicConfigAnnotation(element);
        LOG.debug("PropertyNameReferenceProvider: 是否在 @DynamicConfig 注解中=" + inAnnotation);
        
        if (!inAnnotation) {
            LOG.debug("PropertyNameReferenceProvider: 不在 @DynamicConfig 注解中，跳过");
            return PsiReference.EMPTY_ARRAY;
        }
        
        // 提取配置键
        propertyKey = propertyKey.substring(2, propertyKey.length() - 1);
        // 处理默认值，如 ${xxx:default}
        int colonIndex = propertyKey.indexOf(':');
        if (colonIndex > 0) {
            propertyKey = propertyKey.substring(0, colonIndex);
        }
        
        LOG.info("PropertyNameReferenceProvider: 创建引用, propertyKey=" + propertyKey);
        
        Project project = element.getProject();
        ConfigPropertyResolver resolver = new ConfigPropertyResolver(project);
        
        PsiReference reference = new PropertyNameReference(literalExpression, propertyKey, resolver);
        LOG.info("PropertyNameReferenceProvider: 引用已创建");
        
        return new PsiReference[] { reference };
    }
    
    /**
     * 检查元素是否在 @DynamicConfig 注解中
     */
    private boolean isInDynamicConfigAnnotation(@NotNull PsiElement element) {
        LOG.debug("isInDynamicConfigAnnotation: 开始检查");
        
        // 获取注解
        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
        if (annotation == null) {
            LOG.debug("isInDynamicConfigAnnotation: 未找到注解");
            return false;
        }
        
        String annotationText = annotation.getText();
        LOG.debug("isInDynamicConfigAnnotation: 注解文本=" + (annotationText != null ? annotationText.substring(0, Math.min(100, annotationText.length())) : "null"));
        
        // 检查注解名称（支持完整限定名和简单名称）
        String qualifiedName = annotation.getQualifiedName();
        LOG.debug("isInDynamicConfigAnnotation: qualifiedName=" + qualifiedName);
        
        if (qualifiedName == null) {
            // 如果 qualifiedName 为 null，尝试从文本中获取
            LOG.debug("isInDynamicConfigAnnotation: qualifiedName 为 null，尝试从文本中获取");
            if (annotationText != null && annotationText.contains("DynamicConfig")) {
                LOG.debug("isInDynamicConfigAnnotation: 文本中包含 DynamicConfig");
                // 检查是否在 name 参数中
                PsiNameValuePair nameValuePair = PsiTreeUtil.getParentOfType(element, PsiNameValuePair.class);
                boolean isNameParam = nameValuePair != null && "name".equals(nameValuePair.getName());
                LOG.debug("isInDynamicConfigAnnotation: 是否在 name 参数中=" + isNameParam);
                return isNameParam;
            }
            LOG.debug("isInDynamicConfigAnnotation: 文本中不包含 DynamicConfig");
            return false;
        }
        
        // 支持完整限定名和简单名称（如果使用了导入）
        boolean isDynamicConfig = DYNAMIC_CONFIG_ANNOTATION.equals(qualifiedName) 
            || qualifiedName.endsWith(".DynamicConfig") 
            || "DynamicConfig".equals(qualifiedName);
        
        LOG.debug("isInDynamicConfigAnnotation: 是否是 DynamicConfig 注解=" + isDynamicConfig);
        
        if (!isDynamicConfig) {
            LOG.debug("isInDynamicConfigAnnotation: 不是 DynamicConfig 注解，qualifiedName=" + qualifiedName);
            return false;
        }
        
        // 确保是在 name 参数中
        PsiNameValuePair nameValuePair = PsiTreeUtil.getParentOfType(element, PsiNameValuePair.class);
        if (nameValuePair == null) {
            LOG.debug("isInDynamicConfigAnnotation: 未找到 PsiNameValuePair");
            return false;
        }
        
        String paramName = nameValuePair.getName();
        LOG.debug("isInDynamicConfigAnnotation: 参数名称=" + paramName);
        
        boolean isNameParam = "name".equals(paramName);
        LOG.debug("isInDynamicConfigAnnotation: 是否在 name 参数中=" + isNameParam);
        
        return isNameParam;
    }
}