package edu.iastate.memo.profiler;

import edu.iastate.memo.commons.relational.BinaryRelation;
import edu.iastate.memo.commons.relational.FieldsDom;
import edu.iastate.memo.commons.relational.MethodsDom;

import static edu.iastate.memo.constants.Params.UNIT_SIZE;

/**
 * The class for recording field accesses.
 * The class maintains a bit list of field codes that are accessed during
 * running the method.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class FieldAccessRecorder {
    private static final long[][][] FIELD_READS;

    private static final long[][][] FIELD_WRITES;

    static {
        FIELD_READS = new long[UNIT_SIZE][][];
        FIELD_WRITES = new long[UNIT_SIZE][][];
    }

    public static void registerFieldRead(final int methodIndex,
                                         final int fieldIndex) {
        final int unitIndex = methodIndex / UNIT_SIZE;
        final int index = methodIndex % UNIT_SIZE;
        final long[] reads = FIELD_READS[unitIndex][index];
        reads[fieldIndex / Long.SIZE] |= 1L << (fieldIndex % Long.SIZE);
    }

    public static void registerFieldWrite(final int methodIndex,
                                          final int fieldIndex) {
        final int unitIndex = methodIndex / UNIT_SIZE;
        final int index = methodIndex % UNIT_SIZE;
        final long[] writes = FIELD_WRITES[unitIndex][index];
        writes[fieldIndex / Long.SIZE] |= 1L << (fieldIndex % Long.SIZE);
    }

    static void allocateSpace(final int methodIndex) {
        final int unitIndex = methodIndex / UNIT_SIZE;
        final int index = methodIndex % UNIT_SIZE;
        if (FIELD_READS[unitIndex] == null) {
            FIELD_READS[unitIndex] = new long[UNIT_SIZE][];
        }
        FIELD_READS[unitIndex][index] = new long[UNIT_SIZE];
        if (FIELD_WRITES[unitIndex] == null) {
            FIELD_WRITES[unitIndex] = new long[UNIT_SIZE][];
        }
        FIELD_WRITES[unitIndex][index] = new long[UNIT_SIZE];
    }

    static BinaryRelation getDirectlyReadsRel(final MethodsDom methodsDom,
                                              final FieldsDom fieldsDom) {
        final BinaryRelation directlyReadsRel = new BinaryRelation("directly_reads", methodsDom, fieldsDom);
        populateBinaryRel(directlyReadsRel, FIELD_READS);
        return directlyReadsRel;
    }

    static BinaryRelation getDirectlyWritesRel(final MethodsDom methodsDom,
                                               final FieldsDom fieldsDom) {
        final BinaryRelation directlyWritesRel = new BinaryRelation("directly_writes", methodsDom, fieldsDom);
        populateBinaryRel(directlyWritesRel, FIELD_WRITES);
        return directlyWritesRel;
    }

    private static void populateBinaryRel(final BinaryRelation rel,
                                          final long[][][] bitmap) {
        int methodId = 0;
        for (final long[][] unit : bitmap) {
            if (unit == null) {
                methodId += UNIT_SIZE;
                continue;
            }
            for (final long[] accessList : unit) {
                if (accessList != null) {
                    for (int accessGroupIndex = 0; accessGroupIndex < accessList.length; accessGroupIndex++) {
                        long accessGroup = accessList[accessGroupIndex];
                        for (int bitIndex = 0; bitIndex < Long.SIZE; bitIndex++) {
                            if ((accessGroup & 1L) != 0L) {
                                final int fieldIndex = accessGroupIndex * Long.SIZE + bitIndex;
                                rel.add(methodId, fieldIndex);
                            }
                            accessGroup >>>= 1;
                        }
                    }
                }
                methodId++;
            }
        }
    }
}
