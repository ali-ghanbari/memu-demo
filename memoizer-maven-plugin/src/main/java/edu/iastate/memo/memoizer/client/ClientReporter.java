package edu.iastate.memo.memoizer.client;

import edu.iastate.memo.commons.process.ChildProcessReporter;

import java.io.OutputStream;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ClientReporter extends ChildProcessReporter {
    public ClientReporter(OutputStream os) {
        super(os);
    }

    public synchronized void reportMemoizationResult(final HitMissPair results) {
        this.dos.writeByte(Byte.MIN_VALUE); // unused
        this.dos.write(results);
        this.dos.flush();
    }
}
