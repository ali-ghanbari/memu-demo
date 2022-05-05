package edu.iastate.memo.memoizer.client;

import edu.iastate.memo.memoizer.ChildProcessCommonArgs;
import org.apache.commons.lang3.Validate;
import org.pitest.functional.predicate.Predicate;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ClientArguments extends ChildProcessCommonArgs {
    private static final long serialVersionUID = 1L;

    private final Predicate<String> isFailingTest;

    private final String provisionallyMemoizedMethodName;

    public ClientArguments(final Predicate<String> appClassFilter,
                           final Collection<String> testClassNames,
                           final Collection<String> relevantTestUnits,
                           final Collection<String> memoizedMethods,
                           final Predicate<String> failingTestFilter,
                           final String provisionallyMemoizedMethodName) {
        super(appClassFilter, testClassNames, relevantTestUnits, memoizedMethods);
        Validate.isInstanceOf(Serializable.class, failingTestFilter);
        this.isFailingTest = failingTestFilter;
        this.provisionallyMemoizedMethodName = provisionallyMemoizedMethodName;
    }

    public Predicate<String> getFailingTestPredicate() {
        return this.isFailingTest;
    }

    public String getProvisionallyMemoizedMethodName() {
        return this.provisionallyMemoizedMethodName;
    }
}
