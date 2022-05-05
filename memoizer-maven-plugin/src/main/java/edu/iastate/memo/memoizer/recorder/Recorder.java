package edu.iastate.memo.memoizer.recorder;

import edu.iastate.memo.commons.misc.Ansi;
import edu.iastate.memo.commons.process.ProcessUtils;
import edu.iastate.memo.commons.testing.junit.runner.JUnitRunner;
import edu.iastate.memo.commons.testing.junit.runner.TestUnitFilter;
import edu.iastate.memo.constants.Params;
import edu.iastate.memo.memoizer.MemoTableDatabase;
import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.predicate.Predicate;
import org.pitest.process.ProcessArgs;
import org.pitest.util.ExitCode;
import org.pitest.util.IsolationUtils;
import org.pitest.util.SafeDataInputStream;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class Recorder {
    public static void main(String[] args) throws Exception {
        System.out.println("Recorder is HERE!");
        final int port = Integer.parseInt(args[0]);
        Socket socket = null;
        try {
            socket = new Socket("localhost", port);

            final SafeDataInputStream dis = new SafeDataInputStream(socket.getInputStream());

            final RecorderArguments arguments = dis.read(RecorderArguments.class);

            final ClassLoader contextClassLoader = IsolationUtils.getContextClassLoader();
            ClassByteArraySource byteArraySource = new ClassloaderByteArraySource(contextClassLoader);
            byteArraySource = new CachingByteArraySource(byteArraySource, Params.BYTECODE_CLASS_CACHE_SIZE);

            HotSwapAgent.addTransformer(new RecorderTransformer(byteArraySource,
                    arguments.getAppClassFilter(),
                    arguments.getMemoizedMethods(),
                    arguments.getMethodsDom()));

            final MemoTableDatabase database = RecorderStateManager.initializeRecorder(arguments.getMethodsDom(),
                    arguments.getFieldsDom(),
                    arguments.getStaticReads(),
                    arguments.getStaticAccesses(),
                    arguments.getInstanceReads(),
                    arguments.getInstanceAccesses(),
                    arguments.getMemoizedMethods());

            final JUnitRunner runner = new JUnitRunner(arguments.getTestClassNames());
            final Set<String> relevantTestUnits = new HashSet<>(arguments.getRelevantTestUnits());
            // running only relevant tests without early exit
            final long start = System.currentTimeMillis();
            runner.run(TestUnitFilter.some(relevantTestUnits));
            System.out.println(Ansi.constructInfoMessage("Info", "Recording takes " + (System.currentTimeMillis() - start) + " ms"));

            final RecorderReporter reporter = new RecorderReporter(socket.getOutputStream());
            writeMemoTableDB(database);

            System.out.println("Recording is DONE!");
            reporter.done(ExitCode.OK);
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.out);
            System.out.println("WARNING: Error during profiling!");
        } finally {
            ProcessUtils.safelyCloseSocket(socket);
        }
        System.exit(ExitCode.OK.getCode());
    }

    private static void writeMemoTableDB(final MemoTableDatabase database) {
        try (final OutputStream os = new FileOutputStream("memo-table-db");
             final ObjectOutputStream out = new ObjectOutputStream(os)) {
            database.writeOut(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runRecorder(final ProcessArgs processArgs,
                                   final Predicate<String> appClassFilter,
                                   final Collection<String> testClassNames,
                                   final Collection<String> relevantTestUnits,
                                   final Collection<String> expensiveMethods) throws Exception {
        final RecorderArguments arguments = new RecorderArguments(appClassFilter, testClassNames, relevantTestUnits, expensiveMethods);
        final RecorderProcess process = new RecorderProcess(processArgs, arguments);
        process.start();
        process.waitToDie();
    }
}