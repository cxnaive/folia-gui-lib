package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 子布局注解
 * <p>
 * 定义可复用的子布局块
 * <pre>
 * &#64;Layout({
 *     "#########",
 *     "@equip_slots@",      // 引用其他布局
 *     "@action_buttons@",
 *     "#########"
 * })
 * protected String layout;
 *
 * &#64;SubLayout(name = "equip_slots", row = 1)
 * protected String equipLayout = "#F#P#S#X#";
 *
 * &#64;SubLayout(name = "action_buttons", row = 2)
 * protected String actionLayout = "#G#C#B#A#";
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SubLayout {

    /**
     * 子布局名称
     * <p>
     * 在主布局中使用 @name@ 引用
     *
     * @return 名称
     */
    @NotNull
    String name();

    /**
     * 插入的行号（0-based）
     * <p>
     * 指定子布局插入到主布局的哪一行
     * -1 表示根据字段定义自动确定
     *
     * @return 行号
     */
    int row() default -1;

    /**
     * 是否可选
     * <p>
     * 如果为 true，可以通过条件控制是否显示此子布局
     *
     * @return 是否可选
     */
    boolean optional() default false;

    /**
     * 显示条件
     * <p>
     * 当 optional = true 时，此条件决定是否显示
     *
     * @return 条件表达式
     */
    @NotNull
    String condition() default "";
}