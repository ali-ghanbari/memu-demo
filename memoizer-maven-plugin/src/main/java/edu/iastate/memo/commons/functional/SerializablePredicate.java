package edu.iastate.memo.commons.functional;

import org.pitest.functional.predicate.Predicate;

import java.io.Serializable;

/**
 *
 * @param <T>
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public interface SerializablePredicate<T extends Serializable> extends Predicate<T>, Serializable {

}
