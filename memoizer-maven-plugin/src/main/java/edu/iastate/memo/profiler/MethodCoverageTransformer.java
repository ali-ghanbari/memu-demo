package edu.iastate.memo.profiler;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Transformer that injects code for tracking method coverage for each
 * test unit (i.e., a test suite or a test case)
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class MethodCoverageTransformer extends AdviceAdapter {
    private static final Type METHOD_COVERAGE_RECORDER = Type.getType(MethodCoverageRecorder.class);

    private final int methodIndex;

    public MethodCoverageTransformer(final MethodVisitor methodVisitor,
                                     final int access,
                                     final String name,
                                     final String descriptor,
                                     final int methodIndex) {
        super(ASM7, methodVisitor, access, name, descriptor);
        this.methodIndex = methodIndex;
    }

    @Override
    protected void onMethodEnter() {
        push(this.methodIndex);
        invokeStatic(METHOD_COVERAGE_RECORDER, Method.getMethod("void recordMethod(int)"));
    }

}
