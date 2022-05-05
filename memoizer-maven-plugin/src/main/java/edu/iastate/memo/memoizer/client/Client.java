package edu.iastate.memo.memoizer.client;

import edu.iastate.memo.commons.misc.Ansi;
import edu.iastate.memo.commons.process.ProcessUtils;
import edu.iastate.memo.commons.relational.MethodsDom;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class Client {
    public static void main(String[] args) {
        System.out.println("Client is here!");
        final int port = Integer.parseInt(args[0]);
        Socket socket = null;
        try {
            socket = new Socket("localhost", port);

            final SafeDataInputStream dis = new SafeDataInputStream(socket.getInputStream());

            ClientArguments arguments = dis.read(ClientArguments.class);

            final ClassLoader contextClassLoader = IsolationUtils.getContextClassLoader();
            ClassByteArraySource byteArraySource = new ClassloaderByteArraySource(contextClassLoader);
            byteArraySource = new CachingByteArraySource(byteArraySource, Params.BYTECODE_CLASS_CACHE_SIZE);

            final MethodsDom methodsDom = arguments.getMethodsDom();

            HotSwapAgent.addTransformer(new ClientTransformer(byteArraySource,
                    arguments.getAppClassFilter(),
                    arguments.getMemoizedMethods(),
                    methodsDom,
                    true));

            ClientStateManager.initializeClientStateManager(methodsDom,
                    arguments.getFieldsDom(),
                    arguments.getStaticReads(),
                    arguments.getStaticAccesses(),
                    arguments.getInstanceReads(),
                    arguments.getInstanceAccesses(),
                    arguments.getMemoizedMethods(),
                    loadMemoTableDB());

            CacheStatus.reset();

            final JUnitRunner runner = new JUnitRunner(arguments.getTestClassNames(),
                    arguments.getFailingTestPredicate());

            final Set<String> relevantTestUnits = new HashSet<>(arguments.getRelevantTestUnits());

            System.out.println("*************");
            System.out.println("# relevant tests " + relevantTestUnits.size());
            System.out.println("*************");

            // running only relevant tests without early exit
            final long start = System.currentTimeMillis();
            boolean res = runner.run(TestUnitFilter.some(relevantTestUnits));
            System.out.println(Ansi.constructInfoMessage("Info", "Client takes " + (System.currentTimeMillis() - start) + " ms"));

            final ClientReporter reporter = new ClientReporter(socket.getOutputStream());
            final String currentMethod = arguments.getProvisionallyMemoizedMethodName();
            if (res) {
                final int currentMethodIndex = methodsDom.indexOf(currentMethod);
                reporter.reportMemoizationResult(new HitMissPair(CacheStatus.getHits(currentMethodIndex), CacheStatus.getMisses(currentMethodIndex)));
            } else {
                reporter.reportMemoizationResult(new HitMissPair(-1, -1));
            }

            System.out.println("Client's work is DONE!");
            reporter.done(ExitCode.OK);
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.out);
            System.out.println("WARNING: Error during profiling!");
        } finally {
            ProcessUtils.safelyCloseSocket(socket);
        }
        System.exit(ExitCode.OK.getCode()); // kills surviving threads!
    }

    private static MemoTableDatabase loadMemoTableDB() {
        final File memoTableDBFile = new File("memo-table-db");
        try (final InputStream is = new FileInputStream(memoTableDBFile);
             final ObjectInputStream input = new ObjectInputStream(is)) {
            return MemoTableDatabase.readFromInput(input);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static MemoizationResults runClient(final ProcessArgs processArgs,
                                               final Predicate<String> appClassFilter,
                                               final Collection<String> testClassNames,
                                               final Predicate<String> failingTestFilter,
                                               final Collection<String> relevantTestUnits,
                                               final Collection<String> memoizedMethods,
                                               final String provisionallyMemoizedMethodName) throws Exception {
        final ClientArguments arguments = new ClientArguments(appClassFilter,
                testClassNames,
                relevantTestUnits,
                memoizedMethods,
                failingTestFilter,
                provisionallyMemoizedMethodName);
        final ClientProcess process = new ClientProcess(processArgs, arguments);
        process.start();
        process.waitToDie();
        return process.getMemoizationResults();
    }
}