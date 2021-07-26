package com.xh.utils.mock.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

@Slf4j
public class ReflectUtil {

    private ReflectUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取传入字段名在嵌套类中的类实例
     * 例如：格式为"a.b.c"，在instance中有个字段名为a，a有个字段名为b，b有个字段名为c
     *     其中a已经是当前类的某个字段
     * @param instance
     * @param fieldNameSplits   fieldNameSplits为”a.b.c"以.分割的数组，如果数组长度小于2直接返回当前实例
     * @return
     */
    public static Object getClassInstanceOfField(Object instance, String[] fieldNameSplits) throws NoSuchFieldException, IllegalAccessException {
        Objects.requireNonNull(instance, "instance cannot be null");
        Objects.requireNonNull(fieldNameSplits, "fieldNameSplits cannot be null");
        //定位到最内层的类
        Object classInstanceOfField = instance;
        for (int i = 0; i < fieldNameSplits.length; i++) {
            if (i == fieldNameSplits.length - 1) {
                break;
            }
            Class<?> clazz = classInstanceOfField.getClass();
            Field field = clazz.getDeclaredField(fieldNameSplits[i]);
            field.setAccessible(true);
            classInstanceOfField = field.get(classInstanceOfField);
        }
        return classInstanceOfField;
    }

    /**
     * 获取类定义中某个字段的Field
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Field getFieldOfClassByName(Class<?> clazz, String fieldName) {
        Objects.requireNonNull(clazz, "clazz cannot be null");
        Objects.requireNonNull(fieldName, "fieldName cannot be null");
        Class<?> currentClazz = clazz;
        do {
            Field[] fields = currentClazz.getDeclaredFields();
            currentClazz = currentClazz.getSuperclass();
            for (Field field : fields) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
        } while (Objects.nonNull(currentClazz));
        throw new IllegalArgumentException("getFieldOfClassByName not found, className:" + clazz.getName() + "fieldName:" + fieldName);
    }

    /**
     * 反射修改类实例中指定属性的值为指定值
     *
     * @param instance
     * @param fieldName 格式为"a.b.c"，其中a已经是当前类的某个字段
     * @param fieldValue 序列化的字符串，如果本身就是String类型，则要配置为"\"string\""
     */
    public static void modifyFieldValue(Object instance, String fieldName, String fieldValue) {
        if (Objects.isNull(instance) || StringUtils.isEmpty(fieldName)) {
            return;
        }
        try {
            String[] fieldNameSplits = fieldName.split("\\.");
            Object classInstanceOfField = getClassInstanceOfField(instance, fieldNameSplits);
            //修改该类中指定的字段名为指定值
            Field field = getFieldOfClassByName(classInstanceOfField.getClass(), fieldNameSplits[fieldNameSplits.length - 1]);
            field.setAccessible(true);
            if (fieldValue == null) {
                field.set(classInstanceOfField, null);
            } else {
                if (field.getType().getName().equals("java.lang.String")) {
                    field.set(classInstanceOfField, fieldValue);
                } else {
                    field.set(classInstanceOfField, JsonUtils.fromJson(fieldValue, getJavaType(field.getGenericType())));
                }
            }
        } catch (Exception e) {
            log.error("modify real invoke result error:{}, modifyFieldName:{}, modifyFieldValue:{}"
                    , instance, fieldName, fieldValue, e);
        }
    }

    /**
     * 处理方法返回类型中带泛型的情况，返回具体javaType，用于反序列化
     * @param type
     * @return
     */
    public static JavaType getJavaType(Type type) {
        //判断是否带有泛型
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments();
            //获取泛型类型
            Class<?> rowClass = (Class<?>)((ParameterizedType)type).getRawType();
            JavaType[] javaTypes = new JavaType[actualTypeArguments.length];
            for (int i = 0; i < actualTypeArguments.length; i++) {
                //泛型也可能带有泛型，递归处理
                javaTypes[i] = getJavaType(actualTypeArguments[i]);
            }
            return TypeFactory.defaultInstance().constructParametricType(rowClass, javaTypes);
        } else {
            //简单类型直接用该类构建
            return TypeFactory.defaultInstance().constructParametricType((Class<?>)type, new JavaType[0]);
        }
    }
}
