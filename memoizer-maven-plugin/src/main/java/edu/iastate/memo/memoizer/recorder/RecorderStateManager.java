package edu.iastate.memo.memoizer.recorder;

import edu.iastate.memo.commons.collections.LongStack;
import edu.iastate.memo.commons.collections.NonNegativeIntSet;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.constants.Params;
import edu.iastate.memo.memoizer.StaticFieldValueTable;
import edu.iastate.memo.memoizer.MemoTable;
import edu.iastate.memo.memoizer.MemoTableDatabase;
import edu.iastate.memo.memoizer.MethodExceptionalPostState;
import edu.iastate.memo.memoizer.NonVoidMethodNormalPostState;
import edu.iastate.memo.memoizer.PostState;
import edu.iastate.memo.memoizer.StateManager;
import edu.iastate.memo.memoizer.VoidMethodNormalPostState;
import edu.utdallas.objectutils.InclusionPredicate;
import edu.utdallas.objectutils.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class RecorderStateManager extends StateManager {
    // mapping method index to a call stack of input hash codes
    private static final LongStack[][] INPUTS;

    // mapping method index to the total number of times the method is invoked
    private static final int[][] TOTAL_INVOCATIONS;

    static {
        INPUTS = new LongStack[Params.UNIT_SIZE][];
        TOTAL_INVOCATIONS = new int[Params.UNIT_SIZE][];
    }

    private RecorderStateManager() { }

    private static boolean reenter(final int unitIndex, final int index) {
        // this array should already be populated during instrumentation
        // while we retrieve the code of each method
        return (TOTAL_INVOCATIONS[unitIndex][index]++) < Params.MEMOIZER_MAX_ENTRIES;
    }

    /**
     * To be called in *every* methods entry
     * @param methodIndex method index as per the loaded methods domain
     * @param parameterValues values of parameters
     */
    public static void submitInput(final int methodIndex, final Object[] parameterValues) {
        if (threadSafe()) {
            final int unitIndex = methodIndex / Params.UNIT_SIZE;
            final int index = methodIndex % Params.UNIT_SIZE;
            // all the relevant slots of this array should have been allocated during instrumentation
            final LongStack stack = INPUTS[unitIndex][index];
            long preState = Params.STACK_NOT_A_MEMBER;
            if (reenter(unitIndex, index)) {
                try {
                    // since we have already populated this array, we are sure that
                    // we will not get NullPointerException or ArrayOutOfBoundException
                    final String[] fieldNames = retrieveStaticFieldNames(STATIC_READS, unitIndex, index);
//                    READ_FIELDS_PREDICATE.selectMethod(methodIndex);
//                    preState = getStaticFieldsDeepHashCode(fieldNames);
//                    preState = preState * 31L + ObjectUtils.deepHashCode(parameterValues);
                    preState = ObjectUtils.deepHashCode(parameterValues);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
            stack.push(preState);
        }
    }

    /**
     * To be called in *normal* exit points for void methods
     * @param methodIndex method index as per the loaded methods domain
     * @param parameterValues values of parameters (including <code>this</code>, if applicable)
     */
    public static void submitOutput(final int methodIndex,
                                    final Object[] parameterValues) {
        if (threadSafe()) {
            final int unitIndex = methodIndex / Params.UNIT_SIZE;
            final int index = methodIndex % Params.UNIT_SIZE;
            // the array INPUTS is already populated during calls to submitInput
            final LongStack stack = INPUTS[unitIndex][index];
            final long preState = stack.pop();
            if (preState != Params.STACK_NOT_A_MEMBER) {
                // all the relevant slots for this array is already allocated during instrumentation or initialization
                final MemoTable memoTable = memoTables[unitIndex][index];
                // TODO: Check what happens if we remove this check
                if (!memoTable.containsKey(preState)) {
                    final String[] fieldNames;
                    fieldNames = retrieveStaticFieldNames(STATIC_ACCESSES, unitIndex, index);
                    final StaticFieldValueTable staticFieldValueTable;
                    staticFieldValueTable = new StaticFieldValueTable(fieldNames, ACCESSED_FIELDS_PREDICATE);
                    final PostState postState = VoidMethodNormalPostState.createPostState(parameterValues,
                            staticFieldValueTable, InclusionPredicate.INCLUDE_ALL);
                    memoTable.put(preState, postState);
                }
            }
        }
    }

    /**
     * To be called at *normal* exit points for non-void methods
     * @param methodIndex method index as per the loaded methods domain
     * @param parameterValues values of parameters (including <code>this</code>, if applicable)
     * @param returnValue the value that the memoized method is to return
     */
    public static void submitOutput(final Object returnValue,
                                    final int methodIndex,
                                    final Object[] parameterValues) {
        if (threadSafe()) {
            final int unitIndex = methodIndex / Params.UNIT_SIZE;
            final int index = methodIndex % Params.UNIT_SIZE;
            // the array INPUTS is already populated
            final LongStack stack = INPUTS[unitIndex][index];
            final long preState = stack.pop();
            if (preState != Params.STACK_NOT_A_MEMBER) {
                final MemoTable memoTable = memoTables[unitIndex][index];
                // TODO: Check what happens if we remove this check
                if (!memoTable.containsKey(preState)) {
                    final String[] fieldNames;
                    fieldNames = retrieveStaticFieldNames(STATIC_ACCESSES, unitIndex, index);
                    final StaticFieldValueTable staticFieldValueTable;
                    staticFieldValueTable = new StaticFieldValueTable(fieldNames, ACCESSED_FIELDS_PREDICATE);
                    final PostState postState = NonVoidMethodNormalPostState.createPostState(parameterValues,
                            returnValue, staticFieldValueTable, InclusionPredicate.INCLUDE_ALL);
                    memoTable.put(preState, postState);
                }
            }
        }
    }

    /**
     * To be called at *exceptional* exit points for void or non-void methods
     * @param methodIndex method index as per the loaded methods domain
     * @param parameterValues values of parameters (including <code>this</code>, if applicable)
     * @param throwable thrown exception
     */
    public static void submitOutput(final Throwable throwable,
                                    final int methodIndex,
                                    final Object[] parameterValues) {
        if (threadSafe()) {
            final int unitIndex = methodIndex / Params.UNIT_SIZE;
            final int index = methodIndex % Params.UNIT_SIZE;
            // the array INPUTS is already populated
            final LongStack stack = INPUTS[unitIndex][index];
            final long preState = stack.pop();
            if (preState != Params.STACK_NOT_A_MEMBER) {
                final MemoTable memoTable = memoTables[unitIndex][index];
                // TODO: Check what happens if we remove this check
                if (!memoTable.containsKey(preState)) {
                    final String[] fieldNames;
                    fieldNames = retrieveStaticFieldNames(STATIC_ACCESSES, unitIndex, index);
                    final StaticFieldValueTable staticFieldValueTable;
                    staticFieldValueTable = new StaticFieldValueTable(fieldNames, ACCESSED_FIELDS_PREDICATE);
                    final PostState postState = MethodExceptionalPostState.createPostState(parameterValues,
                            throwable, staticFieldValueTable, InclusionPredicate.INCLUDE_ALL);
                    memoTable.put(preState, postState);
                }
            }
        }
    }

    static MemoTableDatabase initializeRecorder(final MethodsDom methodsDom,
                                                final FieldsDom fieldsDom,
                                                final Map<Integer, List<Integer>> staticReads,
                                                final Map<Integer, List<Integer>> staticAccesses,
                                                final Map<Integer, NonNegativeIntSet> instanceReads,
                                                final Map<Integer, NonNegativeIntSet> instanceAccesses,
                                                final Collection<String> memoizedMethodNames) {
        final List<Integer> memoizedMethods = methodsDom.getIndices(memoizedMethodNames);
        initializeStateManager(memoizedMethods, staticReads, staticAccesses, fieldsDom, instanceReads, instanceAccesses);
        final MemoTableDatabase database = new MemoTableDatabase(memoizedMethods);
        memoTables = database.getMemoTables();
        for (final int methodIndex : memoizedMethods) {
            final int unitIndex = methodIndex / Params.UNIT_SIZE;
            final int index = methodIndex % Params.UNIT_SIZE;
            if (INPUTS[unitIndex] == null) {
                INPUTS[unitIndex] = new LongStack[Params.UNIT_SIZE];
            }
            INPUTS[unitIndex][index] = new LongStack();
            TOTAL_INVOCATIONS[unitIndex] = new int[Params.UNIT_SIZE];
        }
        return database;
    }
}
