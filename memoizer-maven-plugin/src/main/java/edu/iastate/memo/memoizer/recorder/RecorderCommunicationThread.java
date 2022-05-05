package edu.iastate.memo.memoizer.recorder;

import edu.iastate.memo.memoizer.MemoTableDatabase;
import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class RecorderCommunicationThread extends CommunicationThread {
    private final DataReceiver receiver;

    public RecorderCommunicationThread(final ServerSocket socket,
                                       final RecorderArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());
    }

    private RecorderCommunicationThread(final ServerSocket socket,
                                        DataSender sender,
                                        DataReceiver receiver) {
        super(socket, sender, receiver);
        this.receiver = receiver;
    }

    public MemoTableDatabase getMemoTableDatabase() {
        return this.receiver.memoTableDatabase;
    }

    private static class DataSender implements SideEffect1<SafeDataOutputStream> {
        final RecorderArguments arguments;

        public DataSender(final RecorderArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(SafeDataOutputStream dos) {
            dos.write(this.arguments);
            dos.flush();
        }
    }

    private static class DataReceiver implements ReceiveStrategy {
        MemoTableDatabase memoTableDatabase;

        @Override
        public void apply(byte __, SafeDataInputStream sid) {
            // handling report memo table database
            this.memoTableDatabase = sid.read(MemoTableDatabase.class);
        }
    }
}
