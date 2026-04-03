package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 玩家背包布局注解
 * 定义玩家背包中哪些槽位作为菜单的一部分
 * 
 * 示例：
 * <pre>
 * @PlayerInventoryLayout({
 *     "         ",
 *     "    G    "
 * })
 * </pre>
 * 
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PlayerInventoryLayout {
    
    /**
     * 布局字符串数组
     * 每行9个槽位，共4行（0-35）
     * @return 布局
     */
    @NotNull
    String[] value();
    
    /**
     * 装饰字符
     * @return 装饰字符
     */
    char decorationChar() default '#';
    
    /**
     * 空槽位字符
     * @return 空槽位字符
     */
    char emptyChar() default ' ';
    
    /**
     * 是否允许玩家点击自己的背包物品
     * @return 是否允许
     */
    boolean allowClick() default false;
    
    /**
     * 是否允许玩家拖拽物品
     * @return 是否允许
     */
    boolean allowDrag() default false;
}
