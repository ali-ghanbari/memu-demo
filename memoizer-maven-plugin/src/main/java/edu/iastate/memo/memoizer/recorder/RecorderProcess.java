package edu.iastate.memo.memoizer.recorder;

import edu.iastate.memo.commons.process.ChildProcess;
import edu.iastate.memo.memoizer.MemoTableDatabase;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.SocketFinder;

import java.net.ServerSocket;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class RecorderProcess extends ChildProcess {
    public RecorderProcess(final ProcessArgs processArgs,
                           final RecorderArguments arguments) {
        this((new SocketFinder()).getNextAvailableServerSocket(), processArgs, arguments);
    }

    private RecorderProcess(final ServerSocket socket,
                            final ProcessArgs processArgs,
                            final RecorderArguments arguments) {
        super(new WrappingProcess(socket.getLocalPort(), processArgs, Recorder.class),
                new RecorderCommunicationThread(socket, arguments));
    }

    public MemoTableDatabase getMemoTableDatabase() {
        return ((RecorderCommunicationThread) this.communicationThread).getMemoTableDatabase();
    }
}
