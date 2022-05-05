package edu.iastate.memo.memoizer.client;

import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ClientCommunicationThread extends CommunicationThread {
    private final DataReceiver receiver;

    public ClientCommunicationThread(final ServerSocket socket,
                                     final ClientArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());

    }

    public ClientCommunicationThread(final ServerSocket socket,
                                     final DataSender sender,
                                     final DataReceiver receiver) {
        super(socket,sender, receiver);
        this.receiver = receiver;
    }

    public MemoizationResults getMemoizationResults() {
        return this.receiver.results;
    }

    private static class DataSender implements SideEffect1<SafeDataOutputStream> {
        private final ClientArguments arguments;

        public DataSender(ClientArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(SafeDataOutputStream safeDataOutputStream) {
            safeDataOutputStream.write(this.arguments);
        }
    }

    private static class DataReceiver implements ReceiveStrategy {
        MemoizationResults results;

        @Override
        public void apply(byte __, SafeDataInputStream safeDataInputStream) {
            // handling memoization client report
            this.results = safeDataInputStream.read(HitMissPair.class);
        }
    }
}
