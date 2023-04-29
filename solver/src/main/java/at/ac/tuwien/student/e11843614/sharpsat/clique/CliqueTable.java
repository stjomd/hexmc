package at.ac.tuwien.student.e11843614.sharpsat.clique;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An object that maps (A, B, C) in [1, ..., k]^3 to an integer.
 */
public class CliqueTable {

    private final Map<Set<Integer>, Map<Set<Integer>, Map<Set<Integer>, Integer>>> map = new HashMap<>();

    /**
     * Returns the mapped integer.
     * @param a a subset of [1, ..., k].
     * @param b a subset of [1, ..., k].
     * @param c a subset of [1, ..., k].
     * @return the integer mapped to (a, b, c).
     */
    public int get(Set<Integer> a, Set<Integer> b, Set<Integer> c) {
        return map.get(a).get(b).get(c);
    }

    /**
     * Maps an integer to (a, b, c) in [1, ..., k]^3.
     * @param a a subset of [1, ..., k].
     * @param b a subset of [1, ..., k].
     * @param c a subset of [1, ..., k].
     * @param x the value to be mapped to (a, b, c).
     */
    public void set(Set<Integer> a, Set<Integer> b, Set<Integer> c, int x) {
        if (!map.containsKey(a)) {
            map.put(a, new HashMap<>());
        }
        Map<Set<Integer>, Map<Set<Integer>, Integer>> aMap = map.get(a);
        if (!aMap.containsKey(b)) {
            aMap.put(b, new HashMap<>());
        }
        Map<Set<Integer>, Integer> bMap = aMap.get(b);
        bMap.put(c, x);
    }

}
