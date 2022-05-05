package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.relational.FieldsDom;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.Set;

import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.PUTSTATIC;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class FieldAccessTransformer extends GeneratorAdapter {
    private static final Type FIELD_ACCESSES_RECORDER = Type.getType(FieldAccessRecorder.class);

    private final int methodIndex;

    private final FieldsDom fieldsDom;

    private final Set<Integer> staticFields;

    private boolean hasFieldAccess;

    public FieldAccessTransformer(final MethodVisitor methodVisitor,
                                  final int access,
                                  final String name,
                                  final String descriptor,
                                  final int methodIndex,
                                  final FieldsDom fieldsDom,
                                  final Set<Integer> staticFields) {
        super(ASM7, methodVisitor, access, name, descriptor);
        this.methodIndex = methodIndex;
        this.fieldsDom = fieldsDom;
        this.staticFields = staticFields;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        final String fieldFullName = getFieldFullName(owner, name);
        final int fieldIndex = this.fieldsDom.getOrAdd(fieldFullName);
        populateStaticFieldsRel(opcode, fieldIndex);
        switch (opcode) {
            case GETFIELD:
            case GETSTATIC:
                insertRegisterFieldReadCall(fieldIndex);
                this.hasFieldAccess = true;
                break;
            case PUTSTATIC:
                insertRegisterFieldWriteCall(fieldIndex);
                this.hasFieldAccess = true;
        }
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    private void populateStaticFieldsRel(final int opcode, final int fieldIndex) {
        if (opcode == GETSTATIC || opcode == PUTSTATIC) {
            this.staticFields.add(fieldIndex);
        }
    }

    private void insertRegisterFieldReadCall(final int fieldIndex) {
        push(this.methodIndex);
        push(fieldIndex);
        invokeStatic(FIELD_ACCESSES_RECORDER, Method.getMethod("void registerFieldRead(int,int)"));
    }

    private void insertRegisterFieldWriteCall(final int fieldIndex) {
        push(this.methodIndex);
        push(fieldIndex);
        invokeStatic(FIELD_ACCESSES_RECORDER, Method.getMethod("void registerFieldWrite(int,int)"));
    }

    private static String getFieldFullName(String className, String fieldName) {
        return className.replace('/', '.') + "." + fieldName;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (this.hasFieldAccess) {
            FieldAccessRecorder.allocateSpace(this.methodIndex);
        }
    }
}
