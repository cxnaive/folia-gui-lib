package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 输入捕获注解
 * <p>
 * 标记一个字段为输入捕获组件，点击后会让玩家输入内容
 * <pre>
 * &#64;Input(
 *     prompt = "请输入名称：",
 *     timeout = 60000,
 *     restoreGUI = true
 * )
 * private Button nameInputButton;
 * </pre>
 *
 * @author TheNextLvl
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Input {

    /**
     * 输入提示文本
     */
    @NotNull String prompt() default "请输入：";

    /**
     * 超时时间（毫秒）
     * -1 表示无超时
     */
    long timeout() default -1;

    /**
     * 输入完成后是否恢复 GUI
     */
    boolean restoreGUI() default true;

    /**
     * 取消关键词
     */
    @NotNull String[] cancelKeywords() default {"cancel", "取消"};

    /**
     * 输入验证正则表达式
     * 空字符串表示不验证
     */
    @NotNull String validator() default "";

    /**
     * 验证失败提示
     */
    @NotNull String validationErrorMessage() default "§c输入格式不正确！";

    /**
     * 输入完成后的动作处理器名称
     * 格式: handlerName arg1 arg2 ...
     */
    @NotNull String onComplete() default "";
}