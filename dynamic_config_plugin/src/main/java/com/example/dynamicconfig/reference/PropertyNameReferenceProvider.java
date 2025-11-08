package com.example.dynamicconfig.reference;

import com.example.dynamicconfig.util.ConfigPropertyResolver;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性名引用提供者，用于 @DynamicConfig 注解的 name 参数
 */
public class PropertyNameReferenceProvider extends PsiReferenceProvider {
    
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
        
        String propertyKey = (String) value;
        
        // 处理 ${xxx} 格式的表达式
        if (propertyKey.startsWith("${") && propertyKey.endsWith("}")) {
            propertyKey = propertyKey.substring(2, propertyKey.length() - 1);
        }
        
        Project project = element.getProject();
        ConfigPropertyResolver resolver = new ConfigPropertyResolver(project);
        
        return new PsiReference[] { new PropertyNameReference(literalExpression, propertyKey, resolver) };
    }
}