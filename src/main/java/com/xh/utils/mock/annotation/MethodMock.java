package com.xh.utils.mock.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface MethodMock {

    //服务名，例如xxx-api，用于拼接远程配置名称。 改造apollo等框架时可以用上
    //String serverName();
}
