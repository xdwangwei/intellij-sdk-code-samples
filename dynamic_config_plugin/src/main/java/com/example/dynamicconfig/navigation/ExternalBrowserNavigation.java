package com.example.dynamicconfig.navigation;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * 外部浏览器导航工具类
 */
public class ExternalBrowserNavigation {
    
    private static final Logger LOG = Logger.getInstance(ExternalBrowserNavigation.class);
    
    /**
     * 打开浏览器访问指定 URL
     * 
     * @param url 要访问的 URL
     */
    public static void openBrowser(@NotNull String url) {
        try {
            BrowserUtil.browse(url);
        } catch (Exception e) {
            LOG.error("Failed to open browser with URL: " + url, e);
            Messages.showErrorDialog("无法打开浏览器: " + e.getMessage(), "浏览器导航错误");
        }
    }
    
    /**
     * 打开浏览器访问指定 URL（带项目上下文）
     * 
     * @param project 项目实例
     * @param url 要访问的 URL
     */
    public static void openBrowser(@NotNull Project project, @NotNull String url) {
        try {
            BrowserUtil.browse(url);
        } catch (Exception e) {
            LOG.error("Failed to open browser with URL: " + url, e);
            Messages.showErrorDialog(project, "无法打开浏览器: " + e.getMessage(), "浏览器导航错误");
        }
    }
    
    /**
     * 打开浏览器访问指定 URL（带确认对话框）
     * 
     * @param url 要访问的 URL
     * @param title 对话框标题
     * @param message 对话框消息
     */
    public static void openBrowserWithConfirmation(@NotNull String url, @NotNull String title, @NotNull String message) {
        int result = Messages.showYesNoDialog(message, title, Messages.getQuestionIcon());
        if (result == Messages.YES) {
            openBrowser(url);
        }
    }
    
    /**
     * 打开浏览器访问指定 URL（带确认对话框和项目上下文）
     * 
     * @param project 项目实例
     * @param url 要访问的 URL
     * @param title 对话框标题
     * @param message 对话框消息
     */
    public static void openBrowserWithConfirmation(@NotNull Project project, @NotNull String url, 
                                                  @NotNull String title, @NotNull String message) {
        int result = Messages.showYesNoDialog(project, message, title, Messages.getQuestionIcon());
        if (result == Messages.YES) {
            openBrowser(project, url);
        }
    }
}