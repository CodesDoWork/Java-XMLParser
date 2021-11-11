package com.codesdowork.sax.handler;

import com.codesdowork.sax.exceptions.MalformedXMLException;
import com.codesdowork.sax.parsers.RegisteredParsers;
import com.codesdowork.sax.parsers.ValueParser;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SAXHandler<T> extends DefaultHandler {

    private final Class<T> resultType;

    private final XMLObject<T> xmlObject;

    private final ValueHandler valueHandler;

    public SAXHandler(Class<T> resultType) {
        this(resultType, resultType.getSimpleName());
    }

    public SAXHandler(Class<T> resultType, String rootTag) {
        this.resultType = resultType;
        this.xmlObject = new XMLObject<>(resultType, rootTag);
        this.valueHandler = new ValueHandler();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        try {
            xmlObject.startObject(getTag(localName, qName));
            if (attributes.getLength() > 0) {
                xmlObject.addAttributes(attributes);
            }
        } catch (MalformedXMLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        xmlObject.endObject(valueHandler.getValue());
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        valueHandler.add(new String(ch, start, length));
    }

    private String getTag(String localName, String qName) {
        return (localName.isBlank() ? qName : localName).toLowerCase();
    }

    public T getResult() {
        return xmlObject.getResult();
    }

    public <S> void registerParser(Class<S> clazz, ValueParser<S> parser) {
        RegisteredParsers.registerParser(resultType, clazz, parser);
    }
}
