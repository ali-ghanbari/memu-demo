package edu.iastate.memo.memoizer.recorder;

import edu.iastate.memo.memoizer.ChildProcessCommonArgs;
import org.pitest.functional.predicate.Predicate;

import java.util.Collection;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class RecorderArguments extends ChildProcessCommonArgs {
    private static final long serialVersionUID = 1L;

    public RecorderArguments(final Predicate<String> appClassFilter,
                             final Collection<String> testClassNames,
                             final Collection<String> relevantTestUnits,
                             final Collection<String> memoizedMethods) {
        super(appClassFilter, testClassNames, relevantTestUnits, memoizedMethods);
    }
}
