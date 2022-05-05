package edu.iastate.memo.commons.reflection;

import java.lang.reflect.Field;

import static org.apache.commons.lang3.reflect.FieldUtils.getField;

/**
 * More field-related utility!
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class FieldUtils {
    private FieldUtils() {

    }

    public static Field getFieldByName(String fieldFullName) throws Exception {
        final int indexOfLastDot = fieldFullName.lastIndexOf('.');
        final String className = fieldFullName.substring(0, indexOfLastDot);
        final String fieldName = fieldFullName.substring(1 + indexOfLastDot);
        final Class<?> clazz = Thread.currentThread()
                .getContextClassLoader()
                .loadClass(className);
        return getField(clazz, fieldName, true);
    }

    public static String getFieldName(final Field field) {
        return field.getDeclaringClass().getName().concat(".").concat(field.getName());
    }
}
