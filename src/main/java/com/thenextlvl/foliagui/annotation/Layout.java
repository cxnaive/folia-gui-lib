package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 布局注解 - TrMenu风格的可视化布局定义
 * <p>
 * 使用字符串数组定义GUI布局，字符对应组件ID
 * <p>
 * 示例：
 * <pre>
 * // 直接定义布局
 * &#64;Layout({
 *     "########`Close`",
 *     "  A B C  ",
 *     "  D E F  ",
 *     "         ",
 *     "########`Next`"
 * })
 *
 * // 使用模板
 * &#64;Layout(template = "standard_frame", contentRow = 2)
 *
 * // 使用子布局引用
 * &#64;Layout({
 *     "#########",
 *     "@equip_slots@",
 *     "@action_buttons@",
 *     "#########"
 * })
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Layout {

    /**
     * 布局字符串数组
     * <p>
     * 每个字符串代表一行，字符对应组件ID
     * <ul>
     *   <li>空格表示空槽位</li>
     *   <li># 可以用作装饰字符（自动映射到decoration）</li>
     *   <li>`字符` 表示特殊组件（如Close、Next等）</li>
     *   <li>@name@ 表示引用子布局</li>
     *   <li>{content} 表示模板中的内容区域占位符</li>
     * </ul>
     *
     * @return 布局
     */
    @NotNull
    String[] value() default {};

    /**
     * 装饰字符映射
     * <p>
     * 默认#表示装饰
     *
     * @return 装饰字符
     */
    char decorationChar() default '#';

    /**
     * 空槽位字符
     * <p>
     * 默认空格表示空槽位
     *
     * @return 空槽位字符
     */
    char emptyChar() default ' ';

    /**
     * 引用的模板ID
     * <p>
     * 如果指定，则继承模板的布局
     *
     * @return 模板ID
     */
    @NotNull
    String template() default "";

    /**
     * 内容区域起始行号
     * <p>
     * 当使用模板时，指定内容插入到哪一行
     * 默认 -1 表示自动检测 {content} 占位符
     *
     * @return 行号
     */
    int contentRow() default -1;

    /**
     * 内容区域行数
     * <p>
     * 当使用模板时，指定内容区域占几行
     *
     * @return 行数
     */
    int contentRows() default 1;
}
