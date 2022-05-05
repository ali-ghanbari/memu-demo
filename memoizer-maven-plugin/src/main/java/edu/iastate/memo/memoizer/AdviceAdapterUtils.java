package edu.iastate.memo.memoizer;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.lang.reflect.Modifier;

public final class AdviceAdapterUtils {
    private AdviceAdapterUtils() { }

    public static void loadArgArray(final AdviceAdapter adapter) {
        final Type objectType = Type.getObjectType("java/lang/Object");
        final Type[] argumentTypes = adapter.getArgumentTypes();
        final boolean isStatic = Modifier.isStatic(adapter.getAccess());
        adapter.push(isStatic ? argumentTypes.length : 1 + argumentTypes.length);
        adapter.newArray(objectType);
        int arrayIndex = 0;
        if (!isStatic) {
            adapter.dup();
            adapter.push(arrayIndex++);
            adapter.loadThis();
            adapter.arrayStore(objectType);
        }
        for (int i = 0; i < argumentTypes.length; i++) {
            adapter.dup();
            adapter.push(arrayIndex++);
            adapter.loadArg(i);
            adapter.box(argumentTypes[i]);
            adapter.arrayStore(objectType);
        }
    }
}
