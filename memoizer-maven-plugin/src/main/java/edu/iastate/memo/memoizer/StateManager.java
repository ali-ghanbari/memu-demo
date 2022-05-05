package edu.iastate.memo.memoizer;

import edu.iastate.memo.commons.collections.NonNegativeIntSet;
import edu.iastate.memo.commons.reflection.FieldUtils;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.constants.Params;
import edu.utdallas.objectutils.InclusionPredicate;
import edu.utdallas.objectutils.ObjectUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.reflect.FieldUtils.readStaticField;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public abstract class StateManager {
    private static long mainThreadId;

    protected static MemoTable[][] memoTables;

    // mapping method index to the list of static field names that it accesses (directly/indirectly)
    protected static final String[][][] STATIC_READS;

    protected static final String[][][] STATIC_ACCESSES;

    protected static Map<Integer, NonNegativeIntSet> instanceReads;

    protected static Map<Integer, NonNegativeIntSet> instanceAccesses;

    protected static FieldsDom fieldsDom;

    protected static final ReadFieldsPredicate READ_FIELDS_PREDICATE;

    protected static final InclusionPredicate ACCESSED_FIELDS_PREDICATE;

    static {
        STATIC_READS = new String[Params.UNIT_SIZE][][];
        STATIC_ACCESSES = new String[Params.UNIT_SIZE][][];
        READ_FIELDS_PREDICATE = new ReadFieldsPredicate();
        ACCESSED_FIELDS_PREDICATE = InclusionPredicate.INCLUDE_ALL;
        mainThreadId = -1L;
    }

    protected static class ReadFieldsPredicate extends InclusionPredicate {
        private NonNegativeIntSet currentSet;

        public void selectMethod(final int methodIndex) {
            this.currentSet = instanceReads.get(methodIndex);
        }

        @Override
        public boolean test(final Field field) {
            final String fieldName = FieldUtils.getFieldName(field);
            final int fieldIndex = fieldsDom.indexOf(fieldName);
            if (fieldIndex < 0) {
                return true; // we take into account fields outside of the app
            }
            return this.currentSet != null && this.currentSet.contains(fieldIndex);
        }
    }

    protected static String[] retrieveStaticFieldNames(final String[][][] mapping,
                                                       final int unitIndex,
                                                       final int index) {
        final String[][] unit = mapping[unitIndex];
        String[] fieldIndices = EMPTY_STRING_ARRAY;;
        if (unit != null) {
            fieldIndices = unit[index];
            if (fieldIndices == null) {
                fieldIndices = EMPTY_STRING_ARRAY;
            }
        }
        return fieldIndices;
    }

    protected static boolean threadSafe() {
        final long currentThreadId = Thread.currentThread().getId();
        if (mainThreadId == -1) {
            mainThreadId = currentThreadId;
        }
        return mainThreadId == currentThreadId;
    }

//    protected static long getStaticFieldsDeepHashCode(final String[] fields) {
//        long hashCode = 0L;
//        for (final String fieldName : fields) {
//            try {
//                final Field field = FieldUtils.getFieldByName(fieldName);
//                final Object value = readStaticField(field, true);
//                hashCode = hashCode * 31L + ObjectUtils.deepHashCode(value, InclusionPredicate.INCLUDE_ALL);
//            } catch (final Throwable ignored) {
//                hashCode *= 31L;
//            }
//        }
//        return hashCode;
//    }

    protected static void initializeStateManager(final List<Integer> memoizedMethods,
                                                 final Map<Integer, List<Integer>> staticReads,
                                                 final Map<Integer, List<Integer>> staticAccesses,
                                                 final FieldsDom fieldsDom,
                                                 final Map<Integer, NonNegativeIntSet> instanceReads,
                                                 final Map<Integer, NonNegativeIntSet> instanceAccesses) {
        StateManager.fieldsDom = fieldsDom;
        StateManager.instanceReads = instanceReads;
        StateManager.instanceAccesses = instanceAccesses;
        for (final int methodIndex : memoizedMethods) {
            initializeAccesses(StateManager.STATIC_READS, fieldsDom, staticReads, methodIndex);
            initializeAccesses(StateManager.STATIC_ACCESSES, fieldsDom, staticAccesses, methodIndex);
        }
    }

    private static void initializeAccesses(final String[][][] accesses,
                                           final FieldsDom fieldsDom,
                                           final Map<Integer, List<Integer>> map,
                                           final int methodIndex) {
        final int unitIndex = methodIndex / Params.UNIT_SIZE;
        final int index = methodIndex % Params.UNIT_SIZE;
        if (accesses[unitIndex] == null) {
            accesses[unitIndex] = new String[Params.UNIT_SIZE][];
        }
        final List<Integer> list = map.get(methodIndex);
        if (list == null) {
            accesses[unitIndex][index] = null;
        } else {
            final String[] fieldNames = new String[list.size()];
            int i = 0;
            for (final int fieldIndex : list) {
                fieldNames[i++] = fieldsDom.get(fieldIndex);
            }
            accesses[unitIndex][index] = fieldNames;
        }
    }
}