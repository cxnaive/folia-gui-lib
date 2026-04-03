package com.thenextlvl.foliagui.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * 数据提供器注解
 * <p>
 * 标记字段指定其数据提供器方法名称
 * <pre>
 * &#64;DynamicIcon(id = "F")
 * &#64;DataProvider("flower_display")
 * private DynamicIcon flowerIcon;
 *
 * &#64;DataProviderMethod("flower_display")
 * public ItemStack getFlowerDisplay(Player player) {
 *     // 返回要显示的物品
 * }
 * </pre>
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataProvider {

    /**
     * 数据提供器方法名称
     * <p>
     * 对应 @DataProviderMethod 标记的方法
     * @return 方法名称
     */
    @NotNull
    String value();
}