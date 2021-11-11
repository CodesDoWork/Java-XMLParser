package com.codesdowork.sax;

import com.codesdowork.sax.handler.SAXHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;

public abstract class SAX {

    private static SAXParser parser;

    static {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            parser = factory.newSAXParser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parse(String xml, DefaultHandler handler) {
        try {
            parser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (IOException | SAXException e) {
            System.err.println("Error parsing " + xml + ": " + e);
            e.printStackTrace();
        }
    }

    public static <T> T parseToObject(String xml, SAXHandler<T> handler) {
        parse(xml, handler);
        return handler.getResult();
    }
}
