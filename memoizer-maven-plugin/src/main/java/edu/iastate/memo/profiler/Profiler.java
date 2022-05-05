package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.io.FileUtils;
import edu.iastate.memo.commons.misc.Ansi;
import edu.iastate.memo.commons.misc.NameUtils;
import edu.iastate.memo.commons.process.ResourceUtils;
import edu.iastate.memo.commons.relational.BinaryRelation;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.commons.relational.MayCallRel;
import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.commons.relational.Solver;
import edu.iastate.memo.commons.relational.UnaryRelation;
import edu.iastate.memo.commons.testing.junit.runner.JUnitRunner;
import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.predicate.Predicate;
import org.pitest.process.ProcessArgs;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.IsolationUtils;
import org.pitest.util.SafeDataInputStream;

import java.io.File;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static edu.iastate.memo.constants.Params.BYTECODE_CLASS_CACHE_SIZE;

/**
 * Entry point for dynamic analyzers!
 *
 * Profiler computes:
 *  - dynamic call graph,
 *  - transitive closure of field accesses relation:
 *      -- both read and write,
 *  - timing for each method shall also be recorded.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class Profiler {
    public static void main(String[] args) {
        System.out.println("Profiler is here!");
        final int port = Integer.parseInt(args[0]);
        Socket socket = null;
        try {
            socket = new Socket("localhost", port);

            final SafeDataInputStream dis = new SafeDataInputStream(socket.getInputStream());

            final ProfilerArguments arguments = dis.read(ProfilerArguments.class);

            final ClassLoader contextClassLoader = IsolationUtils.getContextClassLoader();
            ClassByteArraySource byteArraySource = new ClassloaderByteArraySource(contextClassLoader);
            byteArraySource = new CachingByteArraySource(byteArraySource, BYTECODE_CLASS_CACHE_SIZE);

            final Collection<String> testClassNames = arguments.testClassNames;

            final FieldsDom fieldsDom = new FieldsDom();
            final MethodsDom methodsDom = new MethodsDom();
            final Set<Integer> staticFields = new HashSet<>();
            final Set<Integer> constructorMethods = new HashSet<>();
            final ProfilerTransformer profilerTransformer = new ProfilerTransformer(byteArraySource,
                    arguments.appClassFilter,
                    fieldsDom,
                    methodsDom,
                    staticFields,
                    constructorMethods);
            HotSwapAgent.addTransformer(profilerTransformer);

            final long start = System.currentTimeMillis();
            createDecoratedRunner(testClassNames).run();
            System.out.println(Ansi.constructInfoMessage("Info", "Profiling took " + (System.currentTimeMillis() - start) + " ms"));

            // finalizing & reporting the results
            final MayCallRel mayCallRel = CallGraphRecorder.getCallGraph(methodsDom);
            methodsDom.save(".", true);
            mayCallRel.save(".");
            final Process implicatesComputation = Solver.runBDDBDDB("implicates.dlog", true);
            final ProfilerReporter reporter = new ProfilerReporter(socket.getOutputStream());
            final CompletableFuture<Void> reporterThread = CompletableFuture.runAsync(() -> {
                reporter.reportMethodsTiming(TimeRecorder.getMethodsTiming());
                reporter.reportMethodsCoverage(MethodCoverageRecorder.getCoverageMap());
                reporter.reportConstructorMethods(constructorMethods);
            });
            final BinaryRelation directlyReadRel = FieldAccessRecorder.getDirectlyReadsRel(methodsDom, fieldsDom);
            final BinaryRelation directlyWriteRel = FieldAccessRecorder.getDirectlyWritesRel(methodsDom, fieldsDom);
            fieldsDom.save(".", true);
            directlyReadRel.save(".");
            directlyWriteRel.save(".");
            final UnaryRelation staticFieldRel = new UnaryRelation("static_field", fieldsDom);
            final UnaryRelation instanceFieldRel = new UnaryRelation("instance_field", fieldsDom);
            for (final int fieldIndex : fieldsDom.getIndices()) {
                if (staticFields.contains(fieldIndex)) {
                    staticFieldRel.add(fieldIndex);
                } else {
                    instanceFieldRel.add(fieldIndex);
                }
            }
            staticFieldRel.save(".");
            instanceFieldRel.save(".");
            Solver.runBDDBDDB("accesses.dlog", false);
            implicatesComputation.waitFor();
            makeImplicatesRelReflexive(methodsDom);
            reporterThread.get();
            System.out.println("Profiling is DONE!");
            reporter.done(ExitCode.OK);
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.out);
            System.out.println(Ansi.constructWarningMessage("WARNING", "Error during profiling!"));
        } finally {
            ResourceUtils.safelyCloseSocket(socket);
        }
    }

    private static void makeImplicatesRelReflexive(final MethodsDom methodsDom) {
        final BinaryRelation implicates = BinaryRelation.load("implicates", methodsDom, methodsDom);
        for (final String method : methodsDom) {
            implicates.add(method, method);
        }
        implicates.save(".");
    }

    private static JUnitRunner createDecoratedRunner(final Collection<String> testClassNames) {
        final JUnitRunner runner = new JUnitRunner(testClassNames);
        final List<TestUnit> testUnits = new LinkedList<>();
        for (final TestUnit testUnit : runner.getTestUnits()) {
            testUnits.add(new TestUnit() {
                @Override
                public void execute(ResultCollector rc) {
                    String testName = testUnit.getDescription().getName();
                    testName = NameUtils.sanitizeExtendedTestName(testName);
                    MethodCoverageRecorder.addTestUnit(testName);
                    testUnit.execute(rc);
                }

                @Override
                public Description getDescription() {
                    return testUnit.getDescription();
                }
            });
        }
        runner.setTestUnits(testUnits);
        return runner;
    }

    public static ProfilerResults runProfiler(final ProcessArgs processArgs,
                                              final Predicate<String> appClassFilter,
                                              final Set<String> testClassNames,
                                              final boolean force) throws Exception {
        if (!force) {
            if (MemoizedProfilerResults.filesOK()) {
                return new MemoizedProfilerResults();
            }
            System.out.println(Ansi.constructWarningMessage("Profiler", "Unable to use cached files; rerunning..."));
        }
        final ProfilerArguments arguments = new ProfilerArguments(appClassFilter, testClassNames);
        final ProfilerProcess process = new ProfilerProcess(processArgs, arguments);
        process.start();
        process.waitToDie();
        return new MemoizedProfilerResults(process);
    }

    private static class MemoizedProfilerResults implements ProfilerResults {
        private static final String[] FILE_NAMES = {
                "constructor-methods",
                "methods-timing",
                "method-coverage",
                "F.dom",
                "F.map",
                "M.dom",
                "M.map",
                "may_call.bdd",
                "static_reads.bdd",
                "static_writes.bdd",
                "static_accesses.bdd",
                "instance_reads.bdd",
                "implicates.bdd"
        };

        private final Set<String> savedFiles;

        private final boolean saveBeforeReturn;

        private MethodsDom methodsDom = null;

        private FieldsDom fieldsDom = null;

        private final Set<Integer> constructorMethods;

        private final Map<Integer, Double> methodTimingMap;

        private final Map<String, List<Integer>> methodCoverageMap;

        private MayCallRel callGraph = null;

        private BinaryRelation instanceReadsRel = null;

        private BinaryRelation staticReadsRel = null;

        private BinaryRelation staticWritesRel = null;

        private BinaryRelation staticAccessesRel = null;

        public static boolean filesOK() {
            for (final String fileName : FILE_NAMES) {
                if (!(new File(fileName)).isFile()) {
                    return false;
                }
            }
            return true;
        }

        @SuppressWarnings({"unchecked"})
        public MemoizedProfilerResults() {
            this.saveBeforeReturn = false;
            this.savedFiles = new HashSet<>();
            this.constructorMethods = FileUtils.readObject(new File("constructor-methods"), HashSet.class);
            this.methodTimingMap = FileUtils.readObject(new File("methods-timing"), HashMap.class);
            this.methodCoverageMap = FileUtils.readObject(new File("method-coverage"), HashMap.class);
        }

        public MemoizedProfilerResults(final ProfilerProcess process) {
            this.saveBeforeReturn = true;
            this.savedFiles = new HashSet<>();
            this.constructorMethods = process.getConstructorMethods();
            this.methodTimingMap = process.getMethodsTiming();
            this.methodCoverageMap = process.getMethodCoverage();
        }

        @Override
        public MethodsDom getMethodsDom() {
            if (this.methodsDom == null) {
                this.methodsDom = new MethodsDom(".");
            }
            return this.methodsDom;
        }

        @Override
        public FieldsDom getFieldsDom() {
            if (this.fieldsDom == null) {
                this.fieldsDom = new FieldsDom(".");
            }
            return this.fieldsDom;
        }

        @Override
        public Set<Integer> getConstructorMethods() {
            final String fileName = "constructor-methods";
            if (this.saveBeforeReturn && !this.savedFiles.contains(fileName)) {
                this.savedFiles.add(fileName);
                FileUtils.writeObject(new File(fileName), this.constructorMethods);
            }
            return this.constructorMethods;
        }

        @Override
        public Map<String, List<Integer>> getMethodCoverageMap() {
            final String fileName = "method-coverage";
            if (this.saveBeforeReturn && !this.savedFiles.contains(fileName)) {
                this.savedFiles.add(fileName);
                FileUtils.writeObject(new File(fileName), this.methodCoverageMap);
            }
            return this.methodCoverageMap;
        }

        @Override
        public Map<Integer, Double> getMethodTimingMap() {
            final String fileName = "methods-timing";
            if (this.saveBeforeReturn && !this.savedFiles.contains(fileName)) {
                this.savedFiles.add(fileName);
                FileUtils.writeObject(new File(fileName), this.methodTimingMap);
            }
            return this.methodTimingMap;
        }

        @Override
        public MayCallRel getCallGraph() {
            if (this.callGraph == null) {
                this.callGraph = MayCallRel.load(getMethodsDom());
            }
            return this.callGraph;
        }

        @Override
        public BinaryRelation getInstanceReadsRel() {
            if (this.instanceReadsRel == null) {
                this.instanceReadsRel = BinaryRelation.load("instance_reads", getMethodsDom(), getFieldsDom());
            }
            return this.instanceReadsRel;
        }

        @Override
        public BinaryRelation getStaticReadsRel() {
            if (this.staticReadsRel == null) {
                this.staticReadsRel = BinaryRelation.load("static_reads", getMethodsDom(), getFieldsDom());
            }
            return this.staticReadsRel;
        }

        @Override
        public BinaryRelation getStaticWritesRel() {
            if (this.staticWritesRel == null) {
                this.staticWritesRel = BinaryRelation.load("static_writes", getMethodsDom(), getFieldsDom());
            }
            return this.staticWritesRel;
        }

        @Override
        public BinaryRelation getStaticAccessesRel() {
            if (this.staticAccessesRel == null) {
                this.staticAccessesRel = BinaryRelation.load("static_accesses", getMethodsDom(), getFieldsDom());
            }
            return this.staticAccessesRel;
        }
    }
}