package edu.iastate.memo.commons.relational;

/**
 * Representing methods domain. It can be loaded from disk or a fresh instance of
 * the class can be obtained.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class MethodsDom extends StringDomain {

    public MethodsDom() {
        super("M");
    }

    public MethodsDom(final String pathName) {
        this();
        load(pathName);
    }
}