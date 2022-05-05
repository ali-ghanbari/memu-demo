package edu.iastate.memo.memoizer;

import edu.utdallas.objectutils.InclusionPredicate;
import edu.utdallas.objectutils.Wrapped;
import edu.utdallas.objectutils.Wrapper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static edu.iastate.memo.commons.reflection.FieldUtils.getFieldByName;
import static org.apache.commons.lang3.reflect.FieldUtils.readStaticField;
import static org.apache.commons.lang3.reflect.FieldUtils.writeStaticField;

/**
 * A table for storing name-value pairs for static fields.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class StaticFieldValueTable implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String[] fieldNames;

    private final Wrapped[] fieldWrappedValues;

    private StaticFieldValueTable() {
        this.fieldNames = null;
        this.fieldWrappedValues = null;
    }

    public StaticFieldValueTable(final String[] fieldNames,
                                 final InclusionPredicate ip) {
        this.fieldNames = fieldNames;
        final int len = fieldNames.length;
        this.fieldWrappedValues = new Wrapped[len];
        for (int i = 0; i < len; i++) {
            try {
                final Field field = getFieldByName(fieldNames[i]);
                final Object value = readStaticField(field, true);
                this.fieldWrappedValues[i] = Wrapper.wrapObject(value, ip);
            } catch (final Exception ignored) {
                this.fieldWrappedValues[i] = null;
            }
        }
    }

    public void unwrapAndAssign() throws Exception {
        final Wrapped[] fieldWrappedValues = this.fieldWrappedValues;
        final int len = this.fieldNames.length;
        for (int i = 0; i < len; i++) {
            if (fieldWrappedValues[i] == null) {
                continue;
            }
            final Field field = getFieldByName(this.fieldNames[i]);
            // assert field is static
            Object value;
            try {
                value = readStaticField(field);
            } catch (final IllegalAccessException ignored) {
                continue; // if we can't read it for whatever reason, just ignore it
                          // like what we did in the first place
            }
            if (Modifier.isFinal(field.getModifiers())) {
                if (value == null || value.getClass().isPrimitive()) {
                    continue;
                }
                fieldWrappedValues[i].unwrap(value);
            } else {
                value = fieldWrappedValues[i].unwrap(value);
                writeStaticField(field, value);
            }
        }
    }

    public String[] getFieldNames() {
        return this.fieldNames;
    }

    public Wrapped[] getFieldWrappedValues() {
        return this.fieldWrappedValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StaticFieldValueTable)) {
            return false;
        }
        StaticFieldValueTable that = (StaticFieldValueTable) o;
        return Arrays.equals(this.fieldNames, that.fieldNames) &&
                Arrays.equals(this.fieldWrappedValues, that.fieldWrappedValues);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.fieldNames);
        result = 31 * result + Arrays.hashCode(this.fieldWrappedValues);
        return result;
    }
}