package com.codesdowork.sax.writer;

import com.codesdowork.sax.annotations.XmlTag;
import com.codesdowork.sax.handler.TagInfo;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

public class XMLWriter {

    private Class<?> resultType;

    public <T> String toXML(T obj) {
        resultType = obj.getClass();
        try (PrettyXmlWriter writer = new PrettyXmlWriter()) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");

            return toXML(writer, obj).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private <T> StringWriter toXML(PrettyXmlWriter writer, T obj) throws IOException {
        Class<?> clazz = obj.getClass();

        String tag = clazz.getSimpleName().toLowerCase();
        writer.writeTag(tag, TagType.Opening);
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if(Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                XmlTag xmlTag = field.getDeclaredAnnotation(XmlTag.class);
                String fieldName = xmlTag == null ? field.getName().toLowerCase() : xmlTag.value();
                Class<?> fieldType = field.getType();

                writer.writeTag(fieldName, TagType.Opening);
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);

                    TagInfo tagInfo = TagInfo.getInstance(resultType, field);
                    if (tagInfo.isPrimitiveType) {
                        writer.writeValue(String.valueOf(value));
                    } else if (tagInfo.isRepeatableType) {
                        if (fieldType.isArray()) {
                            for (Object listItem : (Object[]) value) {
                                toXML(writer, listItem);
                            }
                        } else {
                            // Collection
                            for (Object listItem : (Collection<?>) value) {
                                toXML(writer, listItem);
                            }
                        }
                    } else {
                        toXML(writer, value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                writer.writeTag(fieldName, TagType.Closing);
            }

            clazz = clazz.getSuperclass();
        } while (clazz != null); writer.writeTag(tag, TagType.Closing);

        return writer;
    }
}
