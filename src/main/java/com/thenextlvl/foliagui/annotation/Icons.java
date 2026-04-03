package com.thenextlvl.foliagui.annotation;

import java.lang.annotation.*;

/**
 * 图标容器注解
 * 用于支持在同一个字段上使用多个@Icon注解
 *
 * @author TheNextLvl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Icons {
    Icon[] value();
}
