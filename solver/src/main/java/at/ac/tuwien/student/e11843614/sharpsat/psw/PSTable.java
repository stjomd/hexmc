package at.ac.tuwien.student.e11843614.sharpsat.psw;

import at.ac.tuwien.student.e11843614.formula.Clause;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PSTable {

    private final Map<Set<Clause>, Map<Set<Clause>, Integer>> map = new HashMap<>();

    public int get(Set<Clause> c1, Set<Clause> c2) {
        return map.get(c1).get(c2);
    }

    public void set(Set<Clause> c1, Set<Clause> c2, int n) {
        if (!map.containsKey(c1)) {
            map.put(c1, new HashMap<>());
        }
        Map<Set<Clause>, Integer> c1Map = map.get(c1);
        c1Map.put(c2, n);
    }

}
