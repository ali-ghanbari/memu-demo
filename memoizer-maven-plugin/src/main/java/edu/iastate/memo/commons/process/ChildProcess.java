package edu.iastate.memo.commons.process;

import org.pitest.process.WrappingProcess;
import org.pitest.util.CommunicationThread;

import java.io.IOException;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public abstract class ChildProcess {
    protected final WrappingProcess process;

    protected final CommunicationThread communicationThread;

    protected ChildProcess(final WrappingProcess process,
                           final CommunicationThread communicationThread) {
        this.process = process;
        this.communicationThread = communicationThread;
    }

    public void start() throws IOException, InterruptedException {
        this.communicationThread.start();
        this.process.start();
    }

    public void waitToDie() {
        try {
            this.communicationThread.waitToFinish();
        } finally {
            this.process.destroy();
        }
    }
}
