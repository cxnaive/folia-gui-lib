package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 布局模板注解
 * <p>
 * 标记类为布局模板，其他 GUI 可以继承此模板
 * <pre>
 * // 定义模板
 * &#64;LayoutTemplate(id = "standard_frame")
 * public abstract class StandardFrameGUI extends AbstractDeclarativeGUI {
 *
 *     &#64;Layout({
 *         "####T####",
 *         "#         #",
 *         "# {content} #",
 *         "#         #",
 *         "####B####"
 *     })
 *     protected String layout;
 *
 *     &#64;Icon(id = "T", material = Material.GOLD_BLOCK, name = "&6{title}")
 *     protected Object titleBar;
 *
 *     &#64;Icon(id = "B", material = Material.BEDROCK, name = "&c关闭")
 *     &#64;Action("close")
 *     protected Object bottomBar;
 * }
 *
 * // 继承模板
 * &#64;GUIConfig(id = "shop", title = "&6商店")
 * &#64;Layout(template = "standard_frame", contentRow = 2)
 * public class ShopGUI extends StandardFrameGUI {
 *     // 自动继承模板布局
 * }
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LayoutTemplate {

    /**
     * 模板ID
     * <p>
     * 用于在 @Layout 中引用
     *
     * @return 模板ID
     */
    @NotNull
    String id();

    /**
     * 模板描述
     *
     * @return 描述
     */
    @NotNull
    String description() default "";
}