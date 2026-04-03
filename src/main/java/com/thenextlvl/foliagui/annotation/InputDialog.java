package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 输入对话框注解
 * <p>
 * 标记方法为输入对话框处理器，返回对话框配置
 * <pre>
 * &#64;InputDialog("deposit_amount")
 * public InputDialogConfig onDepositInput(Player player) {
 *     return InputDialogConfig.builder()
 *         .title("&e存入银行")
 *         .prompt("&7请输入存入数量:")
 *         .inputType(InputType.NUMBER)
 *         .maxValue(1000000)
 *         .onSubmit((input) -&gt; {
 *             int amount = Integer.parseInt(input);
 *             depositToBank(player, amount);
 *             player.sendMessage("§a已存入 " + amount + " 金币");
 *         })
 *         .build();
 * }
 *
 * // 在按钮上使用
 * &#64;Action("input:deposit_amount")
 * private Button depositButton;
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InputDialog {

    /**
     * 对话框ID
     * <p>
     * 通过 "input:&lt;id&gt;" 动作触发
     *
     * @return ID
     */
    @NotNull
    String value();
}