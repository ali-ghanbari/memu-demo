package edu.iastate.memo.profiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.iastate.memo.constants.Params.UNIT_SIZE;

/**
 * This class is intended to record method coverage so as to track the set of methods
 * executed by each test unit (i.e., a test suite or a test case).
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class MethodCoverageRecorder {
    private static final long[] METHODS_BITMAP;

    public static final Map<String, long[]> COVERAGE_MAP; // test suite/case name --> set of covered methods

    public static long[] coveredMethods;

    static {
        METHODS_BITMAP = new long[UNIT_SIZE];
        Arrays.fill(METHODS_BITMAP, 0L);
        COVERAGE_MAP = new HashMap<>();
    }

    private MethodCoverageRecorder() { }

    /**
     * Adds the name of a test suite/case to <code>COVERAGE_MAP</code> and sets
     * <code>coveredMethods</code> to be used by instrumented code.
     *
     * @param testUnitName Full name of test suite or test case
     */
    static void addTestUnit(final String testUnitName) {
        coveredMethods = METHODS_BITMAP.clone();
        COVERAGE_MAP.put(testUnitName, coveredMethods);
    }

    public static void recordMethod(final int methodIndex) {
        final int unitIndex = methodIndex / Long.SIZE;
        final int bitIndex = methodIndex % Long.SIZE;
        coveredMethods[unitIndex] |= 1L << bitIndex;
    }

    static Map<String, List<Integer>> getCoverageMap() {
        final Map<String, List<Integer>> coverageMap = new HashMap<>();
        for (final Map.Entry<String, long[]> entry : COVERAGE_MAP.entrySet()) {
            final String testUnitName = entry.getKey();
            final ArrayList<Integer> list = new ArrayList<>();
            final long[] methodsBitmap = entry.getValue();
            int methodIndex = 0;
            for (long unit : methodsBitmap) {
                for (int bitIndex = 0; bitIndex < Long.SIZE; bitIndex++) {
                    if ((unit & 1L) != 0L) {
                        list.add(methodIndex);
                    }
                    unit >>>= 1;
                    methodIndex++;
                }
            }
            list.trimToSize();
            coverageMap.put(testUnitName, list);
        }
        return coverageMap;
    }
}
