package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.collections.SafeBoundedLongStack;

import java.util.HashMap;
import java.util.Map;

import static edu.iastate.memo.constants.Params.PROFILER_MAX_TIMING_SAMPLES;
import static edu.iastate.memo.constants.Params.STACK_NOT_A_MEMBER;
import static edu.iastate.memo.constants.Params.UNIT_SIZE;

/**
 * The class for recording time elapsed between start and end of each method.
 * We put a cap-limit on the number of times the expensive
 * <code>System.currentTimeMillis()</code> is called for each method.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class TimeRecorder {
    private static long mainThreadId;

    // using safe stacks we can have a faster and more flexible time recorder
    private static final SafeBoundedLongStack[][] TIMESTAMPS;

    private static final long[][] EXEC_STATS;

    private static final byte[][] ENTRIES_COUNT;

    static {
        TIMESTAMPS = new SafeBoundedLongStack[UNIT_SIZE][];
        EXEC_STATS = new long[UNIT_SIZE][];
        ENTRIES_COUNT = new byte[UNIT_SIZE][];
        mainThreadId = -1L;
    }

    private TimeRecorder() { }

    private static boolean reenter(final int unitIndex, final int index) {
        // all entries are pre-allocated during instrumentation
        final byte[] unit = ENTRIES_COUNT[unitIndex];
        if (unit[index] <= PROFILER_MAX_TIMING_SAMPLES) {
            unit[index]++;
            return true;
        }
        return false;
    }

    private static boolean threadSafe() {
        final long currentThreadId = Thread.currentThread().getId();
        if (mainThreadId == -1L) {
            mainThreadId = currentThreadId;
        }
        return mainThreadId == currentThreadId;
    }

    public static void registerEntryTimestamp(final int methodIndex) {
        if (threadSafe()) {
            final int unitIndex = methodIndex / UNIT_SIZE;
            final int index = methodIndex % UNIT_SIZE;
            final long timestamp;
            if (reenter(unitIndex, index)) {
                timestamp = System.currentTimeMillis();
            } else {
                timestamp = STACK_NOT_A_MEMBER;
            }
            TIMESTAMPS[unitIndex][index].push(timestamp);
        }
    }

    public static void registerExitTimestamp(final int methodIndex) {
        if (threadSafe()) {
            final int unitIndex = methodIndex / UNIT_SIZE;
            final int index = methodIndex % UNIT_SIZE;
            final long entryTS = TIMESTAMPS[unitIndex][index].pop();
            if (entryTS == STACK_NOT_A_MEMBER) {
                return;
            }
            final int timeElapsed = (int) (System.currentTimeMillis() - entryTS);
            // all entries are pre-allocated
            final long[] unit = EXEC_STATS[unitIndex];
            final long pair = unit[index];
            final int count = 1 + (int) (pair >> 32);
            final int summation = timeElapsed + (int) pair;
            unit[index] = (((long) count) << 32) | (summation & 0xFFFFFFFFL);
        }
    }

    static void allocateResources(final int methodIndex) {
        final int unitIndex = methodIndex / UNIT_SIZE;
        if (ENTRIES_COUNT[unitIndex] == null) {
            ENTRIES_COUNT[unitIndex] = new byte[UNIT_SIZE];
        }
        if (EXEC_STATS[unitIndex] == null) {
            EXEC_STATS[unitIndex] = new long[UNIT_SIZE];
        }
        if (TIMESTAMPS[unitIndex] == null) {
            TIMESTAMPS[unitIndex] = new SafeBoundedLongStack[UNIT_SIZE];
        }
        final int index = methodIndex % UNIT_SIZE;
        if (TIMESTAMPS[unitIndex][index] == null) {
            TIMESTAMPS[unitIndex][index] = new SafeBoundedLongStack();
        }
    }

    static Map<Integer, Double> getMethodsTiming() {
        final Map<Integer, Double> methodsTiming = new HashMap<>();
        int methodId = 0;
        for (final long[] unit : EXEC_STATS) {
            if (unit == null) {
                methodId += UNIT_SIZE;
                continue;
            }
            for (final long pair : unit) {
                if (pair != 0L) {
                    final int count = (int) (pair >> 32);
                    final int summation = (int) pair;
                    final double avg = ((double) summation) / count;
                    methodsTiming.put(methodId, avg);
                }
                methodId++;
            }
        }
        return methodsTiming;
    }
}