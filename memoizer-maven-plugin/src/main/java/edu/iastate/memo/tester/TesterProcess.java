package edu.iastate.memo.tester;

import edu.iastate.memo.commons.process.ChildProcess;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.SocketFinder;

import java.net.ServerSocket;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class TesterProcess extends ChildProcess {
    public TesterProcess(final ProcessArgs processArgs,
                         final TesterArguments arguments) {
        this((new SocketFinder()).getNextAvailableServerSocket(), processArgs, arguments);
    }

    private TesterProcess(final ServerSocket socket,
                          final ProcessArgs processArgs,
                          final TesterArguments arguments) {
        super(new WrappingProcess(socket.getLocalPort(), processArgs, Tester.class),
                new TesterCommunicationThread(socket, arguments));
    }
}