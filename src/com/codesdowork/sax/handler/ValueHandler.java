package com.codesdowork.sax.handler;

import com.codesdowork.utils.Strings;

public class ValueHandler {

    private String currentValue = Strings.EMPTY;

    public void add(String s) {
        currentValue += s;
    }

    public String getValue() {
        clearEnding(true);
        clearEnding(false);
        String result = currentValue;
        currentValue = Strings.EMPTY;

        return result;
    }

    private void clearEnding(boolean isStart) {
        if (currentValue.isEmpty()) {
            return;
        }

        CharFunction f = isStart ? this::firstChar : this::lastChar;
        for (char c = f.getChar(); isRemovable(c); c = f.getChar()) {
            int startIndex = isStart ? 1 : 0;
            currentValue = currentValue.substring(startIndex, currentValue.length() + startIndex - 1);
            if (currentValue.isEmpty()) {
                return;
            }
        }
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
