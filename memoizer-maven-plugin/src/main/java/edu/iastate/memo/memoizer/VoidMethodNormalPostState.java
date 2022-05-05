package edu.iastate.memo.memoizer;

import edu.utdallas.objectutils.InclusionPredicate;
import edu.utdallas.objectutils.Wrapped;
import edu.utdallas.objectutils.Wrapper;

/**
 * Represents *normal* post state for a void method.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class VoidMethodNormalPostState extends PostState {
    private VoidMethodNormalPostState() {
        super();
    }

    private VoidMethodNormalPostState(final StaticFieldValueTable staticFieldValueTable,
                                      final Wrapped wrappedParamArray) {
        super(staticFieldValueTable, wrappedParamArray);
    }

    public static VoidMethodNormalPostState createPostState(final Object[] parameterValues,
                                                            final StaticFieldValueTable staticFieldValueTable,
                                                            final InclusionPredicate ip) {
        try {
            final Wrapped paramArrayWrapped = Wrapper.wrapObject(parameterValues, ip);
            return new VoidMethodNormalPostState(staticFieldValueTable, paramArrayWrapped);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}