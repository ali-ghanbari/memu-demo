package edu.iastate.memo.commons.relational;

/**
 * A binary relation representing may-call relation.
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class MayCallRel extends BinaryRelation {
    protected MayCallRel(final BinaryRelation binaryRel) {
        super(binaryRel);
    }

    public MayCallRel(MethodsDom methodsDom) {
        super("may_call", "_", methodsDom, methodsDom);
    }

    public static MayCallRel load(MethodsDom methodsDom) {
        final BinaryRelation binRel = BinaryRelation.load("may_call", methodsDom, methodsDom);
        return new MayCallRel(binRel);
    }

    public MethodsDom getMethodsDom() {
        return (MethodsDom) getLeftDomain();
    }
}
