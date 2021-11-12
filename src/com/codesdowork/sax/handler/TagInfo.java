package com.codesdowork.sax.handler;

import com.codesdowork.sax.annotations.XmlParser;
import com.codesdowork.sax.parsers.RegisteredParsers;
import com.codesdowork.sax.parsers.ValueParser;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class TagInfo {

    private final static ArrayList<TagInfo> TAG_INFOS = new ArrayList<>();

    private Class<?> resultType;

    public final Field field;

    public final Class<?> type;

    public final Class<?> componentType;

    public final boolean isRepeatableType;

    public final boolean isPrimitiveType;

    public final ValueParser<?> parser;

    public final boolean isParsable;

    public static TagInfo getInstance(Class<?> resultType, Field field) {
        for(TagInfo info : TAG_INFOS) {
            if(field.equals(info.field)) {
                info.resultType = resultType;
                return info;
            }
        }

        TagInfo info = new TagInfo(resultType, field);
        TAG_INFOS.add(info);
        return info;
    }

    public static TagInfo getInstance(Class<?> resultType, Class<?> type) {
        for(TagInfo info : TAG_INFOS) {
            if(info.type == type) {
                info.resultType = resultType;
                return info;
            }
        }

        TagInfo info = new TagInfo(resultType, type);
        TAG_INFOS.add(info);
        return info;
    }

    private TagInfo(Class<?> resultType, Field field) {
        this(resultType, field, field.getType());
    }

    private TagInfo(Class<?> resultType, Class<?> type) {
        this(resultType, null, type);
    }

    private TagInfo(Class<?> resultType, Field field, Class<?> type) {
        this.resultType = resultType;
        this.field = field;
        this.type = type;
        ComponentTypeResult result = getComponentType();
        this.componentType = result.componentType;
        this.isRepeatableType = result.isRepeatableType;
        this.isPrimitiveType = isPrimitiveType(componentType);
        this.parser = getParserClass();
        this.isParsable = parser != null;
    }

    private static record ComponentTypeResult(Class<?> componentType, boolean isRepeatableType) {}

    private ComponentTypeResult getComponentType() {
        Class<?> currentClass = this.type;
        if (currentClass.isArray()) {
            return new ComponentTypeResult(currentClass.getComponentType(), true);
        }

        do {
            AnnotatedType[] types = currentClass.getAnnotatedInterfaces();
            for (AnnotatedType annotatedType : types) {
                if (isCollectionType(annotatedType)) {
                    if (annotatedType instanceof ParameterizedType pt) {
                        Type t = pt.getActualTypeArguments()[0];
                        if (t instanceof Class<?>) {
                            return new ComponentTypeResult((Class<?>) t, true);
                        }
                    }

                    if (field != null && field.getGenericType() instanceof ParameterizedType pt) {
                        Type t = pt.getActualTypeArguments()[0];
                        return new ComponentTypeResult((Class<?>) t, true);
                    }

                    return new ComponentTypeResult(Object.class, true);
                }
            }

            AnnotatedType superType = currentClass.getAnnotatedSuperclass();
            if (superType != null && superType.getType() instanceof ParameterizedType pt) {
                Type t = pt.getActualTypeArguments()[0];
                if (t instanceof Class<?>) {
                    return new ComponentTypeResult((Class<?>) t, true);
                }
            }

            currentClass = currentClass.getSuperclass();
        } while (currentClass != null);

        return new ComponentTypeResult(this.type, false);
    }

    private static boolean isCollectionType(AnnotatedType at) {
        return at.getType() instanceof ParameterizedType pt && pt.getRawType() == Collection.class;
    }

    private static boolean isPrimitiveType(Class<?> type) {
        return type.isPrimitive() || type == String.class;
    }

    private ValueParser<?> getParserClass() {
        XmlParser annotation = field == null ? null : field.getDeclaredAnnotation(XmlParser.class);
        if(annotation == null) {
            ValueParser<?> registeredParser = RegisteredParsers.getParser(resultType, type);
            return registeredParser == null ? RegisteredParsers.getDefaultParser(type) : registeredParser;
        } else {
            try {
                //noinspection unchecked
                Constructor<ValueParser<?>> constructor =
                        (Constructor<ValueParser<?>>) annotation.value().getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
