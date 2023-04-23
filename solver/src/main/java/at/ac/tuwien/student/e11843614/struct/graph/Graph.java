package at.ac.tuwien.student.e11843614.struct.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An object that represents a graph.
 */
public class Graph {

    private final Set<Edge> edges = new HashSet<>();
    private final Set<Integer> vertices = new HashSet<>();

    /**
     * Returns the set of vertices in this graph.
     * @return the set of vertices.
     */
    public Set<Integer> vertices() {
        return vertices;
    }

    /**
     * Returns the set of edges of this graph.
     * @return the set of Edge instances.
     */
    public Set<Edge> edges() {
        return edges;
    }

    /**
     * Returns a set of vertices adjacent to the given vertex.
     * @param vertex the vertex to find neighbors of.
     * @return the set of neighbors/adjacent vertices.
     */
    public Set<Integer> getNeighbors(int vertex) {
        Set<Integer> neighbors = new HashSet<>();
        for (Edge edge : edges) {
            if (edge.getEndpoints().get(0) == vertex) {
                neighbors.add(edge.getEndpoints().get(1));
            } else if (edge.getEndpoints().get(1) == vertex) {
                neighbors.add(edge.getEndpoints().get(0));
            }
        }
        return neighbors;
    }

    /**
     * Adds a vertex to this graph.
     * @param v the new vertex.
     */
    public void addVertex(int v) {
        vertices.add(v);
    }

    /**
     * Adds an edge between two vertices.
     * @param edge the edge to be added.
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
        vertices.add(edge.getEndpoints().get(0));
        vertices.add(edge.getEndpoints().get(1));
    }

    /**
     * Adds an edge between two vertices.
     * @param v a vertex, represented by an integer.
     * @param u a vertex, represented by an integer.
     */
    public void addEdge(int u, int v) {
        addEdge(new Edge(u, v));
    }

    /**
     * Checks if this graph has an edge with specified endpoints.
     * @param u a vertex.
     * @param v a vertex.
     * @return true, if this graph has an edge uv or vu, and false otherwise.
     */
    public boolean hasEdgeWithEndpoints(int u, int v) {
        for (Edge edge : edges) {
            List<Integer> endpoints = edge.getEndpoints();
            if (endpoints.contains(u) && endpoints.contains(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "(V=" + vertices + ", E=" + edges + ")";
    }

}
