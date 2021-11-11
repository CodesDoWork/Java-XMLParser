package com.codesdowork.sax.parsers;

import org.xml.sax.Attributes;

@FunctionalInterface
public interface ValueParser<T> {
    T parse(String value, Attributes attributes);
}
