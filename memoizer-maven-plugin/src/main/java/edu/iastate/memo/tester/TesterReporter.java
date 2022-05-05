package edu.iastate.memo.tester;

import edu.iastate.memo.commons.process.ChildProcessReporter;

import java.io.OutputStream;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class TesterReporter extends ChildProcessReporter {
    public TesterReporter(OutputStream os) {
        super(os);
    }
}