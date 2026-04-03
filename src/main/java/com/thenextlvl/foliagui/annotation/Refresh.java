package com.thenextlvl.foliagui.annotation;

import java.lang.annotation.*;

/**
 * 自动刷新注解
 * 标记字段需要自动刷新
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Refresh {

    /**
     * 刷新间隔（tick）
     * @return 间隔
     */
    int interval() default 20;

    /**
     * 是否异步刷新
     * @return 是否异步
     */
    boolean async() default false;
}