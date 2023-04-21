package at.ac.tuwien.student.e11843614.decomposition.clique.recoloring;

import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueRecoloring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An iterator that iterates over recoloring possibilities of specified length (applicable to one edge in the tree).
 */
public class EdgeRecoloringIterator implements Iterator<List<CliqueRecoloring>> {

    // Each recoloring is a pair (i, j) with 1 <= i,j <= k; i != j.
    // Assign to each pair (i, j) a number k(i-1) + j, then we can iterate over these numbers to obtain a pair.
    // Maximum value is k^2, corresponding to (j, j).
    // Let pairList be a list of specified length initialized with ones. Treat it as a number in base k^2.
    // Then we can increment pairList (just like a number) and go through all recoloring possibilities of given length.

    private final long k;
    private final List<Long> list;
    private boolean started = false;

    public EdgeRecoloringIterator(int n, int k) {
        this.k = k;
        this.list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(2L);
        }
        while (hasDuplicates()) {
            increment(list.size() - 1);
            if (!hasNext()) {
                break;
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (!started) {
            return true;
        } else {
            for (Long value : list) {
                if (value != 2) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public List<CliqueRecoloring> next() {
        if (hasNext()) {
            started = true;
            List<CliqueRecoloring> recolorings = new ArrayList<>();
            for (Long value : list) {
                int from = (int) Math.ceil((double) value / k);
                int to = (int) (value - k * (from - 1));
                recolorings.add(new CliqueRecoloring(from, to));
            }
            increment(list.size() - 1);
            while (hasDuplicates()) {
                increment(list.size() - 1);
                if (!hasNext()) {
                    break;
                }
            }
            return recolorings;
        }
        throw new NoSuchElementException("There are no more possibilities");
    }

    private boolean hasDuplicates() {
        Set<Long> set = new HashSet<>();
        for (Long value : list) {
            if (set.contains(value)) {
                return true;
            } else {
                set.add(value);
            }
        }
        return false;
    }

    private void increment(int index) {
        if (index < 0) {
            return;
        }
        // if value == width^2 it results in i=j, so skip that.
        // look at last value, if its < width^2 - 1, just increment it by 1
        if (list.get(index) < k * k - 1) {
            list.set(index, list.get(index) + 1);
            // if i==j, increment once again at same index
            int i = (int) Math.ceil((double) list.get(index) / k);
            int j = (int) (list.get(index) - k *(i - 1));
            if (i == j) {
                increment(index);
            }
        } else {
            // otherwise we set it back to 2... (if 1, i=j=1)
            list.set(index, 2L);
            // and increment the previous digit.
            increment(index - 1);
        }
    }

}
