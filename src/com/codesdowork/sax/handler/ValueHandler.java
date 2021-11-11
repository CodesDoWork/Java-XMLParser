package com.codesdowork.sax.handler;

import com.codesdowork.utils.Strings;

public class ValueHandler {

    private String currentValue = Strings.EMPTY;

    public void add(String s) {
        currentValue += s;
    }

    public String getValue() {
        String result = currentValue.trim();
        currentValue = Strings.EMPTY;

        return result;
    }

    private char firstChar() {
        return currentValue.charAt(0);
    }

    private char lastChar() {
        return currentValue.charAt(currentValue.length() - 1);
    }

    private boolean isRemovable(char c) {
        return c == ' ' || c == '\r' || c == '\n';
    }

    @FunctionalInterface
    private interface CharFunction {
        char getChar();
    }
}
