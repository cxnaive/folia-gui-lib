package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 物品变化回调注解
 * <p>
 * 标记 ItemSlot 字段，指定物品变化时的回调处理器
 * <pre>
 * &#64;ItemSlot(id = "A", placeholder = Material.CHEST)
 * &#64;OnItemChange("artifact_change")
 * private ItemSlot artifactSlot;
 *
 * // 处理器方法
 * &#64;SlotHandler("artifact_change")
 * public void onArtifactChange(Player player, ItemStack newItem, ItemStack oldItem) {
 *     if (newItem != null) {
 *         player.sendMessage("§a放入了物品");
 *     }
 * }
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OnItemChange {

    /**
     * 处理器名称
     * <p>
     * 对应 @SlotHandler 标记的方法
     * @return 处理器名称
     */
    @NotNull
    String value();
}