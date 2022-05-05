package edu.iastate.memo.constants;

import edu.iastate.memo.commons.misc.PropertyUtils;

/**
 * Some constants for tuning our memoizer
 * 
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class Params {
    public static final int BYTECODE_CLASS_CACHE_SIZE;

    public static final int UNIT_SIZE;

    // maximum number of i/o pairs to be stored for each method
    public static final int MEMOIZER_MAX_ENTRIES;

    // maximum number of timing measurements conducted for each method
    public static final int PROFILER_MAX_TIMING_SAMPLES;

    public static final int DEFAULT_MAX_STACK_SIZE;

    public static final long STACK_NOT_A_MEMBER;

    public static final int TEST_UNIT_TIME_OUT;

    static {
        BYTECODE_CLASS_CACHE_SIZE = PropertyUtils.getIntProperty("memo.def.cache.size", 500);
        UNIT_SIZE = PropertyUtils.getIntProperty("memo.def.unit.capacity", 1024);
        MEMOIZER_MAX_ENTRIES = PropertyUtils.getIntProperty("memo.def.memoizer.max.entries", 500);
        PROFILER_MAX_TIMING_SAMPLES = PropertyUtils.getIntProperty("memo.def.profiler.max.timing", 3);
        DEFAULT_MAX_STACK_SIZE = PropertyUtils.getIntProperty("memo.def.max.stack", 10);
        STACK_NOT_A_MEMBER = PropertyUtils.getLongProperty("memo.def.stack.nam", Long.MAX_VALUE);
        TEST_UNIT_TIME_OUT = PropertyUtils.getIntProperty("memo.unit.test.timeout", 5);
    }

    private Params() {

    }
}