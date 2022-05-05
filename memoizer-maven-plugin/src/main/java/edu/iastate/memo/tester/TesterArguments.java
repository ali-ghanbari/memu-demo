package edu.iastate.memo.tester;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class TesterArguments  implements Serializable {
    private static final long serialVersionUID = 1L;

    final Collection<String> testClassNames;

    public TesterArguments(final Collection<String> testClassNames) {
        Validate.isInstanceOf(Serializable.class, testClassNames);
        this.testClassNames = testClassNames;
    }
}
