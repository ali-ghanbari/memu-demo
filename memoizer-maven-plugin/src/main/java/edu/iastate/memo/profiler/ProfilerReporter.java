package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.process.ChildProcessReporter;
import edu.iastate.memo.constants.ControlId;
import org.apache.commons.lang3.Validate;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Child-process-to-memoizer communication utilities.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ProfilerReporter extends ChildProcessReporter {
    public ProfilerReporter(OutputStream os) {
        super(os);
    }

    public synchronized void reportMethodsTiming(final Map<Integer, Double> methodsTiming) {
        Validate.isInstanceOf(Serializable.class, methodsTiming);
        this.dos.writeByte(ControlId.PROFILER_REPORT_METHODS_TIMING);
        this.dos.write((Serializable) methodsTiming);
        this.dos.flush();
    }

    public synchronized void reportMethodsCoverage(final Map<String, List<Integer>> methodCoverage) {
        Validate.isInstanceOf(Serializable.class, methodCoverage);
        this.dos.writeByte(ControlId.PROFILER_REPORT_METHOD_COVERAGE);
        this.dos.write((Serializable) methodCoverage);
        this.dos.flush();
    }

    public synchronized void reportConstructorMethods(final Set<Integer> constructorMethods) {
        Validate.isInstanceOf(Serializable.class, constructorMethods);
        this.dos.writeByte(ControlId.PROFILER_REPORT_CONSTRUCTORS);
        this.dos.write((Serializable) constructorMethods);
        this.dos.flush();
    }
}