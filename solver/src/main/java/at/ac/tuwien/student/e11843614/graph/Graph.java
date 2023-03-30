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
public class Graph<T> {

    /**
     * An object that represents an undirected edge.
     */
//    public static class Edge<T> {
//        private final List<T> endpoints;
//        public Edge (T v, T u) {
//            this.endpoints = List.of(v, u);
//        }
//        public List<T> getEndpoints() {
//            return endpoints;
//        }
//        @Override
//        public String toString() {
//            return String.format("<%d, %d>", endpoints.get(0), endpoints.get(1));
//        }
//    }

    private final Set<Edge<T>> edges = new HashSet<>();
    private final Set<T> vertices = new HashSet<>();

    /**
     * Adds an edge between two vertices.
     * @param v a vertex, represented by an integer.
     * @param u a vertex, represented by an integer.
     */
    public void addEdge(T u, T v) {
        addEdge(new Edge<>(u, v));
    }

    /**
     * Adds an edge between two vertices.
     * @param edge the edge to be added.
     */
    public void addEdge(Edge<T> edge) {
        edges.add(edge);
        vertices.add(edge.getEndpoints().get(0));
        vertices.add(edge.getEndpoints().get(1));
    }

    public Set<T> getVertices() {
        return vertices;
    }

    /**
     * Returns the set of edges of this graph.
     * @return the set of Edge instances.
     */
    public Set<Edge<T>> getEdges() {
        return edges;
    }

    /**
     * Creates a deep copy of the graph.
     * @return the duplicate of this graph.
     */
    public Graph<T> duplicate() {
        Graph<T> graph = new Graph<>();
        for (Edge<T> edge : edges) {
            graph.addEdge(edge);
        }
        return graph;
    }

    /**
     * Returns a set of vertices adjacent to the given vertex.
     * @param vertex the vertex to find neighbors of.
     * @return the set of neighbors/adjacent vertices.
     */
    private Set<T> neighborsOf(T vertex) {
        Set<T> neighbors = new HashSet<>();
        for (Edge<T> edge : edges) {
            if (edge.getEndpoints().get(0) == vertex) {
                neighbors.add(edge.getEndpoints().get(1));
            } else if (edge.getEndpoints().get(1) == vertex) {
                neighbors.add(edge.getEndpoints().get(0));
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
    public List<T> path(T source, T target) {
        Map<T, T> parents = new HashMap<>();
        Queue<T> queue = new LinkedList<>();
        Set<T> visited = new HashSet<>();
        // Breadth-first search; keep track of parent nodes to retrieve path later
        queue.add(source);
        while (!queue.isEmpty()) {
            T vertex = queue.remove();
            if (vertex.equals(target)) {
                break;
            }
            visited.add(vertex);
            for (T neighbor : neighborsOf(vertex)) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    parents.put(neighbor, vertex);
                }
            }
        }
        // Backtrack to retrieve the path
        List<T> path = new ArrayList<>();
        T vertex = target;
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

    public int distance(T u, T v) {
        List<T> path = path(u, v);
        if (path == null) {
            return Integer.MAX_VALUE;
        } else {
            return path.size() - 1;
        }
    }

    /**
     * Calculates the eccentricity of a vertex, i.e. the greatest distance between the given vertex and any other
     * vertex.
     * @param vertex the vertex to calculate the eccentricity of.
     * @return the eccentricity of the vertex.
     */
    public int eccentricity(T vertex) {
        int e = 0;
        for (T v : vertices) {
            List<T> path = path(vertex, v);
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
    public int diameter() {
        int d = 0;
        for (T v : vertices) {
            d = Math.max(d, eccentricity(v));
        }
        return d;
    }

    @Override
    public String toString() {
        return edges.toString();
    }

}
