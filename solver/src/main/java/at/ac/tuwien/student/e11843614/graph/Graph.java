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
 * A class that represents a graph.
 * @param <T> the class of this graph's vertices.
 */
public class Graph<T> {

    private final Set<Edge<T>> edges = new HashSet<>();
    private final Set<T> vertices = new HashSet<>();

    // ----- Properties ------------------------------------------------------------------------------------------------

    /**
     * Returns the set of vertices in this graph.
     * @return the set of vertices.
     */
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

    // ----- Mutators --------------------------------------------------------------------------------------------------

    /**
     * Adds a vertex to this graph.
     * @param v the new vertex.
     */
    public void addVertex(T v) {
        vertices.add(v);
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

    /**
     * Adds an edge between two vertices.
     * @param v a vertex, represented by an integer.
     * @param u a vertex, represented by an integer.
     */
    public void addEdge(T u, T v) {
        addEdge(new Edge<>(u, v));
    }

    /**
     * Removes a vertex and the incident edges from this graph.
     * @param v the vertex to be removed.
     */
    public void removeVertex(T v) {
        vertices.remove(v);
        Set<Edge<T>> removing = new HashSet<>();
        for (Edge<T> edge : edges) {
            List<T> endpoints = edge.getEndpoints();
            if (endpoints.contains(v)) {
                removing.add(edge);
            }
        }
        edges.removeAll(removing);
    }

    /**
     * Performs edge contraction in this graph. Contraction of an edge e=uv results in a graph without the edge e,
     * with u and v merged into a single vertex, and with edges to u/v being redirected to the new merged vertex.
     * @param edge the edge to be contracted.
     * @return a list of two vertices, where the first is the source vertex, and the second the target vertex.
     * The source vertex is always merged into target.
     */
    public List<T> contractEdge(Edge<T> edge) {
        T u = edge.getEndpoints().get(0), v = edge.getEndpoints().get(1);
        // Choose a source and target vertex.
        Set<T> neighborsU = neighborsOf(u);
        Set<T> neighborsV = neighborsOf(v);
        boolean sourceIsU = neighborsU.size() < neighborsV.size();
        T source = sourceIsU ? u : v;
        T target = sourceIsU ? v : u;
        // Remove the edge, remove the source vertex. (it 'merges' into target)
        this.edges.remove(edge);
        this.vertices.remove(source);
        // Set of edges still contains edges to just removed source vertex. Find such, and 'redirect' them to target.
        Set<Edge<T>> edgesToRedirect = new HashSet<>();
        for (Edge<T> e : this.edges) {
            List<T> eEndpoints = e.getEndpoints();
            if (eEndpoints.contains(source)) {
                edgesToRedirect.add(e);
            }
        }
        for (Edge<T> e : edgesToRedirect) {
            List<T> eEndpoints = e.getEndpoints();
            if (eEndpoints.get(0).equals(source)) {
                eEndpoints.set(0, target);
            } else if (eEndpoints.get(1).equals(source)) {
                eEndpoints.set(1, target);
            }
        }
        return List.of(source, target);
    }

    // ----- Paths -----------------------------------------------------------------------------------------------------

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

    /**
     * Calculates the shortest distance between two vertices.
     * @param u the source vertex.
     * @param v the target vertex.
     * @return the distance between u and v.
     */
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
        int eccentricity = 0;
        for (T v : vertices) {
            List<T> path = path(vertex, v);
            if (path == null) {
                eccentricity = Integer.MAX_VALUE; // eccentricity is defined to be infinite if no path exists
            } else {
                eccentricity = Math.max(eccentricity, path.size() - 1);
            }
        }
        return eccentricity;
    }

    /**
     * Calculates the diameter of the graph, i.e. the largest eccentricity over all vertices, or, in other words,
     * the largest distance between any two vertices.
     * @return the diameter of the graph.
     */
    public int diameter() {
        int diameter = 0;
        for (T v : vertices) {
            diameter = Math.max(diameter, eccentricity(v));
        }
        return diameter;
    }

    // ----- Miscellaneous ---------------------------------------------------------------------------------------------

    /**
     * Creates a duplicate of the graph. This method creates new Edge instances, but keeps the instances of the vertices.
     * @return the duplicate of this graph.
     */
    public Graph<T> duplicate() {
        Graph<T> graph = new Graph<>();
        for (T vertex : vertices) {
            graph.addVertex(vertex);
        }
        for (Edge<T> edge : edges) {
            graph.addEdge(edge);
        }
        return graph;
    }

    @Override
    public String toString() {
        return edges.toString();
    }

}
