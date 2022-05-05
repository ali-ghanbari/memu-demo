package edu.iastate.memo.commons.collections;

import static edu.iastate.memo.constants.Params.DEFAULT_MAX_STACK_SIZE;
import static edu.iastate.memo.constants.Params.STACK_NOT_A_MEMBER;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class SafeBoundedLongStack {
    static final long[] LONG_STACK_UNIT;

    private final long[] stack;

    private final int maxSize;

    private int top;

    private int runOver;

    static {
        LONG_STACK_UNIT = new long[DEFAULT_MAX_STACK_SIZE];
    }

    public SafeBoundedLongStack() {
        this.stack = LONG_STACK_UNIT.clone();
        this.maxSize = DEFAULT_MAX_STACK_SIZE;
    }

    public SafeBoundedLongStack(final int maxSize) {
        this.stack = new long[maxSize];
        this.maxSize = maxSize;
    }

    public void push(final long value) {
        if (this.top < this.maxSize) {
            this.stack[this.top++] = value;
        } else {
            this.runOver++;
        }
    }

    public long pop() {
        if (this.runOver > 0) {
            this.runOver--;
        } else if (this.top > 0) {
            return this.stack[--this.top];
        }
        return STACK_NOT_A_MEMBER;
    }

    public void clear() {
        this.top = 0;
        this.runOver = 0;
    }
}