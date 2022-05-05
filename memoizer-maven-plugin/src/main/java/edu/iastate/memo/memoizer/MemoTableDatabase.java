package edu.iastate.memo.memoizer;
import edu.iastate.memo.constants.Params;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class MemoTableDatabase implements Serializable {
    private static final long serialVersionUID = 1L;

    // this array is to be used for mapping method indices to memo tables
    final MemoTable[][] memoTables;

    private MemoTableDatabase() {
        this.memoTables = new MemoTable[Params.UNIT_SIZE][];
    }

    public MemoTableDatabase(final List<Integer> memoizedMethods) {
        this();
        for (final int methodIndex : memoizedMethods) {
            final int unitIndex = methodIndex / Params.UNIT_SIZE;
            final int index = methodIndex % Params.UNIT_SIZE;
            if (this.memoTables[unitIndex] == null) {
                this.memoTables[unitIndex] = new MemoTable[Params.UNIT_SIZE];
            }
            this.memoTables[unitIndex][index] = new MemoTable();
        }
    }

    public MemoTable[][] getMemoTables() {
        return this.memoTables;
    }

    public MemoTable getMemoTable(final int methodIndex) {
        return getMemoTable(methodIndex / Params.UNIT_SIZE, methodIndex % Params.UNIT_SIZE);
    }

    public void setMemoTable(final int methodIndex, final MemoTable memoTable) {
        setMemoTable(methodIndex / Params.UNIT_SIZE, methodIndex % Params.UNIT_SIZE, memoTable);
    }

    public MemoTable getMemoTable(int unitIndex, int index) {
        return this.memoTables[unitIndex][index];
    }

    public void setMemoTable(int unitIndex, int index, final MemoTable memoTable) {
        if (this.memoTables[unitIndex] == null) {
            this.memoTables[unitIndex] = new MemoTable[Params.UNIT_SIZE];
        }
        this.memoTables[unitIndex][index] = memoTable;
    }

//    private Map<Integer, MemoTable> getMemoTablesMap() {
//        final Map<Integer, MemoTable> result = new HashMap<>();
//        int methodIndex = 0;
//        for (final MemoTable[] unit : this.memoTables) {
//            if (unit != null) {
//                for (final MemoTable memoTable : unit) {
//                    if (memoTable != null) {
//                        result.put(methodIndex, memoTable);
//                    }
//                    methodIndex++;
//                }
//            } else {
//                methodIndex += Params.UNIT_SIZE;
//            }
//        }
//        return result;
//    }

    public void writeOut(final ObjectOutputStream oos) throws IOException {
        oos.writeObject(this);
    }

    public static MemoTableDatabase readFromInput(final ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        return (MemoTableDatabase) ois.readObject();
    }
}
