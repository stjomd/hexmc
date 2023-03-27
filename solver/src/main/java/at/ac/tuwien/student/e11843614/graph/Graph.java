package at.ac.tuwien.student.e11843614.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An object that represents an undirected graph, where vertices are represented using integers.
 */
public class Graph {

    // table.get(v) -> returns a set of vertices adjacent to v
    private final Map<Integer, Set<Integer>> table = new HashMap<>();

    /**
     * Adds an edge between two vertices.
     * @param v a vertex, represented by an integer.
     * @param u a vertex, represented by an integer.
     */
    public void addEdge(Integer v, Integer u) {
        if (!table.containsKey(v)) {
            table.put(v, new HashSet<>());
        }
        if (!table.containsKey(u)) {
            table.put(u, new HashSet<>());
        }
        table.get(v).add(u);
        table.get(u).add(v);
    }

    /**
     * Returns a boolean value indicating if an edge between two vertices is present.
     * @param v a vertex, represented by an integer.
     * @param u a vertex, represented by an integer.
     * @return true, if there exists an edge between v and u, and false otherwise.
     */
    public boolean hasEdge(Integer v, Integer u) {
        if (!table.containsKey(v)) {
            return false;
        }
        return table.get(v).contains(u);
    }

    @Override
    public String toString() {
        return table.toString();
    }
}
