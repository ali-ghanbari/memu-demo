package edu.iastate.memo.memoizer.recorder;

import edu.iastate.memo.commons.process.ChildProcessReporter;

import java.io.OutputStream;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class RecorderReporter extends ChildProcessReporter {
    public RecorderReporter(OutputStream dos) {
        super(dos);
    }
}
