package com.codesdowork.sax.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class RegisteredSubclasses {

    private final static HashMap<Class<?>, HashSet<Class<?>>> SUBCLASSES = new HashMap<>();

    public static void registerSubclass(Class<?> superclass, Class<?> subclass) {
        Objects.requireNonNull(superclass);
        Objects.requireNonNull(subclass);

        Class<?> currentClass = subclass;
        while (currentClass != null && currentClass.getSuperclass() != superclass) {
            currentClass = currentClass.getSuperclass();
        }

        if (currentClass == null) {
            String message = subclass.getSimpleName() + " is no subclass of " + superclass.getSimpleName();
            throw new IllegalArgumentException(message);
        }

        SUBCLASSES.computeIfAbsent(superclass, k -> new HashSet<>()).add(subclass);
    }

    public static Set<Class<?>> getSubclasses(Class<?> superclass) {
        return SUBCLASSES.containsKey(superclass) ? Collections.unmodifiableSet(SUBCLASSES.get(superclass)) : Set.of();
    }
}
