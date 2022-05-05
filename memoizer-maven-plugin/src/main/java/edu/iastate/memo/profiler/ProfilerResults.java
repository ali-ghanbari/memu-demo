package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.relational.BinaryRelation;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.commons.relational.MayCallRel;
import edu.iastate.memo.commons.relational.MethodsDom;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public interface ProfilerResults {
    MethodsDom getMethodsDom();

    FieldsDom getFieldsDom();

    Set<Integer> getConstructorMethods();

    MayCallRel getCallGraph();

    Map<String, List<Integer>> getMethodCoverageMap();

    Map<Integer, Double> getMethodTimingMap();

    BinaryRelation getInstanceReadsRel();

    BinaryRelation getStaticReadsRel();

    BinaryRelation getStaticWritesRel();

    BinaryRelation getStaticAccessesRel();
}