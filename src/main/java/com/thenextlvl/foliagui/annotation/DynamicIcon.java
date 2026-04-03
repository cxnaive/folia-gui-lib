package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 动态图标注解 - 声明式定义动态更新的图标
 * <p>
 * 与静态图标不同，动态图标的内容会根据供应商方法动态更新
 * <pre>
 * &#64;DynamicIcon(
 *     id = "F",
 *     updateInterval = 20,          // 刷新间隔（tick）
 *     condition = "has_flower"       // 显示条件
 * )
 * &#64;DataProvider("flower_display")    // 数据提供器
 * private DynamicIcon flowerIcon;
 *
 * // 数据提供器方法
 * &#64;DataProviderMethod("flower_display")
 * public ItemStack getFlowerDisplay(Player player) {
 *     ItemStack equipped = equipManager.getEquippedArtifact(player, ArtifactType.FLOWER);
 *     if (equipped == null) {
 *         return ItemBuilder.of(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
 *             .name("&b&l生之花")
 *             .lore("&7空槽位")
 *             .build();
 *     }
 *     return ItemBuilder.of(equipped.getType())
 *         .name("&a已装备")
 *         .build();
 * }
 *
 * // 条件判断方法
 * &#64;Condition("has_flower")
 * public boolean hasFlower(Player player) {
 *     return equipManager.getEquippedArtifact(player, ArtifactType.FLOWER) != null;
 * }
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(DynamicIcons.class)
public @interface DynamicIcon {

    /**
     * 图标ID（布局中的字符）
     * @return 图标ID
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
     * 更新间隔（tick）
     * <p>
     * 0 或负数表示不自动更新
     * 默认 20 tick = 1 秒
     * @return 间隔
     */
    int updateInterval() default 20;

    /**
     * 是否异步获取物品
     * <p>
     * 如果供应商方法可能执行耗时操作，建议设为 true
     * @return 是否异步
     */
    boolean async() default false;

    /**
     * 显示条件
     * <p>
     * 对应 @Condition 注解标记的方法
     * @return 条件名称
     */
    @NotNull
    String condition() default "";

    /**
     * 条件不满足时显示的替代图标ID
     * @return 替代图标ID
     */
    @NotNull
    String elseIcon() default "";

    /**
     * 所需权限
     * @return 权限节点
     */
    @NotNull
    String permission() default "";

    /**
     * 点击动作
     * <p>
     * 点击此图标时触发的动作，对应 @ActionHandler 标记的方法
     * 可以指定多个动作，按顺序执行
     * @return 动作名称数组
     */
    @NotNull
    String[] action() default {};
}

/**
 * 动态图标容器注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface DynamicIcons {
    DynamicIcon[] value();
}