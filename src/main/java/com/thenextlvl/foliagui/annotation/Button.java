package com.thenextlvl.foliagui.annotation;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 按钮注解 - 直接在槽位放置按钮
 * 不需要配合@Layout使用，直接指定槽位
 * 
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Buttons.class)
public @interface Button {
    
    /**
     * 槽位（0-53）
     * @return 槽位
     */
    int slot();
    
    /**
     * 材质
     * @return 材质
     */
    @NotNull
    Material material();
    
    /**
     * 显示名称
     * @return 名称
     */
    @NotNull
    String name() default "";
    
    /**
     * 描述
     * @return 描述
     */
    @NotNull
    String[] lore() default {};
    
    /**
     * 数量
     * @return 数量
     */
    int amount() default 1;
    
    /**
     * 是否发光
     * @return 是否发光
     */
    boolean glow() default false;
    
    /**
     * 自定义模型数据
     * @return 模型数据
     */
    int customModelData() default -1;
    
    /**
     * 刷新间隔（tick）
     * @return 间隔
     */
    int update() default -1;
    
    /**
     * 显示条件
     * @return 条件表达式
     */
    @NotNull
    String condition() default "";
    
    /**
     * 所需权限
     * @return 权限节点
     */
    @NotNull
    String permission() default "";
}

/**
 * 按钮容器注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Buttons {
    Button[] value();
}
