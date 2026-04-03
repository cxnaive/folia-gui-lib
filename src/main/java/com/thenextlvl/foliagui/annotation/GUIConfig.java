package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * GUI配置注解 - 声明式定义GUI
 * 用于类级别，定义GUI的基本属性
 * 
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface GUIConfig {
    
    /**
     * GUI唯一标识
     * @return ID
     */
    @NotNull
    String id();
    
    /**
     * GUI标题
     * 支持颜色代码&和占位符
     * @return 标题
     */
    @NotNull
    String[] title() default "";
    
    /**
     * 标题切换间隔（tick）
     * 如果title数组有多个值，按此间隔切换
     * @return 间隔
     */
    int titleUpdate() default 40;
    
    /**
     * GUI行数（1-6）
     * @return 行数
     */
    int rows() default 3;
    
    /**
     * 是否使用玩家背包作为菜单
     * @return 是否启用
     */
    boolean usePlayerInventory() default false;
    
    /**
     * 是否允许玩家点击自己的背包物品
     * @return 是否允许
     */
    boolean allowPlayerInventoryClick() default false;
    
    /**
     * 是否允许玩家拖拽物品到GUI
     * @return 是否允许
     */
    boolean allowDrag() default false;
    
    /**
     * 是否允许玩家Shift+点击
     * @return 是否允许
     */
    boolean allowShiftClick() default false;
}
