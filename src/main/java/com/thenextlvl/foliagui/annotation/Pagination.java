package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分页配置注解
 * <p>
 * 标记一个类使用分页配置，用于声明式分页 GUI
 * <pre>
 * &#64;GUIConfig(id = "items", title = "物品列表", rows = 6)
 * &#64;Pagination(
 *     contentSlots = {10, 11, 12, 13, 14, 15, 16},
 *     prevSlot = 18,
 *     nextSlot = 26,
 *     indicatorSlot = 22
 * )
 * public class ItemsMenu extends DeclarativeGUI {
 *     // ...
 * }
 * </pre>
 *
 * @author TheNextLvl
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pagination {

    /**
     * 内容槽位数组
     */
    int[] contentSlots();

    /**
     * 上一页按钮槽位
     * -1 表示不显示
     */
    int prevSlot() default -1;

    /**
     * 下一页按钮槽位
     * -1 表示不显示
     */
    int nextSlot() default -1;

    /**
     * 页码指示器槽位
     * -1 表示不显示
     */
    int indicatorSlot() default -1;

    /**
     * 每页物品数量
     * 默认根据 contentSlots 长度自动计算
     */
    int itemsPerPage() default -1;

    /**
     * 上一页按钮材质
     */
    @NotNull String prevMaterial() default "ARROW";

    /**
     * 上一页按钮名称
     */
    @NotNull String prevName() default "§e上一页";

    /**
     * 下一页按钮材质
     */
    @NotNull String nextMaterial() default "ARROW";

    /**
     * 下一页按钮名称
     */
    @NotNull String nextName() default "§e下一页";

    /**
     * 页码指示器材质
     */
    @NotNull String indicatorMaterial() default "PAPER";

    /**
     * 页码指示器名称模板
     * 支持 {current} 和 {total} 占位符
     */
    @NotNull String indicatorName() default "§7第 §e{current} §7页 / 共 §e{total} §7页";
}