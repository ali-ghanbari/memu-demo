package edu.iastate.memo.memoizer.client;

import edu.iastate.memo.commons.process.ChildProcess;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.SocketFinder;

import java.net.ServerSocket;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ClientProcess extends ChildProcess {
    public ClientProcess(final ProcessArgs processArgs,
                         final ClientArguments arguments) {
        this((new SocketFinder()).getNextAvailableServerSocket(), processArgs, arguments);
    }

    private ClientProcess (final ServerSocket socket,
                           final ProcessArgs processArgs,
                           final ClientArguments arguments) {
        super(new WrappingProcess(socket.getLocalPort(), processArgs, Client.class),
                new ClientCommunicationThread(socket, arguments));
    }

    public MemoizationResults getMemoizationResults() {
        return ((ClientCommunicationThread) this.communicationThread).getMemoizationResults();
    }
}
