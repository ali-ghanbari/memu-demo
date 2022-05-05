package edu.iastate.memo.memoizer.client;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
class HitMissPair implements MemoizationResults, Serializable {
    private static final long serialVersionUID = 1L;

    private final int hits;

    private final int misses;

    public HitMissPair(final int hits, final int misses) {
        this.hits = hits;
        this.misses = misses;
    }

    @Override
    public int getHits() {
        return this.hits;
    }

    @Override
    public int getMisses() {
        return this.misses;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HitMissPair)) {
            return false;
        }
        final HitMissPair that = (HitMissPair) o;
        return this.hits == that.hits && this.misses == that.misses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hits, this.misses);
    }
}
