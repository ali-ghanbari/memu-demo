package edu.iastate.memo.commons.testing;

import edu.iastate.memo.commons.misc.NameUtils;
import org.pitest.testapi.TestUnit;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class LexTestComparator implements TestComparator {
    @Override
    public int compare(final TestUnit tu1, final TestUnit tu2) {
        final String n1 = NameUtils.sanitizeExtendedTestName(tu1.getDescription().getName());
        final String n2 = NameUtils.sanitizeExtendedTestName(tu2.getDescription().getName());
        return n1.compareTo(n2);
    }
}
