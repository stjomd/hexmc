package at.ac.tuwien.student.e11843614.decomposition.clique.recoloring;

import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueRecoloring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An iterator that iterates over recoloring possibilities of a children of a union node.
 */
public class ChildrenRecoloringIterator implements Iterator<List<List<CliqueRecoloring>>> {

    // Iterates over List<List<CliqueRecoloring>>. Outer list refers to edges where a recoloring node is inserted.
    // The inner list represents a collection of recoloring nodes inserted above one child.

    // Above each child, we can insert at most k recoloring nodes. We can use a list to represent the amount of
    // recoloring nodes at each edge, and treat this list as a number in base k. For each edge and each number, we can
    // use the EdgeRecoloringIterator. A list of iterators is treated as a number as well. Once no iterators have any
    // next elements, the list of integers is incremented, and the respective iterators are re-initialized.

    private final int k;

    private final List<Integer> list;
    private final List<Iterator<List<CliqueRecoloring>>> iterators;

    private final List<List<CliqueRecoloring>> elements;
    private final Set<List<List<Long>>> visited = new HashSet<>();

    private boolean started = false;
    private boolean finishedFirstIterator = false;

    public ChildrenRecoloringIterator(int length, int k) {
        this.k = k;
        this.list = new ArrayList<>();
        this.elements = new ArrayList<>();
        this.iterators = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(1);
            Iterator<List<CliqueRecoloring>> iterator = new EdgeRecoloringIterator(1, k);
            iterators.add(iterator);
            elements.add(iterator.next());
        }
    }

    @Override
    public boolean hasNext() {
        if (!started || !finishedFirstIterator) {
            return true;
        } else {
            for (Integer value : list) {
                if (value != 0) {
                    return true;
                }
            }
            for (Iterator<List<CliqueRecoloring>> iterator : iterators) {
                if (iterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public List<List<CliqueRecoloring>> next() {
        if (hasNext()) {
            started = true;
            List<List<CliqueRecoloring>> el = copy(elements);
            visited.add(numerize(elements));
            boolean obtainedNew = false;
            while (!obtainedNew) {
                List<List<Long>> numbering = numerize(elements);
                if (visited.contains(numbering)) {
                    update(list.size() - 1);
                    if (!hasNext()) {
                        break;
                    }
                } else {
                    visited.add(numbering);
                    obtainedNew = true;
                }
            }
            return el;
        }
        throw new NoSuchElementException("There are no more recoloring possibilities");
    }

    /**
     * Creates a copy of a specified recoloring.
     * @param childrenRecoloring a recoloring.
     * @return a copy.
     */
    private List<List<CliqueRecoloring>> copy(List<List<CliqueRecoloring>> childrenRecoloring) {
        List<List<CliqueRecoloring>> result = new ArrayList<>();
        for (List<CliqueRecoloring> edgeRecoloring : childrenRecoloring) {
            result.add(new ArrayList<>(edgeRecoloring));
        }
        return result;
    }

    /**
     * Encodes a recoloring using a number.
     * @param childrenRecoloring a recoloring of a node's children.
     * @return an equivalent recoloring, where CliqueRecoloring objects are represented using longs.
     */
    private List<List<Long>> numerize(List<List<CliqueRecoloring>> childrenRecoloring) {
        List<List<Long>> result = new ArrayList<>();
        for (List<CliqueRecoloring> edgeRecoloring : childrenRecoloring) {
            List<Long> values = new ArrayList<>();
            for (CliqueRecoloring recoloring : edgeRecoloring) {
                // value = k(i-1) + j
                long value = (long) k*(recoloring.from() - 1) + recoloring.to();
                values.add(value);
            }
            result.add(values);
        }
        return result;
    }

    /**
     * Call this method after obtaining a result. This updates the data structures for the next obtaining.
     * @param index an index in lists. Call this method with index set to the last index of list.
     */
    private void update(int index) {
        if (index < 0) {
            // all iterators have no next elements
            increment(list.size() - 1);
            finishedFirstIterator = true;
            return;
        }
        if (iterators.get(index).hasNext()) {
            elements.set(index, iterators.get(index).next());
        } else {
            iterators.set(index, new EdgeRecoloringIterator(list.get(index), k));
            update(index - 1);
        }
    }

    /**
     * Increments the list.
     * @param index an index in the list. Call this method with index set to the last index of list.
     */
    private void increment(int index) {
        if (index < 0) {
            return;
        }
        if (list.get(index) < k) {
            list.set(index, list.get(index) + 1);
            iterators.set(index, new EdgeRecoloringIterator(list.get(index), k));
        } else {
            list.set(index, 0);
            increment(index - 1);
        }
    }

}
