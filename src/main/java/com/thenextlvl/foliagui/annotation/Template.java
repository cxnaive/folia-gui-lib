package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模板注解
 * <p>
 * 标记一个字段使用模板定义，支持参数化配置
 * <pre>
 * &#64;Template(
 *     id = "shop-item",
 *     params = {"name", "price", "material"}
 * )
 * private Button shopItem;
 * </pre>
 *
 * @author TheNextLvl
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Template {

    /**
     * 模板 ID
     */
    @NotNull String id();

    /**
     * 模板参数名列表
     */
    @NotNull String[] params() default {};

    /**
     * 参数默认值（格式: key=value）
     */
    @NotNull String[] defaults() default {};
}