package edu.iastate.memo.commons.misc;

import edu.iastate.memo.commons.reflection.FieldUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class FieldUtilsTest {
    @Test
    @Ignore
    public void testGetFieldByName() throws Exception {
        Field f = FieldUtils.getFieldByName("sun.reflect.Reflection.fieldFilterMap");
        assertNull(f);
        f = FieldUtils.getFieldByName("java.util.HashMap.EMPTY_TABLE");
        assertNotNull(f);
        f = FieldUtils.getFieldByName("java.util.HashMap$Entry.next");
        assertNotNull(f);
    }
}