package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 槽位处理器注解
 * <p>
 * 标记方法为槽位事件处理器，处理物品变化等事件
 * <pre>
 * &#64;SlotHandler("artifact_change")
 * public void onArtifactChange(Player player, ItemStack newItem, ItemStack oldItem) {
 *     // newItem 为 null 表示取出，非 null 表示放入
 *     // oldItem 为 null 表示之前为空
 * }
 * </pre>
 *
 * 方法签名要求：
 * - 参数1: Player - 打开 GUI 的玩家
 * - 参数2: ItemStack - 新物品（可能为 null）
 * - 参数3: ItemStack - 旧物品（可能为 null）
 * - 返回值: void
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SlotHandler {

    /**
     * 处理器名称
     * @return 名称
     */
    @NotNull
    String value();
}