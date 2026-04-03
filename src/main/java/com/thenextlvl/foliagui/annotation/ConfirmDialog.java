package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 确认对话框注解
 * <p>
 * 标记方法为确认对话框处理器，返回对话框配置
 * <pre>
 * &#64;ConfirmDialog("delete_all")
 * public ConfirmDialogConfig onDeleteAllConfirm(Player player) {
 *     return ConfirmDialogConfig.builder()
 *         .title("&c确认删除")
 *         .message("&7确定要删除背包中所有物品吗？")
 *         .confirmText("&a确认")
 *         .cancelText("&c取消")
 *         .onConfirm(() -&gt; {
 *             player.getInventory().clear();
 *             player.sendMessage("§a已清空背包");
 *         })
 *         .onCancel(() -&gt; player.sendMessage("§7已取消"))
 *         .build();
 * }
 *
 * // 在按钮上使用
 * &#64;Action("confirm:delete_all")
 * private Button deleteAllButton;
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfirmDialog {

    /**
     * 对话框ID
     * <p>
     * 通过 "confirm:&lt;id&gt;" 动作触发
     *
     * @return ID
     */
    @NotNull
    String value();
}