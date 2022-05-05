package edu.iastate.memo;

import edu.iastate.memo.commons.misc.Ansi;
import edu.iastate.memo.commons.process.LoggerUtils;
import edu.iastate.memo.commons.relational.BinaryRelation;
import edu.iastate.memo.commons.relational.BinaryRelationVisitor;
import edu.iastate.memo.commons.relational.MayCallRel;
import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.commons.relational.Solver;
import edu.iastate.memo.memoizer.ProvisionalMemoizer;
import edu.iastate.memo.memoizer.client.MemoizationResults;
import edu.iastate.memo.profiler.Profiler;
import edu.iastate.memo.profiler.ProfilerResults;
import edu.iastate.memo.tester.Tester;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.DefaultCodePathPredicate;
import org.pitest.mutationtest.config.DefaultDependencyPathPredicate;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.mutationtest.tooling.KnownLocationJavaAgentFinder;
import org.pitest.process.JavaAgent;
import org.pitest.process.JavaExecutableLocator;
import org.pitest.process.KnownLocationJavaExecutableLocator;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.iastate.memo.constants.Params.BYTECODE_CLASS_CACHE_SIZE;

/**
 * Entry point for our memoization system!
 *
 @author Ali Ghanbari (alig@iastate.edu)
 */
public class MemoizerEntryPoint {
    private final File compatibleJREHome;

    private final ClassPath classPath;

    private final ClassByteArraySource byteArraySource;

    private final Predicate<String> appClassFilter;

    private final Predicate<String> testClassFilter;

    private final Predicate<String> isFailingTest;

    private final int limit;

    private final double threshold;

    private final boolean forceProfiling;

    private final boolean measureTestingTime;

    private final boolean skipProvisionalMemoization;

    private final Set<String> specialMethodFilter;

    private final List<String> childProcessArguments;

    public MemoizerEntryPoint(final ClassPath classPath,
                              final File compatibleJREHome,
                              final Predicate<String> appClassFilter,
                              final Predicate<String> testClassFilter,
                              final Predicate<String> isFailingTest,
                              final double threshold,
                              final int limit,
                              final boolean forceProfiling,
                              final boolean measureTestingTime,
                              final boolean skipProvisionalMemoization,
                              final Set<String> specialMethodFilter,
                              final List<String> childProcessArguments) {
        this.classPath = classPath;
        this.byteArraySource = createClassByteArraySource(classPath);
        this.compatibleJREHome = compatibleJREHome;
        this.appClassFilter = appClassFilter;
        this.testClassFilter = testClassFilter;
        this.isFailingTest = isFailingTest;
        this.threshold = threshold;
        this.limit = limit;
        this.forceProfiling = forceProfiling;
        this.measureTestingTime = measureTestingTime;
        this.skipProvisionalMemoization = skipProvisionalMemoization;
        this.specialMethodFilter = specialMethodFilter;
        this.childProcessArguments = childProcessArguments;
    }

    private static ClassByteArraySource createClassByteArraySource(final ClassPath classPath) {
        final ClassPathByteArraySource cpbas = new ClassPathByteArraySource(classPath);
        final ClassByteArraySource cbas = fallbackToClassLoader(cpbas);
        return new CachingByteArraySource(cbas, BYTECODE_CLASS_CACHE_SIZE);
    }

    // credit: this method is adopted from PIT's source code
    private static ClassByteArraySource fallbackToClassLoader(final ClassByteArraySource bas) {
        final ClassByteArraySource clSource = ClassloaderByteArraySource.fromContext();
        return clazz -> {
            final Option<byte[]> maybeBytes = bas.getBytes(clazz);
            if (maybeBytes.hasSome()) {
                return maybeBytes;
            }
            return clSource.getBytes(clazz);
        };
    }

    public void start() throws MojoExecutionException {
        try {
            start0();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        }
    }

