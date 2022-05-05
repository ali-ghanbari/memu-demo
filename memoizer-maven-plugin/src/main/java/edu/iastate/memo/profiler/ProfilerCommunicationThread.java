package edu.iastate.memo.profiler;

import edu.iastate.memo.constants.ControlId;
import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles communications between memoizer and the child process
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ProfilerCommunicationThread extends CommunicationThread {
    private final DataReceiver receiver;

    public ProfilerCommunicationThread(final ServerSocket socket,
                                       final ProfilerArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());
    }

    private ProfilerCommunicationThread(final ServerSocket socket,
                                        final DataSender sender,
                                        final DataReceiver receiver) {
        super(socket, sender, receiver);
        this.receiver = receiver;
    }

    public Map<Integer, Double> getMethodsTiming() {
        return this.receiver.methodsTiming;
    }

    public Map<String, List<Integer>> getMethodCoverage() {
        return this.receiver.methodCoverage;
    }

    public Set<Integer> getConstructorMethods() { return this.receiver.constructorMethods; }

    private static class DataSender implements SideEffect1<SafeDataOutputStream> {
        final ProfilerArguments arguments;

        DataSender(final ProfilerArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(final SafeDataOutputStream dos) {
            dos.write(this.arguments);
            dos.flush();
        }
    }

    private static class DataReceiver implements ReceiveStrategy {
        Map<Integer, Double> methodsTiming;

        Map<String, List<Integer>> methodCoverage;

        Set<Integer> constructorMethods;

        @Override
        @SuppressWarnings({"unchecked"})
        public void apply(byte control, SafeDataInputStream is) {
            switch (control) {
                case ControlId.PROFILER_REPORT_METHODS_TIMING:
                    this.methodsTiming = is.read(HashMap.class); break;
                case ControlId.PROFILER_REPORT_METHOD_COVERAGE:
                    this.methodCoverage = is.read(HashMap.class); break;
                case ControlId.PROFILER_REPORT_CONSTRUCTORS:
                    this.constructorMethods = is.read(HashSet.class);
            }

        }
    }
}