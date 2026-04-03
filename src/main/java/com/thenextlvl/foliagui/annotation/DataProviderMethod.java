package com.thenextlvl.foliagui.annotation;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 数据提供器方法注解
 * <p>
 * 标记方法为数据提供器，返回动态图标要显示的物品
 * <pre>
 * &#64;DataProviderMethod("flower_display")
 * public ItemStack getFlowerDisplay(Player player) {
 *     ItemStack equipped = equipManager.getEquippedArtifact(player, ArtifactType.FLOWER);
 *     if (equipped == null) {
 *         return ItemBuilder.of(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
 *             .name("&b&l生之花")
 *             .lore("&7空槽位")
 *             .build();
 *     }
 *     return equipped.clone();
 * }
 * </pre>
 *
 * 方法签名要求：
 * - 参数1: Player - 打开 GUI 的玩家
 * - 返回值: ItemStack - 要显示的物品
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataProviderMethod {

    /**
     * 数据提供器名称
     * @return 名称
     */
    @NotNull
    String value();
}