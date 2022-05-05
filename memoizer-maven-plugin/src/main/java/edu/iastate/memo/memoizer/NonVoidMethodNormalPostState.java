package edu.iastate.memo.memoizer;

import edu.utdallas.objectutils.InclusionPredicate;
import edu.utdallas.objectutils.Wrapped;
import edu.utdallas.objectutils.Wrapper;

/**
 * Represents *normal* post state for a non-void method.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class NonVoidMethodNormalPostState extends PostState {
    private final Wrapped wrappedReturnValue;

    private NonVoidMethodNormalPostState() {
        super();
        this.wrappedReturnValue = null;
    }

    public NonVoidMethodNormalPostState(final StaticFieldValueTable staticFieldValueTable,
                                        final Wrapped wrappedParamArray,
                                        final Wrapped wrappedReturnValue) {
        super(staticFieldValueTable, wrappedParamArray);
        this.wrappedReturnValue = wrappedReturnValue;
    }

    public Wrapped getWrappedReturnValue() {
        return this.wrappedReturnValue;
    }

    public static NonVoidMethodNormalPostState createPostState(final Object[] parameterValues,
                                                               final Object returnValue,
                                                               final StaticFieldValueTable staticFieldValueTable,
                                                               final InclusionPredicate ip) {
        try {
            final Wrapped paramArrayWrapped = Wrapper.wrapObject(parameterValues, ip);
            final Wrapped wrappedReturnValue = Wrapper.wrapObject(returnValue, ip);
            return new NonVoidMethodNormalPostState(staticFieldValueTable, paramArrayWrapped, wrappedReturnValue);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}