package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.collections.IntStack;
import edu.iastate.memo.commons.collections.NonNegativeIntSet;
import edu.iastate.memo.commons.relational.MayCallRel;
import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.constants.Params;

import java.util.Iterator;

/**
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class CallGraphRecorder {
    private static final IntStack CALL_STACK;

    private static long mainThreadId;

    private static final NonNegativeIntSet[][] CALL_GRAPH_EDGES;

    static {
        CALL_STACK = new IntStack();
        CALL_GRAPH_EDGES = new NonNegativeIntSet[Params.UNIT_SIZE][];
        mainThreadId = -1L;
    }

    private CallGraphRecorder() { }

    private static boolean threadSafe() {
        final long currentThreadId = Thread.currentThread().getId();
        if (mainThreadId == -1L) {
            mainThreadId = currentThreadId;
        }
        return mainThreadId == currentThreadId;
    }

    public static void enterMethod(final int callee) {
        if (threadSafe()) {
            final Integer caller = CALL_STACK.peek();
            if (caller != null) {
                NonNegativeIntSet edgeSet = CALL_GRAPH_EDGES[caller / Params.UNIT_SIZE][caller % Params.UNIT_SIZE];
                edgeSet.add(callee);
            }
            CALL_STACK.push(callee);
        }
    }

    public static void leaveMethod() {
        if (threadSafe()) CALL_STACK.pop();
    }

    static void allocateList(final int methodId) {
        final int unitIndex = methodId / Params.UNIT_SIZE;
        NonNegativeIntSet[] unit = CALL_GRAPH_EDGES[unitIndex];
        if (unit == null) {
            unit = new NonNegativeIntSet[Params.UNIT_SIZE];
            CALL_GRAPH_EDGES[unitIndex] = unit;
        }
        unit[methodId % Params.UNIT_SIZE] = new NonNegativeIntSet(Params.UNIT_SIZE * Long.SIZE);
    }

    static MayCallRel getCallGraph(final MethodsDom methodsDom) {
        final MayCallRel mayCallRel = new MayCallRel(methodsDom);
        int callerMethodId = 0;
        for (final NonNegativeIntSet[] unit : CALL_GRAPH_EDGES) {
            if (unit == null) {
                callerMethodId += Params.UNIT_SIZE;
            } else {
                for (final NonNegativeIntSet edgeSet : unit) {
                    if (edgeSet != null) {
                        final Iterator<Integer> it = edgeSet.createIterator();
                        while (it.hasNext()) {
                            final int calleeMethodId = it.next();
                            mayCallRel.add(callerMethodId, calleeMethodId);
                        }
                    }
                    callerMethodId++;
                }
            }
        }
        return mayCallRel;
    }
}