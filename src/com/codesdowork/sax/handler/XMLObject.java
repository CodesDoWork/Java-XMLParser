package com.codesdowork.sax.handler;

import com.codesdowork.sax.annotations.XmlTag;
import com.codesdowork.sax.annotations.XmlValue;
import com.codesdowork.sax.exceptions.MalformedXMLException;
import com.codesdowork.sax.parsers.Primitives;
import org.xml.sax.Attributes;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

public class XMLObject<T> {

    private final Class<T> resultType;

    private final TagInfo rootTagInfo;

    private final String rootTag;

    private final Stack<Object> objectStack;

    private final Stack<String> path;

    private final HashMap<Object, Attributes> attributes;

    private T result;

    public XMLObject(Class<T> resultType, String rootTag) {
        this.resultType = resultType;
        this.rootTagInfo = TagInfo.getInstance(resultType, resultType);
        this.rootTag = rootTag.toLowerCase();
        this.objectStack = new Stack<>();
        this.path = new Stack<>();
        this.attributes = new HashMap<>();
    }

    public void startObject(String tag) throws MalformedXMLException {
        path.push(tag);

        final Object currentObject = objectStack.size() == 0 ? null : objectStack.peek();
        TagInfo tagInfo = null;
        try {
            tagInfo = getTagInfo();
            if (tagInfo.isPrimitiveType || tagInfo.isParsable) {
                return;
            }

            Object valueObject;
            if (tagInfo.type.isArray()) {
                valueObject = Array.newInstance(tagInfo.componentType, 0);
            } else {
                Constructor<?> constructor = tagInfo.type.getDeclaredConstructor();
                constructor.setAccessible(true);
                valueObject = constructor.newInstance();
            }

            if (currentObject != null) {
                if (tagInfo.field == null) {
                    if (currentObject.getClass().isArray()) {
                        addToArray(currentObject, valueObject, tagInfo);
                    } else if (currentObject instanceof Collection) {
                        //noinspection unchecked
                        ((Collection<Object>) currentObject).add(valueObject);
                    }
                } else {
                    tagInfo.field.setAccessible(true);
                    tagInfo.field.set(currentObject, valueObject);
                }
            }

            objectStack.push(valueObject);
        } catch (NoSuchMethodException e) {
            String classname = tagInfo.type.getSimpleName();
            throw new RuntimeException("Empty constructor required for class '" + classname + "'!");
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new MalformedXMLException(e);
        }

        if (result == null) {
            //noinspection unchecked
            result = (T) currentObject;
        }
    }

    public void endObject(String value) {
        try {
            TagInfo tagInfo = getTagInfo();
            if (tagInfo.isPrimitiveType || tagInfo.isParsable) {
                Object currentObject = objectStack.peek();

                Object valueObject = tagInfo.isParsable
                        ? tagInfo.parser.parse(value, attributes.get(currentObject))
                        : Primitives.parseValue(value, tagInfo.type);

                if(tagInfo.field == null) {
                    // array or collection
                    if(currentObject.getClass().isArray()) {
                        addToArray(currentObject, valueObject, tagInfo);
                    } else {
                        //noinspection unchecked
                        ((Collection<Object>) currentObject).add(valueObject);
                    }
                } else {
                    tagInfo.field.setAccessible(true);
                    tagInfo.field.set(currentObject, valueObject);
                }
            } else {
                Object currentObject = objectStack.pop();
                Class<?> clazz = currentObject.getClass();

                Field taggedValueField = null;
                Field valueField = null;

                Class<?> currentClass = clazz;
                while (taggedValueField == null && currentClass != null) {
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.getDeclaredAnnotation(XmlValue.class) != null) {
                            taggedValueField = field;
                            break;
                        }

                        if (field.getName().equals("value")) {
                            valueField = field;
                        }
                    }

                    currentClass = currentClass.getSuperclass();
                }

                valueField = taggedValueField == null ? valueField : taggedValueField;
                if (valueField != null) {
                    valueField.setAccessible(true);
                    valueField.set(currentObject, Primitives.parseValue(value, valueField.getType()));
                }
            }
        } catch (MalformedXMLException | IllegalAccessException e) {
            e.printStackTrace();
        }

        path.pop();
    }

    public void addAttributes(Attributes attributes) {
        Object currentObject = objectStack.peek();
        Class<?> currentClass = currentObject.getClass();
        this.attributes.put(currentObject, attributes);

        try {
            do {
                for (Field field : currentClass.getDeclaredFields()) {
                    String value = attributes.getValue(getFieldName(field));
                    if (value != null) {
                        field.setAccessible(true);
                        field.set(currentObject, Primitives.parseValue(value, field.getType()));
                    }
                }

                currentClass = currentClass.getSuperclass();
            } while (currentClass != null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public T getResult() {
        return result;
    }

    private TagInfo getTagInfo() throws MalformedXMLException {
        TagInfo currentTagInfo = rootTagInfo;

        for (String level : path) {
            if (!level.equals(rootTag)) {
                boolean found = false;

                if (currentTagInfo.isRepeatableType) {
                    if (level.equals("item") || level.equals(getClassname(currentTagInfo.componentType))) {
                        currentTagInfo = TagInfo.getInstance(resultType, currentTagInfo.componentType);
                        found = true;
                    }
                } else {
                    do {
                        for (Field field : currentTagInfo.type.getDeclaredFields()) {
                            if (level.equals(getFieldName(field))) {
                                found = true;
                                currentTagInfo = TagInfo.getInstance(resultType, field);
                                break;
                            }
                        }

                        if (!found && currentTagInfo.type.getSuperclass() != null) {
                            currentTagInfo = TagInfo.getInstance(resultType, currentTagInfo.type.getSuperclass());
                        }
                    } while (!found && currentTagInfo.type.getSuperclass() != null);
                }

                if (!found) {
                    for (Class<?> subclass : RegisteredSubclasses.getSubclasses(currentTagInfo.type)) {
                        if (getClassname(subclass).equals(level)) {
                            currentTagInfo = TagInfo.getInstance(resultType, subclass);
                            found = true;
                            break;
                        }
                    }

                    if (currentTagInfo.isRepeatableType) {
                        for (Class<?> subclass : RegisteredSubclasses.getSubclasses(currentTagInfo.componentType)) {
                            if (getClassname(subclass).equals(level)) {
                                currentTagInfo = TagInfo.getInstance(resultType, subclass);
                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        throw new MalformedXMLException("<" + level + "> not allowed!");
                    }
                }
            }
        }

        return currentTagInfo;
    }

    private void addToArray(Object arrayObject, Object valueObject, TagInfo tagInfo)
            throws MalformedXMLException, IllegalAccessException {
        int index = Array.getLength(arrayObject);
        Object newArray = Array.newInstance(tagInfo.componentType, index + 1);
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(arrayObject, 0, newArray, 0, index);

        Array.set(newArray, index, valueObject);
        objectStack.pop();
        Object parentObject = objectStack.peek();
        objectStack.push(newArray);

        String lastLevel = path.pop();
        TagInfo parentInfo = getTagInfo();
        path.push(lastLevel);

        parentInfo.field.setAccessible(true);
        parentInfo.field.set(parentObject, newArray);
    }

    private String getFieldName(Field field) {
        XmlTag fieldTag = field.getDeclaredAnnotation(XmlTag.class);
        return fieldTag == null ? field.getName().toLowerCase() : fieldTag.value();
    }

    private String getClassname(Class<?> clazz) {
        XmlTag classTag = clazz.getDeclaredAnnotation(XmlTag.class);
        return classTag == null ? clazz.getSimpleName().toLowerCase() : classTag.value();
    }
}
