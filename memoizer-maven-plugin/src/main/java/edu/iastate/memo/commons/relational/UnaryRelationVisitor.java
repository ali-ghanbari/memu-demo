package edu.iastate.memo.commons.relational;

/**
 * An interface used to visit elements stored in a unary relation.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public interface UnaryRelationVisitor {
    void visit(int index);
}