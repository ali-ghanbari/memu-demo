package edu.iastate.memo.commons.collections;

import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

public class NonNegativeIntSetTest {
    @Test
    public void test1() {
        final NonNegativeIntSet set = new NonNegativeIntSet();
        for (int i = 0; i < 100; i++) {
            set.add(i);
        }
        for (int i = 0; i < 100; i++) {
            assertTrue(set.contains(i));
        }
        for (int i = 0; i < 100; i++) {
            if ((i % 2) == 0) {
                set.remove(i);
            }
        }
        for (int i = 0; i < 100; i++) {
            if ((i % 2) == 0) {
                assertFalse(set.contains(i));
            } else {
                assertTrue(set.contains(i));
            }
        }
    }

    @Test
    public void test2() {
        final Set<Integer> ref = new HashSet<>();
        final NonNegativeIntSet set = new NonNegativeIntSet();
        for (int i = 0; i < 100; i++) {
            ref.add(i);
            set.add(i);
        }
        assertTrue(set.containsAll(ref));
    }

    @Test
    public void test3() {
        final Set<Integer> ref = new HashSet<>();
        final NonNegativeIntSet set = new NonNegativeIntSet();
        for (int i = 0; i < 100; i++) {
            ref.add(i);
            set.add(i);
        }
        final Iterator<Integer> it = set.createIterator();
        while (it.hasNext()) {
            final int x = it.next();
            assertTrue(ref.contains(x));
        }
    }
}