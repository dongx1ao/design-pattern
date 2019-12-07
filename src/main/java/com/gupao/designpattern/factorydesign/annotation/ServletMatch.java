package com.gupao.designpattern.factorydesign.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServletMatch {
    String value() default "";
}
