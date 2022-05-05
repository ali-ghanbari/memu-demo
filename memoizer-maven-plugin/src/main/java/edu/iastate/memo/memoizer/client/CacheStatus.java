package edu.iastate.memo.memoizer.client;

import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.constants.Params;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class CacheStatus {
    private static final int[] INT_UNIT;

    private static final int[][] HITS;

    private static final int[][] MISSES;

    static {
        INT_UNIT = new int[Params.UNIT_SIZE];
        Arrays.fill(INT_UNIT, 0);
        HITS = new int[Params.UNIT_SIZE][];
        MISSES = new int[Params.UNIT_SIZE][];
    }

    public static void recordCacheHit(final int methodIndex) {
        final int unitIndex = methodIndex / Params.UNIT_SIZE;
        int[] unit = HITS[unitIndex];
        if (unit == null) {
            unit = INT_UNIT.clone();
            HITS[unitIndex] = unit;
        }
        unit[methodIndex % Params.UNIT_SIZE]++;
    }

    public static void recordCacheMiss(final int methodIndex) {
        final int unitIndex = methodIndex / Params.UNIT_SIZE;
        int[] unit = MISSES[unitIndex];
        if (unit == null) {
            unit = INT_UNIT.clone();
            MISSES[unitIndex] = unit;
        }
        unit[methodIndex % Params.UNIT_SIZE]++;
    }

    public static int getHits(final int methodIndex) {
        final int unitIndex = methodIndex / Params.UNIT_SIZE;
        int[] unit = HITS[unitIndex];
        if (unit == null) {
            return 0;
        }
        return unit[methodIndex % Params.UNIT_SIZE];
    }

    public static int getMisses(final int methodIndex) {
        final int unitIndex = methodIndex / Params.UNIT_SIZE;
        int[] unit = MISSES[unitIndex];
        if (unit == null) {
            return 0;
        }
        return unit[methodIndex % Params.UNIT_SIZE];
    }

    public static boolean checkAllMethodsHaveHits(final Collection<String> methods,
                                                  final MethodsDom methodsDom) {
        for (final String methodName : methods) {
            if (getHits(methodsDom.indexOf(methodName)) == 0) {
                return false;
            }
        }
        return true;
    }

    public static void reset() {
        Arrays.fill(HITS, null);
        Arrays.fill(MISSES, null);
    }
}