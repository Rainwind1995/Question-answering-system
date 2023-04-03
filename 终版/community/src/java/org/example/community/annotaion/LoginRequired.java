package org.example.community.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 说明了注解的作用范围: 方法
@Target(ElementType.METHOD)
// 定义了注解在运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

}
