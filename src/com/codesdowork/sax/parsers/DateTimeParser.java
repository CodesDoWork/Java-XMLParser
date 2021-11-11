package com.codesdowork.sax.parsers;

import org.xml.sax.Attributes;

import java.time.Instant;
import java.util.Date;

public class DateTimeParser implements ValueParser<Date> {

    @Override
    public Date parse(String value, Attributes attributes) {
        return Date.from(Instant.parse(value));
    }
}
