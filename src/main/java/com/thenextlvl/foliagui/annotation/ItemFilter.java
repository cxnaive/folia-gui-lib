package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 物品过滤器注解
 * <p>
 * 标记方法为物品过滤器，用于判断物品是否可以放入槽位
 * <pre>
 * &#64;ItemFilter("artifact")
 * public boolean isArtifact(ItemStack item) {
 *     return artifactManager.parseArtifact(item) != null;
 * }
 *
 * &#64;ItemSlot(id = "A", filter = "artifact")
 * private ItemSlot artifactSlot;
 * </pre>
 *
 * 方法签名要求：
 * - 参数1: ItemStack - 要检查的物品
 * - 返回值: boolean - true 表示可以放入
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ItemFilter {

    /**
     * 过滤器名称
     * @return 名称
     */
    @NotNull
    String value();
}