package com.example.dynamicconfig.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.PsiNameValuePair;
import org.jetbrains.annotations.NotNull;

/**
 * 动态配置引用贡献者，负责注册 @DynamicConfig 注解的引用提供者
 */
public class DynamicConfigReferenceContributor extends PsiReferenceContributor {
    
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        // 注册 name 参数的引用提供者 - 精确的模式匹配
        registrar.registerReferenceProvider(
            // 匹配注解属性值，具体是name属性的值
            PlatformPatterns.psiElement(PsiLiteralExpression.class).withParent(
                // 父元素是注解属性
                PlatformPatterns.psiElement(PsiNameValuePair.class).withName("name").withParent(
                    // 注解属性的父元素是包含DynamicConfig的注解
                    PlatformPatterns.psiElement().withText(PlatformPatterns.string().contains("DynamicConfig"))
                )
            ),
            new PropertyNameReferenceProvider()
        );
        
        // 注册 key 参数的引用提供者 - 精确的模式匹配
        registrar.registerReferenceProvider(
            // 匹配注解属性值，具体是key属性的值
            PlatformPatterns.psiElement(PsiLiteralExpression.class).withParent(
                // 父元素是注解属性
                PlatformPatterns.psiElement(PsiNameValuePair.class).withName("key").withParent(
                    // 注解属性的父元素是包含DynamicConfig的注解
                    PlatformPatterns.psiElement().withText(PlatformPatterns.string().contains("DynamicConfig"))
                )
            ),
            new KeyReferenceProvider()
        );
    }
}