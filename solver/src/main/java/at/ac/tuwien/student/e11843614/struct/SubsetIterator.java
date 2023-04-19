package at.ac.tuwien.student.e11843614.struct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An iterator that iterates over subsets of a set.
 * @param <T> the type of elements in the set.
 */
public class SubsetIterator<T> implements Iterator<List<T>> {

    private final List<T> list;
    private final List<Boolean> inclusion;

    private boolean returnedEmptySubset = false;

    public SubsetIterator(List<T> list) {
        this.list = list;
        this.inclusion = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            inclusion.add(false);
        }
    }

    public SubsetIterator(Set<T> set) {
        this(new ArrayList<>(set));
    }

    @Override
    public boolean hasNext() {
        // First iteration is over an empty subset. Then we return non-empty subsets until we cycle back to an empty
        // subset. This is when we don't have next subsets anymore.
        boolean allFalse = true;
        for (Boolean bool : inclusion) {
            if (bool.equals(true)) {
                allFalse = false;
                break;
            }
        }
        // allFalse is true if inclusion contains 'false' only, i.e. represents an empty subset.
        // hasNext = false  <==>  allFalse && returnedEmptySubset = true
        return !allFalse || !returnedEmptySubset;
    }

    @Override
    public List<T> next() {
        List<T> subset = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (inclusion.get(i).equals(true)) {
                subset.add(list.get(i));
            }
        }
        increment();
        returnedEmptySubset = true;
        return subset;
    }

    /**
     * Increments the inclusion list.
     */
    private void increment() {
        int j = inclusion.size() - 1;
        while (j >= 0 && inclusion.get(j).equals(true)) {
            inclusion.set(j, false);
            j--;
        }
        if (j >= 0) {
            inclusion.set(j, true);
        }
    }

}
