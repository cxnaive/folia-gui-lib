package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 动作注解 - 定义点击时执行的动作
 * <p>
 * 支持多种内置动作类型：
 * <ul>
 *   <li>close - 关闭GUI</li>
 *   <li>refresh - 刷新GUI</li>
 *   <li>tell:&lt;message&gt; - 发送消息</li>
 *   <li>command:&lt;cmd&gt; - 玩家执行命令</li>
 *   <li>console:&lt;cmd&gt; - 控制台执行命令</li>
 *   <li>sound:&lt;sound&gt; - 播放音效</li>
 *   <li>open:&lt;gui-id&gt; - 打开其他GUI</li>
 *   <li>give:&lt;material&gt; &lt;amount&gt; - 给予物品</li>
 *   <li>take:&lt;material&gt; &lt;amount&gt; - 取走物品</li>
 *   <li>check_balance:&lt;amount&gt; - 检查金币（需自定义处理器）</li>
 *   <li>withdraw:&lt;amount&gt; - 扣款（需自定义处理器）</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;Action(value = "tell:&a购买成功!", condition = "permission:shop.buy")
 * &#64;Action(value = "tell:&c没有权限", condition = "!permission:shop.buy")
 * private Button buyButton;
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Repeatable(Actions.class)
public @interface Action {

    /**
     * 动作命令
     * <p>
     * 支持多种格式：
     * <ul>
     *   <li>close - 关闭GUI</li>
     *   <li>refresh - 刷新GUI</li>
     *   <li>tell:&lt;message&gt; - 发送消息（支持颜色代码）</li>
     *   <li>command:&lt;cmd&gt; - 玩家执行命令</li>
     *   <li>console:&lt;cmd&gt; - 控制台执行命令</li>
     *   <li>sound:&lt;sound&gt; [volume] [pitch] - 播放音效</li>
     *   <li>open:&lt;gui-id&gt; - 打开其他GUI</li>
     *   <li>give:&lt;material&gt; &lt;amount&gt; - 给予物品</li>
     *   <li>take:&lt;material&gt; &lt;amount&gt; - 取走物品</li>
     * </ul>
     *
     * @return 动作命令数组
     */
    @NotNull
    String[] value();

    /**
     * 点击类型限制
     *
     * @return 允许的点击类型
     */
    @NotNull
    ClickType[] type() default {ClickType.ALL};

    /**
     * 执行条件
     * <p>
     * 条件表达式，使用 ConditionEngine 解析
     *
     * @return 条件表达式
     */
    @NotNull
    String condition() default "";

    /**
     * 条件不满足时的处理动作
     * <p>
     * 当 condition 不满足时执行的动作
     *
     * @return 失败时动作
     */
    @NotNull
    String[] deny() default {};

    /**
     * 延迟执行（tick）
     * <p>
     * 延迟多少 tick 后执行此动作
     *
     * @return 延迟时间
     */
    int delay() default 0;

    /**
     * 执行失败时的提示消息
     * <p>
     * 当动作执行返回 false 时发送此消息
     * 仅对有返回值的自定义动作处理器有效
     *
     * @return 失败消息
     */
    @NotNull
    String failMessage() default "";

    /**
     * 是否在条件不满足时继续执行后续动作
     * <p>
     * 在 ActionChain 中，当此动作的条件不满足时是否继续执行后续动作
     *
     * @return 是否继续
     */
    boolean continueOnFail() default false;

    /**
     * 点击类型枚举
     */
    enum ClickType {
        ALL,           // 所有点击
        LEFT,          // 左键
        RIGHT,         // 右键
        SHIFT_LEFT,    // Shift+左键
        SHIFT_RIGHT,   // Shift+右键
        MIDDLE,        // 中键
        DOUBLE_CLICK,  // 双击
        DROP,          // 丢弃键(Q)
        CTRL_DROP,     // Ctrl+丢弃
        NUMBER_KEY,    // 数字键(1-9)
        SWAP_OFFHAND,  // F键(副手交换)
        UNKNOWN        // 未知
    }
}

/**
 * 动作容器注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@interface Actions {
    Action[] value();
}