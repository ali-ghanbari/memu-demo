package edu.iastate.memo.memoizer;

import edu.utdallas.objectutils.InclusionPredicate;
import edu.utdallas.objectutils.Wrapped;
import edu.utdallas.objectutils.Wrapper;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class MethodExceptionalPostState extends PostState {
    private final Wrapped wrappedThrowable;

    private MethodExceptionalPostState() {
        super();
        this.wrappedThrowable = null;
    }

    public MethodExceptionalPostState(final StaticFieldValueTable staticFieldValueTable,
                                      final Wrapped wrappedParamArray,
                                      final Wrapped wrappedThrowable) {
        super(staticFieldValueTable, wrappedParamArray);
        this.wrappedThrowable = wrappedThrowable;
    }

    public Wrapped getWrappedThrowable() {
        return this.wrappedThrowable;
    }

    public static MethodExceptionalPostState createPostState(final Object[] parameterValues,
                                                               final Throwable throwable,
                                                               final StaticFieldValueTable staticFieldValueTable,
                                                               final InclusionPredicate ip) {
        try {
            final Wrapped paramArrayWrapped = Wrapper.wrapObject(parameterValues, ip);
            final Wrapped wrappedThrowable = Wrapper.wrapObject(throwable, ip);
            return new MethodExceptionalPostState(staticFieldValueTable, paramArrayWrapped, wrappedThrowable);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
