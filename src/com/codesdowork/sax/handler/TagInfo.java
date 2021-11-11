package com.codesdowork.sax.handler;

import com.codesdowork.sax.annotations.XmlParser;
import com.codesdowork.sax.parsers.RegisteredParsers;
import com.codesdowork.sax.parsers.ValueParser;

import java.lang.reflect.AnnotatedType;
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
        this.componentType = getComponentType(type);
        this.isRepeatableType = this.componentType != this.type;
        this.isPrimitiveType = isPrimitiveType(componentType);
        this.parser = getParserClass();
        this.isParsable = parser != null;
    }

    private static Class<?> getComponentType(Class<?> type) {
        Class<?> originalType = type;
        if (type.isArray()) {
            return type.getComponentType();
        }

        do {
            AnnotatedType[] types = type.getAnnotatedInterfaces();
            for (AnnotatedType annotatedType : types) {
                if (isCollectionType(annotatedType) && annotatedType instanceof ParameterizedType pt) {
                    Type t = pt.getActualTypeArguments()[0];
                    if (t instanceof Class<?>) {
                        return (Class<?>) t;
                    }
                }
            }

            AnnotatedType superType = type.getAnnotatedSuperclass();
            if (superType != null && superType.getType() instanceof ParameterizedType pt) {
                Type t = pt.getActualTypeArguments()[0];
                if (t instanceof Class<?>) {
                    return (Class<?>) t;
                }
            }

            type = type.getSuperclass();
        } while (type != null);

        return originalType;
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
                return annotation.value().getDeclaredConstructor().newInstance();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
