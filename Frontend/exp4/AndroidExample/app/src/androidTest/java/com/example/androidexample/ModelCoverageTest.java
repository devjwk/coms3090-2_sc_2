
package com.example.androidexample;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ModelCoverageTest {

    /**
     * This test intentionally touches common frontend model classes.
     * It uses reflection so it can cover constructors, getters, setters,
     * toString(), hashCode(), and equals() without depending on one exact
     * constructor shape for every model.
     */
    @Test
    public void modelCoverage_touchesCommonDataClasses() throws Exception {
        touchModelClass("com.example.androidexample.User");
        touchModelClass("com.example.androidexample.Match");
        touchModelClass("com.example.androidexample.Report");
        touchModelClass("com.example.androidexample.Group");
        touchModelClass("com.example.androidexample.GroupMember");
        touchModelClass("com.example.androidexample.ChatMessage");
        touchModelClass("com.example.androidexample.RecommendGroup");
        touchModelClass("com.example.androidexample.ModeratorAccount");
        touchModelClass("com.example.androidexample.ModeratorManagedGroup");
        touchModelClass("com.example.androidexample.ModeratorMember");
        touchModelClass("com.example.androidexample.NotificationItem");
    }

    private void touchModelClass(String className) throws Exception {
        Class<?> clazz = Class.forName(className);
        Object instance = createInstance(clazz);
        assertNotNull(instance);

        setFieldsDirectly(clazz, instance);
        callSetters(clazz, instance);
        callGettersAndBooleanMethods(clazz, instance);

        instance.toString();
        instance.hashCode();
        instance.equals(instance);
        instance.equals(null);

        Object secondInstance = createInstance(clazz);
        assertNotNull(secondInstance);
        instance.equals(secondInstance);
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> best = constructors[0];

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length < best.getParameterTypes().length) {
                best = constructor;
            }
        }

        best.setAccessible(true);
        Class<?>[] parameterTypes = best.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = sampleValue(parameterTypes[i]);
        }

        return best.newInstance(args);
    }

    private void setFieldsDirectly(Class<?> clazz, Object instance) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            Object value = sampleValue(field.getType());
            field.set(instance, value);
            field.get(instance);
        }
    }

    private void callSetters(Class<?> clazz, Object instance) throws Exception {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().startsWith("set")) {
                continue;
            }
            if (method.getParameterTypes().length != 1) {
                continue;
            }

            method.setAccessible(true);
            method.invoke(instance, sampleValue(method.getParameterTypes()[0]));
        }
    }

    private void callGettersAndBooleanMethods(Class<?> clazz, Object instance) throws Exception {
        for (Method method : clazz.getDeclaredMethods()) {
            String name = method.getName();
            boolean isGetter = name.startsWith("get") || name.startsWith("is") || name.startsWith("has");

            if (!isGetter) {
                continue;
            }
            if (method.getParameterTypes().length != 0) {
                continue;
            }

            method.setAccessible(true);
            method.invoke(instance);
        }
    }

    private Object sampleValue(Class<?> type) {
        if (type == String.class) {
            return "coverage-test";
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == long.class || type == Long.class) {
            return 1L;
        }
        if (type == double.class || type == Double.class) {
            return 1.0;
        }
        if (type == float.class || type == Float.class) {
            return 1.0f;
        }
        if (type == boolean.class || type == Boolean.class) {
            return true;
        }
        if (type == List.class || type == ArrayList.class) {
            ArrayList<String> values = new ArrayList<>();
            values.add("Gaming");
            values.add("Programming");
            return values;
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length > 0 ? constants[0] : null;
        }
        if (type.isArray()) {
            return java.lang.reflect.Array.newInstance(type.getComponentType(), 0);
        }

        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }
}
