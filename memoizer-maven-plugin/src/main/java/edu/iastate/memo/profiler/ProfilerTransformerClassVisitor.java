package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.collections.Printer;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.commons.relational.MethodsDom;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.pitest.functional.F;

import java.lang.reflect.Modifier;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ASM7;

/**
 * Class transformer for timer and method coverage.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ProfilerTransformerClassVisitor extends ClassVisitor {
    private String classInternalName;

    private final FieldsDom fieldsDom;

    private final MethodsDom methodsDom;

    private final Set<Integer> staticFields;

    private final Set<Integer> constructorMethods;

    private boolean isInterface;

    ProfilerTransformerClassVisitor(final ClassVisitor classVisitor,
                                    final FieldsDom fieldsDom,
                                    final MethodsDom methodsDom,
                                    final Set<Integer> staticFields,
                                    final Set<Integer> constructorMethods) {
        super(ASM7, classVisitor);
        this.fieldsDom = fieldsDom;
        this.methodsDom = methodsDom;
        this.staticFields = staticFields;
        this.constructorMethods = constructorMethods;
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      final String superName,
                      final String[] interfaces) {
        this.isInterface = Modifier.isInterface(access);
        this.classInternalName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    private boolean isConstructor(final String name) {
        return name.matches("<init>|<clinit>");
    }

    @Override
    public MethodVisitor visitMethod(final int access,
                                     final String name,
                                     final String descriptor,
                                     final String signature,
                                     final String[] exceptions) {
        MethodVisitor defaultMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (this.isInterface || Modifier.isAbstract(access) || Modifier.isNative(access)) {
            return defaultMethodVisitor;
        }
        final String methodFullName = getMethodFullName(this.classInternalName, name, descriptor);
        final int methodIndex = this.methodsDom.getOrAdd(methodFullName);
        if (isConstructor(name)) {
            this.constructorMethods.add(methodIndex);
        }
        defaultMethodVisitor = new MethodCoverageTransformer(defaultMethodVisitor, access, name, descriptor, methodIndex);
        defaultMethodVisitor = new FieldAccessTransformer(defaultMethodVisitor, access, name, descriptor, methodIndex, this.fieldsDom, this.staticFields);
        defaultMethodVisitor = new TimerTransformer(defaultMethodVisitor, access, name, descriptor, methodIndex);
        return new CallGraphTransformer(defaultMethodVisitor, access, name, descriptor, methodIndex);
    }

    public static String getMethodFullName(final String className,
                                           final String methodName,
                                           final String descriptor) {
        return String.format("%s.%s(%s)",
                className.replace('/', '.'),
                methodName,
                Printer.join(Type.getArgumentTypes(descriptor), Type::getClassName, ","));
    }
}