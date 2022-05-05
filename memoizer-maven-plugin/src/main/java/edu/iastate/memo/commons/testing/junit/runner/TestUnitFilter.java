package edu.iastate.memo.commons.testing.junit.runner;

import edu.iastate.memo.commons.misc.NameUtils;
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.TestUnit;

import java.util.Collection;

/**
 * Test unit filter allows us to selectively run a subset of test cases.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class TestUnitFilter {
    public static Predicate<TestUnit> all() {
        return testUnit -> true;
    }

    public static Predicate<TestUnit> some(final Collection<String> testUnitNames) {
        return testUnit -> {
            final String testName = NameUtils.sanitizeExtendedTestName(testUnit.getDescription().getName());
            return testUnitNames.contains(testName);
        };
    }
}