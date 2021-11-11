package com.codesdowork.sax.exceptions;

import java.io.IOException;

public class MalformedXMLException extends IOException {

    public MalformedXMLException(String message) {
        super(message);
    }

    public MalformedXMLException(Exception e) {
        super(e);
    }
}
