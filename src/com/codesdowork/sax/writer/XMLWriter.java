package com.codesdowork.sax.writer;

import com.codesdowork.sax.annotations.XmlTag;
import com.codesdowork.sax.handler.TagInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

public class XMLWriter {

    private Class<?> rootType;

    private String rootTag;

    private PrettyXmlWriter writer;

    public <T> String toXML(T obj) {
        return toXML(obj, null);
    }

    public <T> String toXML(T obj, String rootTag) {
        this.rootType = obj.getClass();
        this.rootTag = rootTag;

        try (PrettyXmlWriter writer = new PrettyXmlWriter()) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
            this.writer = writer;

            return writeToXML(obj, TagInfo.getInstance(rootType, rootType)).toString();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    private <T> PrettyXmlWriter writeToXML(T obj, TagInfo classInfo) throws IOException {
        if (obj == null) {
            return writer;
        }

        Class<?> clazz = obj.getClass();
        String tag = clazz.getSimpleName().toLowerCase();


        if (classInfo.isRepeatableType) {
            if (clazz == rootType) {
                writer.writeTag(rootTag, TagType.Opening);
                processRepeatable(obj, classInfo);
                writer.writeTag(rootTag, TagType.Closing);
            } else {
                processRepeatable(obj, classInfo);
            }
        } else {
            writer.writeTag(tag, TagType.Opening);

            do {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        try {
                            field.setAccessible(true);
                            Object value = field.get(obj);
                            if (value != null) {
                                XmlTag xmlTag = field.getDeclaredAnnotation(XmlTag.class);
                                String fieldName = xmlTag == null ? field.getName().toLowerCase() : xmlTag.value();

                                writer.writeTag(fieldName, TagType.Opening);
                                writeValue(field, value);
                                writer.writeTag(fieldName, TagType.Closing);
                            }
                        } catch (IllegalAccessException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }

                clazz = clazz.getSuperclass();
            } while (clazz != null);

            writer.writeTag(tag, TagType.Closing);
        }

        return writer;
    }

    private void writeValue(Field field, Object value) throws IOException {
        TagInfo tagInfo = TagInfo.getInstance(rootType, field);
        if (tagInfo.isPrimitiveType) {
            writer.writeValue(String.valueOf(value));
        } else {
            writeToXML(value, tagInfo);
        }
    }

    private void processRepeatable(Object obj, TagInfo tagInfo) throws IOException {
        if (tagInfo.type.isArray()) {
            for (Object listItem : (Object[]) obj) {
                writeToXML(listItem, TagInfo.getInstance(rootType, tagInfo.componentType));
            }
        } else {
            for (Object listItem : (Collection<?>) obj) {
                writeToXML(listItem, TagInfo.getInstance(rootType, tagInfo.componentType));
            }
        }

    }
}
