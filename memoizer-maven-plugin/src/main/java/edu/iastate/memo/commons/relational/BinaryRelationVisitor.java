package edu.iastate.memo.commons.relational;

/**
 * An interface used to visit code of elements stored in a binary relation.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public interface BinaryRelationVisitor {
    void visit(int leftIndex, int rightIndex);
}