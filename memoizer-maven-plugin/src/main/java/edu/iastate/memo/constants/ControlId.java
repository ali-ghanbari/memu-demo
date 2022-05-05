package edu.iastate.memo.constants;

/**
 * Constants used for communication.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class ControlId {
    public static final byte PROFILER_REPORT_METHODS_TIMING = 1;

    public static final byte PROFILER_REPORT_METHOD_COVERAGE = 2;

    public static final byte PROFILER_REPORT_CONSTRUCTORS = 8;

    public static final byte DONE = 64;

    private ControlId() {

    }
}