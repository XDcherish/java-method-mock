package com.xh.utils.mock.aop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xh.utils.mock.dto.MethodMockDTO;
import com.xh.utils.mock.dto.MockRequestDTO;
import com.xh.utils.mock.util.JsonUtils;
import com.xh.utils.mock.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description: 各种方法mock调用
 * @author: suiyan
 * @create: 2020-10-21
 */
@Slf4j
@Aspect
@Component
public class MethodMockAspect {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@within(com.xh.utils.mock.annotation.MethodMock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ProxyMethodInvocation mi = (ProxyMethodInvocation) FieldUtils.readDeclaredField(joinPoint, "methodInvocation", true);
//        MethodMock methodAnnotation = joinPoint.getTarget().getClass().getAnnotation(MethodMock.class);

        //这里注意类和方法之间是 # ，要和配置保持一致，有些copy reference出来是 . 来分割的
        String groupKey = Objects.requireNonNull(mi.getThis()).getClass().getName() + "#" + mi.getMethod().getName();
        Map<String, MethodMockDTO> methodMockMap;
        try {
            methodMockMap = getConfigContent();
            //LeoUtils.getJsonProperty(methodAnnotation.serverName() + ".method.mock.result", new TypeReference<HashMap<String, MethodMockDTO>>(){});
        } catch (Exception e) {
            log.error("parse methodMockMap error, groupKey:{}, ", groupKey, e);
            return joinPoint.proceed();
        }
        //没有配置mock直接真实调用
        if (methodMockMap == null || methodMockMap.isEmpty() || Objects.isNull(methodMockMap.get(groupKey))) {
            return joinPoint.proceed();
        }
        MethodMockDTO mockDTO = methodMockMap.get(groupKey);
        //没有开启mock直接真实调用
        if (Objects.isNull(mockDTO.getOpenMock()) || !mockDTO.getOpenMock()) {
            return joinPoint.proceed();
        } else {
            try {
                //方法无入参
                if (Objects.isNull(joinPoint.getArgs()) || joinPoint.getArgs().length == 0) {
                    log.info("success mock invoke method without args, type:{}, groupKey:{}, response:{}"
                            , mockDTO.getMockResponseDTOS().get(0).getMockType()
                            , groupKey
                            , JsonUtils.toJson(mockDTO.getMockResponseDTOS().get(0)));

                    return getMethodMockResult(joinPoint
                            , mockDTO.getMockResponseDTOS().get(0).getResponseContent()
                            , mockDTO.getMockResponseDTOS().get(0).getMockType());
                }
                //方法有入参
                for (int i = 0; i < mockDTO.getMockRequestDTOsList().size(); i++) {
                    //最终如果需要对比的字段都相等则mock执行
                    if ( compareArgs(joinPoint.getArgs(), mockDTO.getMockRequestDTOsList().get(i)) ) {
                        log.info("success mock invoke method , type:{}, groupKey:{}, request:{}. response:{}"
                                , mockDTO.getMockResponseDTOS().get(i).getMockType()
                                , groupKey
                                , JsonUtils.toJson(mockDTO.getMockRequestDTOsList().get(i))
                                , JsonUtils.toJson(mockDTO.getMockResponseDTOS().get(i)));

                        return getMethodMockResult(joinPoint
                                , mockDTO.getMockResponseDTOS().get(i).getResponseContent()
                                , mockDTO.getMockResponseDTOS().get(i).getMockType());
                    }
                }
            } catch (Exception e) {
                log.error("parse methodMockMap error, groupKey:{}, request:{}", groupKey, JsonUtils.toJson(joinPoint.getArgs()), e);
            }
            return joinPoint.proceed();
        }
    }

    /**
     * 获取配置内容，这里简单演示在本地创建了一个config.json配置
     * 但真实使用的时候会使用类似携程的apollo等远程配置框架来获取配置内容，这样可以不用重新发版来修改配置
     * @return
     */
    private Map<String, MethodMockDTO> getConfigContent() throws IOException {
        String path = "/config.json";
        InputStream config = MethodMockAspect.class.getResourceAsStream(path);
        if (config != null) {
            Map<String, MethodMockDTO> configMap = objectMapper.readValue(config, new TypeReference<Map<String, MethodMockDTO>>() {});
            System.out.println(JsonUtils.toJson(configMap));
            return configMap;
        }
        return null;
    }

    /**
     * 方法参数对比
     * @param args
     * @param mockRequestDTOS
     * @return
     * @throws IllegalAccessException
     */
    private static boolean compareArgs(Object[] args, List<MockRequestDTO> mockRequestDTOS) throws IllegalAccessException {
        boolean isEqual = true;
        for (int j = 0; j < args.length; j++) {
            //多个参数只要有一个参数有字段不相等则直接结束
            if (!isEqual) {
                break;
            }
            MockRequestDTO mockRequestDTO = mockRequestDTOS.get(j);
            if (mockRequestDTO.getRequestType() == 0) {
                Map<String, Object> needCompareFieldMap = (Map<String, Object>) mockRequestDTO.getRequestCompareContent();
                for (Map.Entry<String, Object> needCompareField : needCompareFieldMap.entrySet()) {
                    //多个字段中只要有一个字段不相等则直接结束
                    if (!isEqual) {
                        break;
                    }
                    Object instance = args[j];
                    Field field = ReflectUtil.getFieldOfClassByName(instance.getClass(), needCompareField.getKey());
                    field.setAccessible(true);
                    if (!JsonUtils.toJson(needCompareField.getValue()).equals(JsonUtils.toJson(field.get(instance)))) {
                        isEqual = false;
                    }
                }
            } else {
                if (!JsonUtils.toJson(mockRequestDTO.getRequestCompareContent()).equals(JsonUtils.toJson(args[j]))) {
                    isEqual = false;
                }
            }
        }
        return isEqual;
    }

    /**
     * 返回方法构造结果
     * @param joinPoint
     * @param responseContent
     * @param mockType
     * @return
     * @throws Throwable
     */
    private Object getMethodMockResult(ProceedingJoinPoint joinPoint
            , Object responseContent
            , Integer mockType) throws Throwable {

        if (mockType == 0) {
            //修改类型，真实调用后修改返回结果
            Object result = joinPoint.proceed();
            Map<String, String> modifyFieldMap = (Map) responseContent;
            modifyFieldMap.forEach((modifyFieldName, modifyFieldValue) -> ReflectUtil.modifyFieldValue(result, modifyFieldName, modifyFieldValue));
            return result;
        } else {
            //构造类型，全部返回mock结果
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return objectMapper.readValue(JsonUtils.toJson(responseContent), ReflectUtil.getJavaType(signature.getMethod().getGenericReturnType()));
        }
    }

}