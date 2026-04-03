package com.thenextlvl.foliagui.annotation;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 条件注解
 * <p>
 * 标记方法为条件判断器，用于控制图标的显示条件
 * <pre>
 * &#64;Condition("has_flower")
 * public boolean hasFlower(Player player) {
 *     return equipManager.getEquippedArtifact(player, ArtifactType.FLOWER) != null;
 * }
 *
 * &#64;DynamicIcon(id = "F", condition = "has_flower")
 * private DynamicIcon flowerIcon;
 * </pre>
 *
 * 方法签名要求：
 * - 参数1: Player - 打开 GUI 的玩家
 * - 返回值: boolean - true 表示条件满足
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Condition {

    /**
     * 条件名称
     * @return 名称
     */
    @NotNull
    String value();
}