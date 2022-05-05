package edu.iastate.memo.memoizer;

import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.memoizer.client.MemoizationResults;
import edu.iastate.memo.memoizer.recorder.Recorder;
import edu.iastate.memo.memoizer.client.Client;
import org.pitest.functional.predicate.Predicate;
import org.pitest.process.ProcessArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of provisional memoization algorithm.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class ProvisionalMemoizer {
    private static final String MEMO_TABLE_DB_FILE_NAME = "memo-table-db";

    private final ProcessArgs processArgs;

    private final Predicate<String> appClassFilter;

    private final Collection<String> testClassNames;

    private final Map<String, List<Integer>> methodCoverage;

    private final Iterator<String> dominatorChainIt;

    private final Collection<String> expensiveMethodNames;

    private final Predicate<String> isFailingTest;

    private final File memoTableDB;

    private final MethodsDom methodsDom;

    private final Map<String, MemoizationResults> memoizationResults;

    public ProvisionalMemoizer(final ProcessArgs processArgs,
                               final Predicate<String> appClassFilter,
                               final Collection<String> testClassNames,
                               final Predicate<String> isFailingTest,
                               final Map<String, List<Integer>> methodCoverage,
                               final Iterator<String> dominatorChainIt,
                               final Collection<String> expensiveMethodNames,
                               final MethodsDom methodsDom) {
        this.processArgs = processArgs;
        this.appClassFilter = appClassFilter;
        this.testClassNames = testClassNames;
        this.dominatorChainIt = dominatorChainIt;
        this.expensiveMethodNames = expensiveMethodNames;
        this.methodCoverage = methodCoverage;
        this.isFailingTest = isFailingTest;
        this.memoTableDB = new File(MEMO_TABLE_DB_FILE_NAME);
        this.methodsDom = methodsDom;
        this.memoizationResults = new HashMap<>();
    }

    public List<String> tryMemoize() {
        List<String> relevantTestMethods = retrieveRelevantTestMethods(this.expensiveMethodNames);
        // memoize all method (this involves running test cases)
        memoize(relevantTestMethods);
        final LinkedList<String> memoizedMethods = new LinkedList<>();
        while (this.dominatorChainIt.hasNext()) {
            final String expensiveMethod = this.dominatorChainIt.next();
            System.out.printf("%nTesting method %s while %d methods are already tested%n", expensiveMethod, memoizedMethods.size());
            memoizedMethods.addLast(expensiveMethod);
            // run test cases while memoizedMethods is memoized
            // we run only those test cases that cover the methods in memoizedMethods
            relevantTestMethods = retrieveRelevantTestMethods(memoizedMethods);
            final MemoizationResults results = testMemoized(memoizedMethods,
                    expensiveMethod,
                    relevantTestMethods);
            // if no new failing test cases get introduced add expensiveMethod to memoizedMethods
            // otherwise set ok to false
            if (results.getHits() <= 0) {
                memoizedMethods.removeLast();
            } else {
                memoizationResults.put(expensiveMethod, results);
            }
        }
        return memoizedMethods;
    }

    public MemoizationResults getMemoizationResult(final String methodName) {
        return this.memoizationResults.get(methodName);
    }

    public File getMemoTableDB() {
        return this.memoTableDB;
    }

    private void memoize(final List<String> relevantTestUnits) {
        try {
            Recorder.runRecorder(this.processArgs,
                    this.appClassFilter,
                    this.testClassNames,
                    relevantTestUnits,
                    this.expensiveMethodNames);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private MemoizationResults testMemoized(final List<String> memoizedMethods,
                                            final String provisionallyMemoizedMethodName,
                                            final List<String> relevantTestUnits) {
        try {
            return Client.runClient(this.processArgs,
                    this.appClassFilter,
                    this.testClassNames,
                    this.isFailingTest,
                    relevantTestUnits,
                    memoizedMethods,
                    provisionallyMemoizedMethodName);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private List<String> retrieveRelevantTestMethods(final Collection<String> methodNames) {
        final Set<Integer> methods = new HashSet<>();
        for (final String methodName : methodNames) {
            methods.add(this.methodsDom.indexOf(methodName));
        }
        final List<String> relevantTestMethods = new ArrayList<>();
        for (final Map.Entry<String, List<Integer>> entry : this.methodCoverage.entrySet()) {
            for (final int methodIndex : entry.getValue()) {
                if (methods.contains(methodIndex)) {
                    relevantTestMethods.add(entry.getKey());
                    break;
                }
            }
        }
        return relevantTestMethods;
    }
}