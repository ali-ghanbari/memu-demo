package edu.iastate.memo.memoizer;

import edu.iastate.memo.commons.collections.NonNegativeIntSet;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.commons.relational.MethodsDom;
import org.apache.commons.lang3.Validate;
import org.pitest.functional.predicate.Predicate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public abstract class ChildProcessCommonArgs implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Predicate<String> appClassFilter;

    protected Collection<String> testClassNames;

    protected Collection<String> relevantTestUnits;

    protected Collection<String> memoizedMethods;

    protected transient MethodsDom methodsDom;

    protected transient FieldsDom fieldsDom;

    protected transient Map<Integer, List<Integer>> staticReads;

    protected transient Map<Integer, List<Integer>> staticAccesses;

    protected transient Map<Integer, NonNegativeIntSet> instanceReads;

    protected transient Map<Integer, NonNegativeIntSet> instanceAccesses;

    private transient boolean initialized;

    protected ChildProcessCommonArgs(final Predicate<String> appClassFilter,
                                     final Collection<String> testClassNames,
                                     final Collection<String> relevantTestUnits,
                                     final Collection<String> memoizedMethods) {
        Validate.isInstanceOf(Serializable.class, appClassFilter);
        Validate.isInstanceOf(Serializable.class, testClassNames);
        Validate.isInstanceOf(Serializable.class, relevantTestUnits);
        Validate.isInstanceOf(Serializable.class, memoizedMethods);
        this.appClassFilter = appClassFilter;
        this.testClassNames = testClassNames;
        this.relevantTestUnits = relevantTestUnits;
        this.memoizedMethods = memoizedMethods;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void init() {
        if (this.initialized) {
            return;
        }
        this.methodsDom = new MethodsDom(".");
        this.fieldsDom = new FieldsDom(".");
        try (final InputStream is = new FileInputStream("rel-static-reads");
             final ObjectInputStream input = new ObjectInputStream(is)) {
            this.staticReads = (HashMap) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (final InputStream is = new FileInputStream("rel-static-accesses");
             final ObjectInputStream input = new ObjectInputStream(is)) {
            this.staticAccesses = (HashMap) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.instanceReads = new HashMap<>();
        this.instanceAccesses = new HashMap<>();
        try (final InputStream is = new FileInputStream("rel-instance-reads");
             final ObjectInputStream input = new ObjectInputStream(is)) {
            this.instanceReads = (HashMap) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (final InputStream is = new FileInputStream("rel-instance-accesses");
             final ObjectInputStream input = new ObjectInputStream(is)) {
            this.instanceAccesses = (HashMap) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.initialized = true;
    }

    public Predicate<String> getAppClassFilter() {
        init();
        return this.appClassFilter;
    }

    public Collection<String> getTestClassNames() {
        init();
        return this.testClassNames;
    }

    public Collection<String> getRelevantTestUnits() {
        init();
        return this.relevantTestUnits;
    }

    public Collection<String> getMemoizedMethods() {
        init();
        return this.memoizedMethods;
    }

    public MethodsDom getMethodsDom() {
        init();
        return this.methodsDom;
    }

    public FieldsDom getFieldsDom() {
        init();
        return this.fieldsDom;
    }

    public Map<Integer, List<Integer>> getStaticReads() {
        init();
        return this.staticReads;
    }

    public Map<Integer, List<Integer>> getStaticAccesses() {
        init();
        return this.staticAccesses;
    }

    public Map<Integer, NonNegativeIntSet> getInstanceReads() {
        init();
        return this.instanceReads;
    }

    public Map<Integer, NonNegativeIntSet> getInstanceAccesses() {
        init();
        return this.instanceAccesses;
    }
}
