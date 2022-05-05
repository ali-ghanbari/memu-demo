package edu.iastate.memo.commons.relational;

import edu.utdallas.relational.Domain;
import edu.utdallas.relational.Relation;

/**
 * A BDD-based <code>String</code>-typed unary relation.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class UnaryRelation {
    protected final Relation relation;

    protected UnaryRelation(Relation relation) {
        this.relation = relation;
    }

    public UnaryRelation(String name, StringDomain domain) {
        this.relation = createUninitializedRelation(name, domain);
        this.relation.zero();
    }

    private static Relation createUninitializedRelation(String name, StringDomain domain) {
        final Relation relation = new Relation();
        relation.setName(name);
        relation.setDoms(new Domain[]{domain});
        final String domName = domain.getName() + "0";
        relation.setSign(new String[]{domName}, domName);
        return relation;
    }

    public UnaryRelation load(String name, StringDomain domain) {
        final Relation relation = createUninitializedRelation(name, domain);
        relation.load(".");
        return new UnaryRelation(relation);
    }

    public void add(int idx) {
        this.relation.add(idx);
    }

    public void add(String val) {
        this.relation.add(val);
    }

    public boolean contains(int key) {
        return this.relation.contains(key);
    }

    public boolean contains(String val) {
        return this.relation.contains(val);
    }

    public void visit(UnaryRelationVisitor visitor) {
        for (final int idx : this.relation.getAry1IntTuples()) {
            visitor.visit(idx);
        }
    }

    public String getName() {
        return this.relation.getName();
    }

    public void save(String dirName) {
        this.relation.save(dirName);
    }

    public void close() {
        this.relation.close();
    }

    public int size() {
        return this.relation.size();
    }
}