package com.thenextlvl.foliagui.annotation;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 物品槽位注解 - 声明式定义可放入物品的槽位
 * <p>
 * 使用示例：
 * <pre>
 * &#64;ItemSlot(
 *     id = "A",                          // 槽位ID（布局中的字符）
 *     placeholder = Material.LIGHT_BLUE_STAINED_GLASS_PANE,
 *     placeholderName = "&b&l圣遗物槽位",
 *     filter = "artifact",               // 过滤器名称
 *     autoReturn = true                  // 关闭时自动返还
 * )
 * &#64;OnItemChange("artifact_change")       // 物品变化回调
 * private ItemSlot artifactSlot;
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(ItemSlots.class)
public @interface ItemSlot {

    /**
     * 槽位ID（布局中的字符）
     * <p>
     * 与 @Layout 中的字符对应
     * @return 槽位ID
     */
    @NotNull
    String id();

    /**
     * 槽位索引（直接指定槽位号）
     * <p>
     * 如果不使用布局，可以直接指定槽位（0-53）
     * @return 槽位号，-1 表示使用布局ID
     */
    int slot() default -1;

    /**
     * 占位符材质
     * @return 材质
     */
    @NotNull
    Material placeholder() default Material.LIGHT_GRAY_STAINED_GLASS_PANE;

    /**
     * 占位符名称
     * @return 名称
     */
    @NotNull
    String placeholderName() default "&7物品槽位";

    /**
     * 占位符描述
     * @return 描述
     */
    @NotNull
    String[] placeholderLore() default {"&e点击放入物品"};

    /**
     * 物品过滤器名称
     * <p>
     * 对应 @ItemFilter 注解标记的方法
     * @return 过滤器名称
     */
    @NotNull
    String filter() default "";

    /**
     * 是否自动返还物品
     * <p>
     * GUI 关闭时是否自动返还槽位中的物品给玩家
     * @return 是否自动返还
     */
    boolean autoReturn() default true;

    /**
     * 物品数量限制
     * <p>
     * -1 表示无限制
     * @return 数量限制
     */
    int stackLimit() default -1;

    /**
     * 显示条件
     * @return 条件表达式
     */
    @NotNull
    String condition() default "";

    /**
     * 所需权限
     * @return 权限节点
     */
    @NotNull
    String permission() default "";
}

/**
 * 物品槽位容器注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ItemSlots {
    ItemSlot[] value();
}