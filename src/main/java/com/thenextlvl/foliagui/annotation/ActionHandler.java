package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 动作处理器注解 - 标记处理自定义动作的方法
 * 
 * 示例：
 * <pre>
 * @ActionHandler("buy")
 * public void onBuy(Player player, String args) {
 *     // args = "DIAMOND 100"
 *     String[] parts = args.split(" ");
 *     Material material = Material.valueOf(parts[0]);
 *     int price = Integer.parseInt(parts[1]);
 *     // 处理购买逻辑
 * }
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ActionHandler {
    
    /**
     * 处理器名称
     * 如果为空，则使用方法名
     * @return 名称
     */
    @NotNull
    String value() default "";
}
