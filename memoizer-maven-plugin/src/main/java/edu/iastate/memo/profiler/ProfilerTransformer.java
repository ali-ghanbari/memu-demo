package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.asm.ComputeClassWriter;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.commons.relational.MethodsDom;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.predicate.Predicate;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.pitest.bytecode.FrameOptions.pickFlags;

/**
 * Class file transformer for our dynamic analyses.
 * The analyses are to be chained together.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class ProfilerTransformer implements ClassFileTransformer {
    private static final Map<String, String> CACHE = new HashMap<>();

    private final ClassByteArraySource  byteArraySource;

    private final Predicate<String> appClassFilter;

    private final FieldsDom fieldsDom;

    private final MethodsDom methodsDom;

    private final Set<Integer> staticFields;

    private final Set<Integer> constructorMethods;

    public ProfilerTransformer(final ClassByteArraySource byteArraySource,
                               final Predicate<String> appClassFilter,
                               final FieldsDom fieldsDom,
                               final MethodsDom methodsDom,
                               final Set<Integer> staticFields,
                               final Set<Integer> constructorMethods) {
        this.byteArraySource = byteArraySource;
        this.appClassFilter = appClassFilter;
        this.fieldsDom = fieldsDom;
        this.methodsDom = methodsDom;
        this.staticFields = staticFields;
        this.constructorMethods = constructorMethods;
    }

    private boolean isAppClass(String className) {
        className = className.replace('/', '.');
        return this.appClassFilter.apply(className);
    }

    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) {
        if (!isAppClass(className)) {
            return null; // no transformation
        }
        final ClassReader classReader = new ClassReader(classfileBuffer);
        final ClassWriter classWriter = new ComputeClassWriter(this.byteArraySource, CACHE, pickFlags(classfileBuffer));
        final ClassVisitor classVisitor = new ProfilerTransformerClassVisitor(classWriter,
                this.fieldsDom,
                this.methodsDom,
                this.staticFields,
                this.constructorMethods);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }
}