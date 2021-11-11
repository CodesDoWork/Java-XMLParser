package com.codesdowork.sax.annotations;

import com.codesdowork.sax.parsers.ValueParser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface XmlParser {
    Class<? extends ValueParser<?>> value();
}
