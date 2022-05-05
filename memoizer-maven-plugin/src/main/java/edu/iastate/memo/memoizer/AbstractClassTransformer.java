package edu.iastate.memo.memoizer;

import edu.iastate.memo.commons.relational.MethodsDom;
import edu.iastate.memo.commons.asm.ComputeClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.predicate.Predicate;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static edu.iastate.memo.commons.misc.NameUtils.getMethodFullName;
import static org.objectweb.asm.Opcodes.ASM7;
import static org.pitest.bytecode.FrameOptions.pickFlags;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public abstract class AbstractClassTransformer implements ClassFileTransformer {
    private static final Map<String, String> CACHE = new HashMap<>();

    private final ClassByteArraySource byteArraySource;

    protected final Predicate<String> appClassFilter;

    protected final Set<Integer> memoizedMethods;

    protected final MethodsDom methodsDom;

    protected AbstractClassTransformer(final ClassByteArraySource byteArraySource,
                                       final Predicate<String> appClassFilter,
                                       final Collection<String> memoizedMethods,
                                       final MethodsDom methodsDom) {
        this.byteArraySource = byteArraySource;
        this.appClassFilter = appClassFilter;
        this.memoizedMethods = new HashSet<>(methodsDom.getIndices(memoizedMethods));
        this.methodsDom = methodsDom;
    }

    private boolean isAppClass(String className) {
        className = className.replace('/', '.');
        return this.appClassFilter.apply(className);
    }

    @Override
    public byte[] transform(final ClassLoader loader,
                            String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) {
        if (!isAppClass(className)) {
            return null; // no transformation
        }
        return transformClass(classfileBuffer);
    }

    public byte[] transformClass(final byte[] classfileBuffer) {
        final ClassReader classReader = new ClassReader(classfileBuffer);
        final ClassWriter classWriter = new ComputeClassWriter(this.byteArraySource, CACHE, pickFlags(classfileBuffer));
        final ClassVisitor classVisitor = new TransformerClassVisitor(classWriter,
                this.methodsDom,
                this.memoizedMethods);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    class TransformerClassVisitor extends ClassVisitor {
        private String classInternalName;

        private final MethodsDom methodsDom;

        private final Set<Integer> memoizedMethods;

        private boolean isInterface;

        public TransformerClassVisitor(final ClassVisitor classVisitor,
                                       final MethodsDom methodsDom,
                                       final Set<Integer> memoizedMethods) {
            super(ASM7, classVisitor);
            this.methodsDom = methodsDom;
            this.memoizedMethods = memoizedMethods;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.isInterface = java.lang.reflect.Modifier.isInterface(access);
            this.classInternalName = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(final int access,
                                         final String name,
                                         final String descriptor,
                                         final String signature,
                                         final String[] exceptions) {
            final MethodVisitor defaultMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (this.isInterface || java.lang.reflect.Modifier.isAbstract(access) || Modifier.isNative(access)) {
                return defaultMethodVisitor;
            }
            final String methodFullName = getMethodFullName(this.classInternalName, name, descriptor);
            final int methodIndex = this.methodsDom.getOrAdd(methodFullName);
            if (!this.memoizedMethods.contains(methodIndex)) {
                return defaultMethodVisitor;
            }
            return AbstractClassTransformer.this.visitMethod(defaultMethodVisitor, access, name, descriptor, methodIndex);
        }
    }

    protected abstract MethodVisitor visitMethod(final MethodVisitor methodVisitor,
                                                 final int access,
                                                 final String name,
                                                 final String descriptor,
                                                 int methodIndex);
}
