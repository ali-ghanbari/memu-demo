package edu.iastate.memo.memoizer.client;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public interface MemoizationResults {
    /**
     *
     * @return -1 if provisional memoization failed
     */
    int getHits();

    /**
     *
     * @return -1 if provisional memoization failed
     */
    int getMisses();
}
