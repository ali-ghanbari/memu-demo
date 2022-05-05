package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.asm.FinallyBlockAdviceAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class CallGraphTransformer extends FinallyBlockAdviceAdapter {
    private static final Type CALL_GRAPH_RECORDER = Type.getType(CallGraphRecorder.class);

    private final int methodIndex;

    public CallGraphTransformer(final MethodVisitor methodVisitor,
                                final int access,
                                final String name,
                                final String descriptor,
                                final int methodIndex) {
        super(ASM7, methodVisitor, access, name, descriptor);
        this.methodIndex = methodIndex;
    }

    @Override
    protected void insertPrelude() {
        push(this.methodIndex);
        invokeStatic(CALL_GRAPH_RECORDER, Method.getMethod("void enterMethod(int)"));
    }

    @Override
    protected void insertSequel(boolean normalExit) {
        invokeStatic(CALL_GRAPH_RECORDER, Method.getMethod("void leaveMethod()"));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        CallGraphRecorder.allocateList(this.methodIndex);
    }
}
