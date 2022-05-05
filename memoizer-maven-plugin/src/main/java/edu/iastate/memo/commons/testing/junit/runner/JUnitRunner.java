package edu.iastate.memo.commons.testing.junit.runner;

import edu.iastate.memo.commons.testing.LexTestComparator;
import edu.iastate.memo.commons.testing.TestComparator;
import edu.iastate.memo.commons.testing.junit.JUnitUtils;
import edu.iastate.memo.constants.Params;
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A versatile JUnit runner based on PIT!
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class JUnitRunner {
    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    }

    private List<TestUnit> testUnits;

    private final ResultCollector resultCollector;

    public JUnitRunner(final Collection<String> classNames) {
        this(classNames, new LexTestComparator());
    }

    public JUnitRunner(final Collection<String> classNames,
                       final TestComparator testComparator) {
        final List<TestUnit> testUnits = JUnitUtils.discoverTestUnits(classNames);
        testUnits.sort(testComparator);
        this.testUnits = testUnits;
        this.resultCollector = new DefaultResultCollector();
    }

    public JUnitRunner(final Collection<String> classNames,
                       final Predicate<String> failingTestFilter) {
        this(classNames, failingTestFilter, new LexTestComparator());
    }

    public JUnitRunner(final Collection<String> classNames,
                       final Predicate<String> failingTestFilter,
                       final TestComparator testComparator) {
        final List<TestUnit> testUnits = JUnitUtils.discoverTestUnits(classNames);
        testUnits.sort(testComparator);
        this.testUnits = testUnits;
        this.resultCollector = new EarlyExitResultCollector(new DefaultResultCollector(), failingTestFilter);
    }

    public List<TestUnit> getTestUnits() {
        return this.testUnits;
    }

    public void setTestUnits(List<TestUnit> testUnits) {
        this.testUnits = testUnits;
    }

    public boolean run() {
        return run(TestUnitFilter.all());
    }

    public boolean run(final Predicate<TestUnit> shouldRun) {
        for (final TestUnit testUnit : this.testUnits) {
            if (!shouldRun.apply(testUnit)) {
                continue;
            }
            final Runnable task = () -> testUnit.execute(JUnitRunner.this.resultCollector);
            try {
                EXECUTOR_SERVICE.submit(task).get(Params.TEST_UNIT_TIME_OUT, TimeUnit.MINUTES);
            } catch (TimeoutException e) {
                System.out.println("WARNING: Running test case is terminated due to TIME_OUT.");
                return false;
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("WARNING: Running test case is terminated.");
                return false;
            }
            if (this.resultCollector.shouldExit()) {
                System.out.println("WARNING: Running test cases is terminated.");
                return false;
            }
        }
        return true;
    }
}