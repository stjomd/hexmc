package at.ac.tuwien.student.e11843614.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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

    private final Set<Edge> edges = new HashSet<>();
    private final Set<Integer> vertices = new HashSet<>();

    /**
     * Adds an edge between two vertices.
     * @param v a vertex, represented by an integer.
     * @param u a vertex, represented by an integer.
     */
    public void addEdge(Integer u, Integer v) {
        addEdge(new Edge(u, v));
    }

    /**
     * Adds an edge between two vertices.
     * @param edge the edge to be added.
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
        vertices.add(edge.getEndpoints()[0]);
        vertices.add(edge.getEndpoints()[1]);
    }

    /**
     * Returns the set of edges of this graph.
     * @return the set of Edge instances.
     */
    public Set<Edge> getEdges() {
        return edges;
    }

    /**
     * Creates a deep copy of the graph.
     * @return the duplicate of this graph.
     */
    public Graph duplicate() {
        Graph graph = new Graph();
        for (Edge edge : edges) {
            graph.addEdge(edge);
        }
        return graph;
    }

    /**
     * Returns a set of vertices adjacent to the given vertex.
     * @param vertex the vertex to find neighbors of.
     * @return the set of neighbors/adjacent vertices.
     */
    private Set<Integer> neighborsOf(Integer vertex) {
        Set<Integer> neighbors = new HashSet<>();
        for (Edge edge : edges) {
            if (edge.endpoints[0] == vertex) {
                neighbors.add(edge.endpoints[1]);
            } else if (edge.endpoints[1] == vertex) {
                neighbors.add(edge.endpoints[0]);
            }
        }
        return neighbors;
    }

    /**
     * Retrieves the shortest path between two vertices.
     * @param source the source vertex.
     * @param target the target vertex.
     * @return a list of vertices, representing the shortest path in the graph between source and target.
     */
    private List<Integer> path(Integer source, Integer target) {
        Map<Integer, Integer> parents = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        // Breadth-first search; keep track of parent nodes to retrieve path later
        queue.add(source);
        while (!queue.isEmpty()) {
            Integer vertex = queue.remove();
            if (vertex.equals(target)) {
                break;
            }
            visited.add(vertex);
            for (Integer neighbor : neighborsOf(vertex)) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    parents.put(neighbor, vertex);
                }
            }
        }
        // Backtrack to retrieve the path
        List<Integer> path = new ArrayList<>();
        Integer vertex = target;
        path.add(vertex);
        while (!vertex.equals(source)) {
            vertex = parents.get(vertex);
            if (vertex == null) {
                return null; // no path exists
            }
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Calculates the eccentricity of a vertex, i.e. the greatest distance between the given vertex and any other
     * vertex.
     * @param vertex the vertex to calculate the eccentricity of.
     * @return the eccentricity of the vertex.
     */
    private int eccentricity(Integer vertex) {
        int e = 0;
        for (Integer v : vertices) {
            List<Integer> path = path(vertex, v);
            if (path == null) {
                e = Integer.MAX_VALUE; // eccentricity is defined to be infinite if no path exists
            } else {
                e = Math.max(e, path.size() - 1);
            }
        }
        return e;
    }

    /**
     * Calculates the diameter of the graph, i.e. the largest eccentricity over all vertices, or, in other words,
     * the largest distance between any two vertices.
     * @return the diameter of the graph.
     */
    private int diameter() {
        int d = 0;
        for (Integer v : vertices) {
            d = Math.max(d, eccentricity(v));
        }
        return d;
    }

    @Override
    public String toString() {
        return edges.toString();
    }

}
