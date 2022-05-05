package edu.iastate.memo.commons.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * A minimal method visitor for adding after advices in the form of finally blocks.
 * In case of constructor methods that call their overloaded constructors or super
 * constructors, the method behaves appropriately.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public abstract class FinallyBlockAdviceAdapter extends AdviceAdapter {
    private final Label startFinally;

    public FinallyBlockAdviceAdapter(final int api,
                                     final MethodVisitor methodVisitor,
                                     final int access,
                                     final String name,
                                     final String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
        this.startFinally = new Label();
    }

    @Override
    protected void onMethodEnter() {
        insertPrelude();
        super.visitLabel(this.startFinally);
    }

    private boolean isReturnInst(int opcode) {
        switch (opcode) {
            case RETURN:
            case IRETURN:
            case ARETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
                return true;
        }
        return false;
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (isReturnInst(opcode)) {
            insertSequel(true);
        }
    }

    protected abstract void insertPrelude();

    protected abstract void insertSequel(boolean normalExit);

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        final Label endFinally = new Label();
        super.visitTryCatchBlock(this.startFinally, endFinally, endFinally, null);
        super.visitLabel(endFinally);
        insertSequel(false);
        super.visitInsn(ATHROW);
        super.visitMaxs(maxStack, maxLocals);
    }
}