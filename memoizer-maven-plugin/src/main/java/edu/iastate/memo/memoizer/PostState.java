package edu.iastate.memo.memoizer;

import edu.utdallas.objectutils.Wrapped;

import java.io.Serializable;

/**
 * Base class for post state.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public abstract class PostState implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final StaticFieldValueTable staticFieldValueTable;

    protected final Wrapped wrappedParamArray;

    protected PostState() {
        this.staticFieldValueTable = null;
        this.wrappedParamArray = null;
    }

    protected PostState(final StaticFieldValueTable staticFieldValueTable,
                        final Wrapped wrappedParamArray) {
        this.staticFieldValueTable = staticFieldValueTable;
        this.wrappedParamArray = wrappedParamArray;
    }

    public StaticFieldValueTable getStaticFieldValueTable() {
        return this.staticFieldValueTable;
    }

    public Wrapped getWrappedParamArray() {
        return this.wrappedParamArray;
    }
}
