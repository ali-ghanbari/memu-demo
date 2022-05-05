package edu.iastate.memo.memoizer.recorder;

import edu.iastate.memo.commons.asm.FinallyBlockAdviceAdapter;
import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.memoizer.AbstractClassTransformer;
import edu.iastate.memo.memoizer.AdviceAdapterUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.predicate.Predicate;

import java.util.Collection;


/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class RecorderTransformer extends AbstractClassTransformer {

    public RecorderTransformer(ClassByteArraySource byteArraySource,
                               Predicate<String> appClassFilter,
                               Collection<String> memoizedMethods,
                               MethodsDom methodsDom) {
        super(byteArraySource, appClassFilter, memoizedMethods, methodsDom);
    }

    @Override
    protected MethodVisitor visitMethod(MethodVisitor methodVisitor, int access, String name, String descriptor, int methodIndex) {
        return new TransformerMethodVisitor(methodVisitor, access, name, descriptor, methodIndex);
    }

    static class TransformerMethodVisitor extends FinallyBlockAdviceAdapter {
        static final Type RECORDER_STATE_MAN = Type.getType(RecorderStateManager.class);

        final int methodIndex;

        public TransformerMethodVisitor(final MethodVisitor methodVisitor,
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
            AdviceAdapterUtils.loadArgArray(this);
            invokeStatic(RECORDER_STATE_MAN, Method.getMethod("void submitInput(int,java.lang.Object[])"));
        }

        @Override
        protected void insertSequel(boolean normalExit) {
            if (normalExit) {
                final Type returnType = getReturnType();
                if (returnType.getSort() == Type.VOID) {
                    push(this.methodIndex);
                    AdviceAdapterUtils.loadArgArray(this);
                    invokeStatic(RECORDER_STATE_MAN, Method.getMethod("void submitOutput(int,java.lang.Object[])"));
                } else {
                    // top of stack contains the value to be returned
                    if (returnType.getSize() == 2) {
                        dup2();
                    } else {
                        dup();
                    }
                    box(returnType);
                    push(this.methodIndex);
                    AdviceAdapterUtils.loadArgArray(this);
                    invokeStatic(RECORDER_STATE_MAN, Method.getMethod("void submitOutput(java.lang.Object,int,java.lang.Object[])"));
                }
            } else {
                // top of stack contains the thrown throwable object
                dup();
                push(this.methodIndex);
                AdviceAdapterUtils.loadArgArray(this);
                invokeStatic(RECORDER_STATE_MAN, Method.getMethod("void submitOutput(java.lang.Throwable,int,java.lang.Object[])"));
            }
        }
    }
}
