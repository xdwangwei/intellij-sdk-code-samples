package com.example.dynamicconfig.reference;

import com.intellij.openapi.diagnostic.Logger;
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
    
    private static final Logger LOG = Logger.getInstance(DynamicConfigReferenceContributor.class);
    
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        LOG.info("DynamicConfigReferenceContributor: 开始注册引用提供者");
        
        // 注册 name 参数的引用提供者
        // 匹配注解参数值中的字符串字面量
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiLiteralExpression.class)
                .withParent(
                    PlatformPatterns.psiElement(PsiNameValuePair.class)
                        .withName("name")
                ),
            new PropertyNameReferenceProvider()
        );
        LOG.info("DynamicConfigReferenceContributor: 已注册 PropertyNameReferenceProvider (name 参数)");
        
        // 注册 key 参数的引用提供者
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiLiteralExpression.class)
                .withParent(
                    PlatformPatterns.psiElement(PsiNameValuePair.class)
                        .withName("key")
                ),
            new KeyReferenceProvider()
        );
        LOG.info("DynamicConfigReferenceContributor: 已注册 KeyReferenceProvider (key 参数)");
        
        LOG.info("DynamicConfigReferenceContributor: 引用提供者注册完成");
    }
}