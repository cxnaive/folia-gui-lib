package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 动作链注解 - 定义一系列按顺序执行的动作
 * <p>
 * 动作会按照定义的顺序依次执行，如果某个动作执行失败
 * （返回 false 或抛出异常），可以选择停止执行或继续
 * <p>
 * 使用示例：
 * <pre>
 * &#64;ActionChain({
 *     &#64;Action(value = "check_balance:100", failMessage = "&c金币不足"),
 *     &#64;Action(value = "withdraw:100", failMessage = "&c扣款失败"),
 *     &#64;Action(value = "give:DIAMOND 1", failMessage = "&c背包已满"),
 *     &#64;Action("tell:&a购买成功！"),
 *     &#64;Action("sound:ENTITY_PLAYER_LEVELUP"),
 *     &#64;Action("refresh")
 * })
 * private Button buyDiamondButton;
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(ActionChains.class)
public @interface ActionChain {

    /**
     * 动作列表
     * <p>
     * 按顺序执行的动作数组
     *
     * @return 动作数组
     */
    @NotNull
    Action[] value();

    /**
     * 点击类型限制
     *
     * @return 允许的点击类型
     */
    @NotNull
    Action.ClickType[] type() default {Action.ClickType.ALL};

    /**
     * 整个动作链的执行条件
     * <p>
     * 如果此条件不满足，整个动作链都不会执行
     *
     * @return 条件表达式
     */
    @NotNull
    String condition() default "";

    /**
     * 条件不满足时的处理
     *
     * @return 失败时的处理动作
     */
    @NotNull
    String[] deny() default {};

    /**
     * 是否在某个动作失败时继续执行后续动作
     * <p>
     * 默认 false，即任一动作失败就停止
     *
     * @return 是否继续
     */
    boolean continueOnFail() default false;

    /**
     * 动作链执行完成后的回调动作
     *
     * @return 完成后动作
     */
    @NotNull
    String[] onComplete() default {};
}

/**
 * 动作链容器注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ActionChains {
    ActionChain[] value();
}