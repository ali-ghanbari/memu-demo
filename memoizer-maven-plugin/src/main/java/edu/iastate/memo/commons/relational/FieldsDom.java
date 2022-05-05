package edu.iastate.memo.commons.relational;

/**
 * Representing fields domain. It can be loaded from disk or a fresh instance
 * of the class can be obtained.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class FieldsDom extends StringDomain {
    public FieldsDom() {
        super("F");
    }

    public FieldsDom(final String pathName) {
        this();
        load(pathName);
    }
}