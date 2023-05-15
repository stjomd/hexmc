package at.ac.tuwien.student.e11843614.sharpsat.psw;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An object that maps elements of PS(F_v) x PS(F_-v) to integers.
 */
public class PSTable {

    private final Map<Set<Integer>, Map<Set<Integer>, Integer>> map = new HashMap<>();

    /**
     * Returns the integer mapped to the specified index.
     * @param c1 the first index.
     * @param c2 the second index.
     * @return the integer mapped to (c1, c2).
     */
    public int get(Set<Integer> c1, Set<Integer> c2) {
        return map.get(c1).get(c2);
    }

    /**
     * Maps an integer to the specified index.
     * @param c1 the first index.
     * @param c2 the second index.
     * @param n the integer to be mapped to (c1, c2).
     */
    public void set(Set<Integer> c1, Set<Integer> c2, int n) {
        if (!map.containsKey(c1)) {
            map.put(c1, new HashMap<>());
        }
        Map<Set<Integer>, Integer> c1Map = map.get(c1);
        c1Map.put(c2, n);
    }

}
