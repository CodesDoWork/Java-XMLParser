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
}
