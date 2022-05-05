package edu.iastate.memo.memoizer.client;

import edu.iastate.memo.commons.collections.NonNegativeIntSet;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.constants.Params;
import edu.iastate.memo.memoizer.MemoTable;
import edu.iastate.memo.memoizer.MemoTableDatabase;
import edu.iastate.memo.memoizer.MethodExceptionalPostState;
import edu.iastate.memo.memoizer.NonVoidMethodNormalPostState;
import edu.iastate.memo.memoizer.PostState;
import edu.iastate.memo.memoizer.StateManager;
import edu.iastate.memo.memoizer.VoidMethodNormalPostState;
import edu.utdallas.objectutils.InclusionPredicate;
import edu.utdallas.objectutils.ObjectUtils;
import org.pitest.functional.Option;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class ClientStateManager extends StateManager {
    private ClientStateManager() { }

    /**
     * Retrieves cached state for a void method
     *
     * @param methodIndex method index as per the loaded methods domain
     * @param parameterValues values of parameters (including <code>this</code>, if applicable) to be searched
     * @param outputTemplate templates for the retrieved objects (each slot could be <code>null</code>)
     * @return <code>true</code> iff cache hit & success
     */
    public static boolean retrieveVoidMethodOutput(final int methodIndex,
                                                   final Object[] parameterValues,
                                                   final Object[] outputTemplate) throws Throwable {
//        if (!threadSafe()) {
//            return false;
//        }
        final PostState postState = retrievePostState(methodIndex, parameterValues);
        if (postState instanceof VoidMethodNormalPostState) {
            postState.getStaticFieldValueTable().unwrapAndAssign();
            postState.getWrappedParamArray().unwrap(outputTemplate);
        } else if (postState instanceof MethodExceptionalPostState) {
            throw ((MethodExceptionalPostState) postState).getWrappedThrowable().<Throwable>unwrap();
        } else {
            return false; // cache miss
        }
        return true;
    }

    /**
     * Retrieves cached state for a void method
     *
     * @param methodIndex method index as per the loaded methods domain
     * @param parameterValues values of parameters (including <code>this</code>, if applicable) to be searched
     * @param outputTemplate templates for the retrieved objects (each slot could be <code>null</code>);
     *                       this includes the reference to the return object.
     * @return Some object value iff cache hit & success, None otherwise
     */
    public static Option<Object> retrieveNonVoidMethodOutput(final int methodIndex,
                                                             final Object[] parameterValues,
                                                             final Object[] outputTemplate) throws Throwable {
//        if (!threadSafe()) {
//            return Option.none();
//        }
        final PostState postState = retrievePostState(methodIndex, parameterValues);
        if (postState instanceof NonVoidMethodNormalPostState) {
            postState.getStaticFieldValueTable().unwrapAndAssign();
            postState.getWrappedParamArray().unwrap(outputTemplate);
            return Option.some(((NonVoidMethodNormalPostState) postState).getWrappedReturnValue().unwrap());
        } else if (postState instanceof MethodExceptionalPostState) {
            throw ((MethodExceptionalPostState) postState).getWrappedThrowable().<Throwable>unwrap();
        } else {
            return Option.none(); // cache miss
        }
    }

    private static PostState retrievePostState(final int methodIndex,
                                               final Object[] parameterValues) throws Exception {
        final int unitIndex = methodIndex / Params.UNIT_SIZE;
        final int index = methodIndex % Params.UNIT_SIZE;
//        final String[] fieldNames = retrieveStaticFieldNames(STATIC_READS, unitIndex, index);
//        READ_FIELDS_PREDICATE.selectMethod(methodIndex);
//        long preState = getStaticFieldsDeepHashCode(fieldNames);
//        preState = preState * 31L + ObjectUtils.deepHashCode(parameterValues, InclusionPredicate.INCLUDE_ALL);
        final long preState = ObjectUtils.deepHashCode(parameterValues, READ_FIELDS_PREDICATE);
        final MemoTable memoTable = memoTables[unitIndex][index];
        if (memoTable == null) {
            throw new IllegalStateException();
        }
        return memoTable.get(preState);
    }

    public static void initializeClientStateManager(final MethodsDom methodsDom,
                                                    final FieldsDom fieldsDom,
                                                    final Map<Integer, List<Integer>> staticReads,
                                                    final Map<Integer, List<Integer>> staticAccesses,
                                                    final Map<Integer, NonNegativeIntSet> instanceReads,
                                                    final Map<Integer, NonNegativeIntSet> instanceAccesses,
                                                    final Collection<String> memoizedMethodNames,
                                                    final MemoTableDatabase database) {
        final List<Integer> memoizedMethods = methodsDom.getIndices(memoizedMethodNames);
        initializeStateManager(memoizedMethods, staticReads, staticAccesses, fieldsDom, instanceReads, instanceAccesses);
        memoTables = database.getMemoTables();
    }
}
