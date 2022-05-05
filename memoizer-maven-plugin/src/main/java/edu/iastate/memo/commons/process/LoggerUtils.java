package edu.iastate.memo.commons.process;

import org.pitest.functional.SideEffect1;

/**
 * A class with two methods for creating objects that help redirect
 * stdout and stderr of the child process to stdout of the parent
 * process.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class LoggerUtils {
    private static final Object LOCK = new Object();

    private LoggerUtils() {

    }

    public static SideEffect1<String> out() {
        return msg -> {
            synchronized (LOCK) {
                System.out.print(msg);
                System.out.flush();
            }
        };
    }

    public static SideEffect1<String> err() {
        return msg -> {
            synchronized (LOCK) {
                System.out.print(msg);
                System.out.flush();
            }
        };
    }
}
