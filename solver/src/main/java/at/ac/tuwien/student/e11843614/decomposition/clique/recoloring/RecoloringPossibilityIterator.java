package at.ac.tuwien.student.e11843614.decomposition.clique.recoloring;

import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueRecoloring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class RecoloringPossibilityIterator implements Iterator<List<CliqueRecoloring>> {

    // nodes = [q_1, q_2, ..., q_n]
    // at each q_i, a recoloring node (i -> j) is possible; i != j; 1 <= i, j <= width.
    // number (i,j) pair as width*(i-1) + j. Max pair (width, width) corresponds to width^2.
    // make a list of length n of ints. each int corresponds to the pair of that recoloring node.
    // increment list like a number. once each int is width^2, we went through all possibilities.

    private final long width;
    private final List<Long> pairList;

    private boolean started = false;

    public RecoloringPossibilityIterator(int n, int width) {
        this.width = width;
        this.pairList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            pairList.add(1L);
        }
    }

    @Override
    public boolean hasNext() {
        if (!started) {
            return true;
        } else {
            for (Long pairNumber : pairList) {
                if (pairNumber != 1) {
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
            List<CliqueRecoloring> list = new ArrayList<>();
            for (Long pairValue : pairList) {
                int from = (int) Math.ceil((double) pairValue / width);
                int to = (int) (pairValue - width * (from - 1));
                list.add(new CliqueRecoloring(from, to));
            }
            increment(pairList.size() - 1);
            return list;
        }
        throw new NoSuchElementException("There are no more possibilities");
    }

    private void increment(int index) {
        if (index < 0) {
            return;
        }
        // if value == width^2 it results in i=j, so skip that.
        // look at last value, if its < width^2 - 1, just increment it by 1
        if (pairList.get(index) < width * width - 1) {
            pairList.set(index, pairList.get(index) + 1);
            // if i==j, increment once again at same index
            int i = (int) Math.ceil((double) pairList.get(index) / width);
            int j = (int) (pairList.get(index) - width*(i - 1));
            if (i == j) {
                increment(index);
            }
        } else {
            // otherwise we set it back to 1...
            pairList.set(index, 1L);
            // and increment the previous digit.
            increment(index - 1);
        }
    }

}