    public void start0() throws Exception {
        final Set<String> testClassNames = retrieveTestClassNames();
        final ProcessArgs defaultProcessArgs = getDefaultProcessArgs();

        if (this.measureTestingTime) {
            Tester.run(defaultProcessArgs, testClassNames);
        }

        final ProfilerResults profilerResults = Profiler.runProfiler(defaultProcessArgs, this.appClassFilter, testClassNames, this.forceProfiling);

        saveDecompressedRelations(profilerResults);
        final MethodsDom methodsDom = profilerResults.getMethodsDom();
        final Map<Integer, Double> methodsTiming = profilerResults.getMethodTimingMap();
        final Set<Integer> constructorMethods = profilerResults.getConstructorMethods();

        final Set<Integer> expensiveMethodIndices = findTopExpensiveMethods(methodsTiming, constructorMethods, methodsDom);

        System.out.println(Ansi.constructInfoMessage("Info", thinLine()));
        System.out.println(Ansi.constructInfoMessage("Info", "Top expensive methods (" + expensiveMethodIndices.size() + "):"));
        for (final int methodId : expensiveMethodIndices) {
            System.out.println("\t" + methodsDom.get(methodId) + " taking " + methodsTiming.get(methodId) + " ms");
        }
        System.out.println(Ansi.constructInfoMessage("Info", thinLine()));

        final MayCallRel mayCallRel = profilerResults.getCallGraph();
        final Iterator<String> dominatorChainIt = getDominatorChains(expensiveMethodIndices, mayCallRel, methodsDom);

        if (this.skipProvisionalMemoization) {
            System.out.println(Ansi.constructInfoMessage("Info", "Provisional memoization is skipped."));
            return;
        }

        final List<String> expensiveMethodNames = methodsDom.getKeys(expensiveMethodIndices);
        final ProvisionalMemoizer memoizer = new ProvisionalMemoizer(getDefaultProcessArgs(),
                this.appClassFilter,
                testClassNames,
                this.isFailingTest,
                profilerResults.getMethodCoverageMap(),
                dominatorChainIt,
                expensiveMethodNames,
                methodsDom);
        final long start = System.currentTimeMillis();
        final List<String> memoizedMethods = memoizer.tryMemoize();
        System.out.println();
        System.out.println(Ansi.constructInfoMessage("Info", thinLine()));
        System.out.println(Ansi.constructInfoMessage("Info", (System.currentTimeMillis() - start) + " ms"));
        System.out.println(Ansi.constructInfoMessage("Info", thinLine()));
        System.out.println("\n#############################");
        double maxTime = Double.NEGATIVE_INFINITY;
        double realMax = Double.NEGATIVE_INFINITY;
        for (final String methodName : memoizedMethods) {
            final double methodTiming = methodsTiming.get(methodsDom.indexOf(methodName));
            maxTime = Math.max(maxTime, methodTiming);
            final MemoizationResults results = memoizer.getMemoizationResult(methodName);
            final int h = results.getHits();
            final int m = results.getMisses();
            if ((((double) h) / ((double) (h + m))) < 1D) {
                continue;
            }
            realMax = Math.max(realMax, methodTiming);
            System.out.println(methodName);
        }
        System.out.println("#############################");
        System.out.printf("Out of %d candidates max was %f%n", memoizedMethods.size(), maxTime);
        System.out.println("Real max: " + realMax);

        try (final PrintWriter pw = new PrintWriter("memoized-methods-names")) {
            for (final String methodName : memoizedMethods) {
                pw.println(methodName);
            }
        }
    }

    private void saveDecompressedRelations(final ProfilerResults profilerResults) {
        saveDecompressedRelation(profilerResults.getStaticReadsRel().decompressList(), "rel-static-reads");
        saveDecompressedRelation(profilerResults.getStaticAccessesRel().decompressList(), "rel-static-accesses");
        saveDecompressedRelation(profilerResults.getInstanceReadsRel().decompressSet(), "rel-instance-reads");
        saveDecompressedRelation(profilerResults.getStaticAccessesRel().decompressSet(), "rel-instance-accesses");
    }

