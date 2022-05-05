package edu.iastate.memo.tester;

import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class TesterCommunicationThread extends CommunicationThread {
    public TesterCommunicationThread(final ServerSocket socket,
                                     final TesterArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());
    }

    private TesterCommunicationThread(final ServerSocket socket,
                                      final DataSender sender,
                                      final DataReceiver receiver) {
        super(socket, sender, receiver);
    }

    private static class DataSender implements SideEffect1<SafeDataOutputStream> {
        final TesterArguments arguments;

        DataSender(final TesterArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(final SafeDataOutputStream dos) {
            dos.write(this.arguments);
            dos.flush();
        }
    }

    private static class DataReceiver implements ReceiveStrategy {
        @Override
        public void apply(byte control, SafeDataInputStream is) {
            // nothing!
        }
    }
}