package edu.iastate.memo.commons.testing.junit.runner;

import edu.iastate.memo.commons.misc.NameUtils;
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.util.Log;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class EarlyExitResultCollector implements ResultCollector {
    protected final ResultCollector child;

    protected boolean hadFailure;

    protected final Predicate<String> failingTestFilter;

    public EarlyExitResultCollector(final ResultCollector child,
                                    final Predicate<String> failingTestFilter) {
        this.child = child;
        this.failingTestFilter = failingTestFilter;
    }

    @Override
    public void notifyEnd(Description description, Throwable t) {
        this.child.notifyEnd(description, t);
        final String failingTestName = NameUtils.sanitizeExtendedTestName(description.getName());
        Log.getLogger().info("******************");
        Log.getLogger().info("" + this.hadFailure);
        Log.getLogger().info("******************");
        this.hadFailure = !this.failingTestFilter.apply(failingTestName);
    }

    @Override
    public void notifyEnd(Description description) {
        this.child.notifyEnd(description);
    }

    @Override
    public void notifyStart(Description description) {
        this.child.notifyStart(description);
    }

    @Override
    public void notifySkipped(Description description) {
        this.child.notifySkipped(description);
    }

    @Override
    public boolean shouldExit() {
        return this.hadFailure;
    }
}