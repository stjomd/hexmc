package at.ac.tuwien.student.e11843614.sharpsat.psw;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PSTableRefs {

    private final Map<Set<Integer>, Map<Set<Integer>, Integer>> map = new HashMap<>();

    public int get(Set<Integer> c1, Set<Integer> c2) {
        return map.get(c1).get(c2);
    }

    public void set(Set<Integer> c1, Set<Integer> c2, int n) {
        if (!map.containsKey(c1)) {
            map.put(c1, new HashMap<>());
        }
        Map<Set<Integer>, Integer> c1Map = map.get(c1);
        c1Map.put(c2, n);
    }

}
