package com.codesdowork.sax.parsers;

import java.util.Date;
import java.util.HashMap;

public abstract class RegisteredParsers {

    private final static HashMap<Class<?>, HashMap<Class<?>, ValueParser<?>>> PARSERS;

    private final static HashMap<Class<?>, ValueParser<?>> DEFAULT_PARSERS;

    static {
        PARSERS = new HashMap<>();
        DEFAULT_PARSERS = PARSERS.computeIfAbsent(null, k -> new HashMap<>());
        DEFAULT_PARSERS.put(Date.class, new DateTimeParser());
    }

    public static <T> ValueParser<T> getDefaultParser(Class<T> clazz) {
        //noinspection unchecked
        return (ValueParser<T>) DEFAULT_PARSERS.get(clazz);
    }

    public static <T> ValueParser<T> getParser(Class<?> resultType, Class<T> clazz) {
        try {
            //noinspection unchecked
            return (ValueParser<T>) PARSERS.get(resultType).get(clazz);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static <T> void registerParser(Class<?> resultType, Class<T> clazz, ValueParser<T> parser) {
        HashMap<Class<?>, ValueParser<?>> typeParsers = PARSERS.computeIfAbsent(resultType, key -> new HashMap<>());
        typeParsers.put(clazz, parser);
    }
}
