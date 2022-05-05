package edu.iastate.memo.commons.process;

import edu.iastate.memo.constants.ControlId;
import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataOutputStream;

import java.io.OutputStream;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public abstract class ChildProcessReporter {
    protected final SafeDataOutputStream dos;

    protected ChildProcessReporter(OutputStream os) {
        this.dos = new SafeDataOutputStream(os);
    }

    public synchronized void done(final ExitCode exitCode) {
        this.dos.writeByte(ControlId.DONE);
        this.dos.writeInt(exitCode.getCode());
        this.dos.flush();
    }
}
