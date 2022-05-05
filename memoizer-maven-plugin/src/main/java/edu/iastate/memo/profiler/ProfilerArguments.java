package edu.iastate.memo.profiler;

import org.apache.commons.lang3.Validate;
import org.pitest.functional.predicate.Predicate;

import java.io.Serializable;
import java.util.Collection;

/**
 * Dynamic analyzer arguments are stored here.
 * This serializable object is being passed from memoizer to the child process.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ProfilerArguments implements Serializable {
    private static final long serialVersionUID = 1L;

    final Predicate<String> appClassFilter;

    final Collection<String> testClassNames;

    public ProfilerArguments(final Predicate<String> appClassFilter,
                             final Collection<String> testClassNames) {
        Validate.isInstanceOf(Serializable.class, testClassNames);
        this.appClassFilter = appClassFilter;
        this.testClassNames = testClassNames;
    }
}