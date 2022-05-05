package edu.iastate.memo.commons.process;

import edu.iastate.memo.commons.misc.Ansi;

import java.io.IOException;
import java.net.Socket;

public class ResourceUtils {
    public static void safelyCloseSocket(final Socket s) {
        if (s != null) {
            try {
                s.close();
            } catch (final IOException e) {
                e.printStackTrace();
                System.out.println(Ansi.constructWarningMessage("WARNING", "Couldn't close socket"));
            }
        }
    }
}
