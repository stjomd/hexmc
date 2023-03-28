package at.ac.tuwien.student.e11843614.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An object that represents an undirected graph, where vertices are represented using integers.
 */
public class Graph {

    /**
     * An object that represents an undirected edge.
     */
    public static class Edge {
        private final int[] endpoints;
        public Edge (int v, int u) {
            this.endpoints = new int[]{v, u};
        }
        public int[] getEndpoints() {
            return endpoints;
        }
        @Override
        public String toString() {
            return String.format("<%d, %d>", endpoints[0], endpoints[1]);
        }
    }

    // table.get(v) -> returns a set of vertices adjacent to v
    private final Map<Integer, Set<Integer>> table = new HashMap<>();
    private final Set<Edge> edges = new HashSet<>();

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
        edges.add(new Edge(v, u));
    }

    /**
     * Returns the set of edges of this graph.
     * @return the set of Edge instances.
     */
    public Set<Edge> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return table.toString();
    }

}
