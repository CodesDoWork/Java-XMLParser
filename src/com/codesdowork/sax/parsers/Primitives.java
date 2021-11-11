package com.codesdowork.sax.parsers;

public class Primitives {

    public static java.lang.constant.Constable parseValue(String value, Class<?> clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == int.class) {
            return Integer.parseInt(value);
        } else if (clazz == long.class) {
            return Long.parseLong(value);
        } else if (clazz == float.class) {
            return Float.parseFloat(value);
        } else if (clazz == double.class) {
            return Double.parseDouble(value);
        } else if (clazz == char.class) {
            return value.charAt(0);
        } else if (clazz == boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return null;
    }
}