    private void saveDecompressedRelation(final Map<Integer, ?> rel, final String fileName) {
        try (final OutputStream out = new FileOutputStream(fileName);
             final ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(rel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Set<String> retrieveTestClassNames() {
        final ProjectClassPaths pcp = new ProjectClassPaths(this.classPath, defaultClassFilter(), defaultPathFilter());
        final CodeSource codeSource = new CodeSource(pcp);
        final Set<String> testClassNames = new HashSet<>();
        for (final ClassInfo classInfo : codeSource.getTests()) {
            testClassNames.add(classInfo.getName().asJavaName());
        }
        return testClassNames;
    }

    private static PathFilter defaultPathFilter() {
        return new PathFilter(new DefaultCodePathPredicate(),
                Prelude.not(new DefaultDependencyPathPredicate()));
    }

    private ClassFilter defaultClassFilter() {
        return new ClassFilter(this.testClassFilter, this.appClassFilter);
    }

    private ProcessArgs getDefaultProcessArgs() {
        final LaunchOptions defaultLaunchOptions = new LaunchOptions(getJavaAgent(),
                getDefaultJavaExecutableLocator(),
                this.childProcessArguments,
                Collections.<String, String>emptyMap());
        return ProcessArgs.withClassPath(this.classPath)
                .andLaunchOptions(defaultLaunchOptions)
                .andStderr(LoggerUtils.err())
                .andStdout(LoggerUtils.out());
    }

    private JavaExecutableLocator getDefaultJavaExecutableLocator() {
        final File javaFile = FileUtils.getFile(this.compatibleJREHome, "bin", "java");
        return new KnownLocationJavaExecutableLocator(javaFile.getAbsolutePath());
    }

    private JavaAgent getJavaAgent() {
        final String jarLocation = (new JarCreatingJarFinder(this.byteArraySource))
                .getJarLocation()
                .value();
        return new KnownLocationJavaAgentFinder(jarLocation);
    }

    private Set<Integer> findTopExpensiveMethods(final Map<Integer, Double> methodsTiming,
                                                 final Set<Integer> constructorMethods,
                                                 final MethodsDom methodsDom) {
        Map.Entry<Integer, Double> max = null;
        final LinkedList<Map.Entry<Integer, Double>> temp = new LinkedList<>();
        for (final Map.Entry<Integer, Double> entry : methodsTiming.entrySet()) {
            final int methodIndex = entry.getKey();
            // a naive but effective way to filter out unwanted methods
            if (constructorMethods.contains(methodIndex)) {
                continue;
            }
            final String methodName = methodsDom.get(methodIndex);
            if (isTestMethod(methodName) || isSpecialMethod(methodName)) {
                continue;
            }
            if (entry.getValue() >= this.threshold) {
                temp.add(entry);
            }
            if (max == null || max.getValue() < entry.getValue()) {
                max = entry;
            }
        }
        if (max != null) {
            final String msg = String.format("Max was %s taking %f ms", methodsDom.get(max.getKey()), max.getValue());
            System.out.println(Ansi.constructInfoMessage("Info", thinLine()));
            System.out.println(Ansi.constructInfoMessage("Info", msg));
            System.out.println(Ansi.constructInfoMessage("Info", thinLine()));
        }
        temp.sort((o1, o2) -> Double.compare(o2.getValue(), o1.getValue()));
        final Set<Integer> res = new HashSet<>();
        final int tempSize = temp.size();
        for (int ignored = 0; ignored < Math.min(this.limit, tempSize); ignored++) {
            res.add(temp.removeFirst().getKey());
        }
        return res;
    }

    private boolean isTestMethod(String methodName) {
        final int indexOfLP = methodName.lastIndexOf('(');
        methodName = methodName.substring(0, indexOfLP);
        final int indexOfDot = methodName.lastIndexOf('.');
        final String className = methodName.substring(0, indexOfDot);
        return this.testClassFilter.apply(className);
    }

    private boolean isSpecialMethod(final String methodName) {
        return this.specialMethodFilter.contains(methodName);
    }

    private static String thinLine() {
        return "-------------------------------------------------------";
    }

    /**
     *
     * @param expensiveMethods The indices of expensive methods
     * @param relevantTestUnits All the test units covering expensive methods
     * @param coveredMethods These are all the methods that are covered by <code>relevantTestUnits</code>
     * @throws Exception Some IO exception
     */
    private static void extractCoverageInfo(final Map<String, List<Integer>> testUnitCoverage,
                                            final Set<Integer> expensiveMethods,
                                            final List<String> relevantTestUnits,
                                            final Set<Integer> coveredMethods) throws Exception {
        final List<Integer> temp = new LinkedList<>();
        for (final Map.Entry<String, List<Integer>> entry : testUnitCoverage.entrySet()) {
            final String testUnitName = entry.getKey();
            boolean flag = true;
            for (final int methodIndex : entry.getValue()) {
                if (flag && expensiveMethods.contains(methodIndex)) {
                    relevantTestUnits.add(testUnitName);
                    flag = false;
                }
                // we tentatively record all covered methods, later if we realize the covering
                // test case covers an expensive method, we add all the covered methods for that
                // test case in relevantTestUnits
                temp.add(methodIndex);
            }
            if (!flag) {
                coveredMethods.addAll(temp);
            }
            temp.clear();
        }
    }

    // such a nasty procedure!
    private Iterator<String> getDominatorChains(final Set<Integer> e, // expensive methods
                                                final MayCallRel mayCallRel,
                                                final MethodsDom methodsDom) throws Exception {
        // constructing the relation >
        final Map<Integer, Integer> idom = new HashMap<>();
        mayCallRel.visit(new BinaryRelationVisitor() {
            final Set<Integer> visited = new HashSet<>();

            @Override
            public void visit(int caller, int callee) {
                if (e.contains(callee)) { // is callee an expensive method?
                    if (idom.containsKey(callee)) { // is callee dominated by some other method?
                        idom.remove(callee);
                        visited.add(callee);
                    } else if (!visited.contains(callee)) { // is callee black listed?
                        idom.put(callee, caller);
                    }
                }
            }
        });
        final BinaryRelation idomRel = new BinaryRelation("idom", methodsDom, methodsDom); // m idom n
        for (final Map.Entry<Integer, Integer> entry : idom.entrySet()) {
            idomRel.add(entry.getValue(), entry.getKey());
        }
        idomRel.save(".");
        idomRel.close();
        // constructing the relation >>
        Solver.runBDDBDDB("dominator.dlog", false);
        final BinaryRelation domRel = BinaryRelation.load("dom", methodsDom, methodsDom);
        // forming chains
        final List<List<Integer>> chains = new LinkedList<>();
        L: for (final int mi : e) {
            for (final List<Integer> chain : chains) {
                for (final int k : chain) {
                    if (domRel.contains(k, mi) || domRel.contains(mi, k)) {
                        chain.add(mi);
                        continue L;
                    }
                }
            }
            final List<Integer> chain = new LinkedList<>();
            chain.add(mi);
            chains.add(chain);
        }
        final Comparator<Integer> revDomComp = (mi1, mi2) -> {
            if (mi1.equals(mi2)) {
                return 0;
            }
            return domRel.contains(mi1, mi2) ? 1 : -1;
        };
        for (final List<Integer> chain : chains) {
            chain.sort(revDomComp);
        }
        // finalizing
        final class ChainIterator implements Iterator<String> {
            final Iterator<Iterator<String>> chains;

            Iterator<String> current = null;

            ChainIterator(final List<List<Integer>> chains) {
                final List<Iterator<String>> temp = new LinkedList<>();
                for (final List<Integer> chain : chains) {
                    temp.add(new Iterator<String>() {
                        final Iterator<Integer> it = chain.iterator();

                        @Override
                        public boolean hasNext() {
                            return this.it.hasNext();
                        }

                        @Override
                        public String next() {
                            return methodsDom.get(this.it.next());
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    });
                }
                this.chains = temp.iterator();
            }

            @Override
            public boolean hasNext() {
                while (this.current == null || !this.current.hasNext()) {
                    if (!chains.hasNext()) {
                        return false;
                    }
                    this.current = chains.next();
                }
                return true;
            }

            @Override
            public String next() {
                return this.current.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
        return new ChainIterator(chains);
    }
}
