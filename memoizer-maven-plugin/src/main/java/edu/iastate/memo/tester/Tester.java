package edu.iastate.memo.tester;

import edu.iastate.memo.commons.misc.Ansi;
import edu.iastate.memo.commons.process.ProcessUtils;
import edu.iastate.memo.commons.testing.junit.runner.JUnitRunner;
import org.pitest.process.ProcessArgs;
import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataInputStream;

import java.net.Socket;
import java.util.Collection;
import java.util.Set;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class Tester {
    public static void main(String[] args) throws Exception {
        System.out.println("Test runner is here!");
        final int port = Integer.parseInt(args[0]);
        Socket socket = null;
        try {
            socket = new Socket("localhost", port);

            final SafeDataInputStream dis = new SafeDataInputStream(socket.getInputStream());

            final TesterArguments arguments = dis.read(TesterArguments.class);

            final Collection<String> testClassNames = arguments.testClassNames;

            final JUnitRunner runner = new JUnitRunner(testClassNames);
            final long start = System.currentTimeMillis();
            runner.run();
            System.out.println(Ansi.constructInfoMessage("Info", "-----------------------------------"));
            System.out.println(Ansi.constructInfoMessage("Info", "Test execution took " + (System.currentTimeMillis() - start) + " ms"));
            System.out.println(Ansi.constructInfoMessage("Info", "-----------------------------------"));

            // finalizing & reporting the results
            final TesterReporter reporter = new TesterReporter(socket.getOutputStream());
            System.out.println("Test execution is DONE!");
            reporter.done(ExitCode.OK);
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.out);
            System.out.println("WARNING: Error during profiling!");
        } finally {
            ProcessUtils.safelyCloseSocket(socket);
        }
    }

    public static void run(final ProcessArgs processArgs, final Set<String> testClassNames) throws Exception {
        final TesterArguments arguments = new TesterArguments(testClassNames);
        final TesterProcess process = new TesterProcess(processArgs, arguments);
        process.start();
        process.waitToDie();
    }
}
