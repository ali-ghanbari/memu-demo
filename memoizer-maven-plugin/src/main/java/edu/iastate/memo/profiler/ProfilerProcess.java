package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.process.ChildProcess;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.SocketFinder;

import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Process object for dynamic analyzer.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ProfilerProcess extends ChildProcess {
    public ProfilerProcess(final ProcessArgs processArgs,
                           final ProfilerArguments arguments) {
        this((new SocketFinder()).getNextAvailableServerSocket(), processArgs, arguments);
    }

    private ProfilerProcess(final ServerSocket socket,
                            final ProcessArgs processArgs,
                            final ProfilerArguments arguments) {
        super(new WrappingProcess(socket.getLocalPort(), processArgs, Profiler.class),
                new ProfilerCommunicationThread(socket, arguments));
    }

    public Map<Integer, Double> getMethodsTiming() {
        return ((ProfilerCommunicationThread) this.communicationThread).getMethodsTiming();
    }

    public Map<String, List<Integer>> getMethodCoverage() {
        return ((ProfilerCommunicationThread) this.communicationThread).getMethodCoverage();
    }

    public Set<Integer> getConstructorMethods() {
        return ((ProfilerCommunicationThread) this.communicationThread).getConstructorMethods();
    }
}