package edu.iastate.memo.memoizer.client;

import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.memoizer.AbstractClassTransformer;
import edu.iastate.memo.memoizer.AdviceAdapterUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.util.Log;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class ClientTransformer extends AbstractClassTransformer {
    private final boolean provisionalMemoization;

    public ClientTransformer(final ClassByteArraySource byteArraySource,
                             final Predicate<String> appClassFilter,
                             final Collection<String> memoizedMethods,
                             final MethodsDom methodsDom) {
        this(byteArraySource, appClassFilter, memoizedMethods, methodsDom, false);
    }

    public ClientTransformer(final ClassByteArraySource byteArraySource,
                             final Predicate<String> appClassFilter,
                             final Collection<String> memoizedMethods,
                             final MethodsDom methodsDom,
                             final boolean provisionalMemoization) {
        super(byteArraySource, appClassFilter, memoizedMethods, methodsDom);
        this.provisionalMemoization = provisionalMemoization;
    }

    @Override
    protected MethodVisitor visitMethod(final MethodVisitor methodVisitor,
                                        final int access,
                                        final String name,
                                        final String descriptor,
                                        final int methodIndex) {
        return new TransformerMethodVisitor(methodVisitor, access, name, descriptor, methodIndex, provisionalMemoization);
    }

    protected static class TransformerMethodVisitor extends AdviceAdapter {
        protected static final Type CLIENT_STATE_MAN = Type.getType(ClientStateManager.class);

        protected static final Type CACHE_STATUS = Type.getType(CacheStatus.class);

        protected final int methodIndex;

        protected final boolean provisionalMemoization;

        public TransformerMethodVisitor(final MethodVisitor methodVisitor,
                                        final int access,
                                        final String name,
                                        final String descriptor,
                                        final int methodIndex,
                                        final boolean provisionalMemoization) {
            super(ASM7, methodVisitor, access, name, descriptor);
            this.methodIndex = methodIndex;
            this.provisionalMemoization = provisionalMemoization;
        }

        protected void injectHitLoggingCode() {
            if (this.provisionalMemoization) {
                push(this.methodIndex);
                invokeStatic(CACHE_STATUS, Method.getMethod("void recordCacheHit(int)"));
            }
        }

        protected void injectMissLoggingCode() {
            if (this.provisionalMemoization) {
                push(this.methodIndex);
                invokeStatic(CACHE_STATUS, Method.getMethod("void recordCacheMiss(int)"));
            }
        }

        protected void injectRetrievingCode() {
            final Type returnType = getReturnType();
            if (returnType.getSort() == Type.VOID) {
                push(this.methodIndex);
                AdviceAdapterUtils.loadArgArray(this);
                dup();
                invokeStatic(CLIENT_STATE_MAN, Method.getMethod("boolean retrieveVoidMethodOutput(int,java.lang.Object[],java.lang.Object[])"));
                final Label escape = new Label();
                ifZCmp(EQ, escape);
                injectHitLoggingCode();
//                injectLogMessagePrinter("***VOID-CACHE-HIT***");
                returnValue();
                mark(escape);
                injectMissLoggingCode();
//                injectLogMessagePrinter("---VOID-CACHE-MISS---");
            } else {
                final Type pitOption = Type.getType(Option.class);
                push(this.methodIndex);
                AdviceAdapterUtils.loadArgArray(this);
                dup();
                invokeStatic(CLIENT_STATE_MAN, Method.getMethod("org.pitest.functional.Option retrieveNonVoidMethodOutput(int,java.lang.Object[],java.lang.Object[])"));
                dup();
                invokeVirtual(pitOption, Method.getMethod("boolean hasSome()"));
                final Label escape = new Label();
                ifZCmp(EQ, escape);
                injectHitLoggingCode();
                invokeVirtual(pitOption, Method.getMethod("java.lang.Object value()"));
                unbox(returnType);
//                injectLogMessagePrinter("***NON-VOID-CACHE-HIT***");
                returnValue();
                mark(escape);
                pop();
                injectMissLoggingCode();
//                injectLogMessagePrinter("---NON-VOID-CACHE-MISS---");
            }
        }

//        protected void injectLogMessagePrinter(final String message) {
//            final Type pitLogger = Type.getType(Log.class);
//            final Type javaLogger = Type.getType(Logger.class);
//            invokeStatic(pitLogger, Method.getMethod("java.util.logging.Logger getLogger()"));
//            push(message);
//            invokeVirtual(javaLogger, Method.getMethod("void info(java.lang.String)"));
//        }

        @Override
        protected void onMethodEnter() {
            injectRetrievingCode();
        }
    }
}
