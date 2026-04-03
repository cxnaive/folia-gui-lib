package com.thenextlvl.foliagui.annotation;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 图标注解 - 定义布局中使用的图标
 * 配合@Layout使用，按ID引用
 * 
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Icons.class)
public @interface Icon {
    
    /**
     * 图标ID（在布局中引用的字符）
     * @return ID
     */
    @NotNull
    String id();
    
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
    
    /**
     * 无权限时的替代图标ID
     * @return 替代图标ID
     */
    @NotNull
    String noPermissionIcon() default "";

    /**
     * 点击动作列表
     * @return 动作列表
     */
    @NotNull
    String[] action() default {};
}
