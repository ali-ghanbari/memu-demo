package edu.iastate.memo.memoizer;

import edu.iastate.memo.constants.Params;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Method-level memo table: a table for storing I/O pairs for a method.
 * For performance reasons, we use hashcode of input states as keys.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class MemoTable extends HashMap<Long, PostState> implements Serializable {
    private static final long serialVersionUID = 1L;

    public MemoTable() {
        super(Params.UNIT_SIZE);
    }
}