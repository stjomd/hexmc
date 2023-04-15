package at.ac.tuwien.student.e11843614.struct.graph;

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
 * An object that represents a graph.
 */
public class Graph {

    private final Set<Edge> edges = new HashSet<>();
    private final Set<Integer> vertices = new HashSet<>();

    // ----- Properties ------------------------------------------------------------------------------------------------

    /**
     * Returns the set of vertices in this graph.
     * @return the set of vertices.
     */
    public Set<Integer> getVertices() {
        return vertices;
    }

    /**
     * Returns the set of edges of this graph.
     * @return the set of Edge instances.
     */
    public Set<Edge> getEdges() {
        return edges;
    }

    /**
     * Returns a set of vertices adjacent to the given vertex.
     * @param vertex the vertex to find neighbors of.
     * @return the set of neighbors/adjacent vertices.
     */
    private Set<Integer> neighborsOf(int vertex) {
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

    // ----- Mutators --------------------------------------------------------------------------------------------------

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
     * Removes a vertex and the incident edges from this graph.
     * @param v the vertex to be removed.
     */
    public void removeVertex(int v) {
        vertices.remove(v);
        Set<Edge> removing = new HashSet<>();
        for (Edge edge : edges) {
            List<Integer> endpoints = edge.getEndpoints();
            if (endpoints.contains(v)) {
                removing.add(edge);
            }
        }
        edges.removeAll(removing);
    }

    /**
     * Removes an edge from the graph. Does not affect the graph's vertices.
     * @param edge the edge to be removed.
     */
    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    /**
     * Performs edge contraction in this graph. Contraction of an edge e=uv results in a graph without the edge e,
     * with u and v merged into a single vertex, and with edges to u/v being redirected to the new merged vertex.
     * @param edge the edge to be contracted.
     * The source vertex is always merged into target.
     */
    public void contractEdge(Edge edge) {
        int u = edge.getEndpoints().get(0), v = edge.getEndpoints().get(1);
        if (u == v) {
            // An edge that is a loop. In this case just remove the edge.
            this.edges.remove(edge);
            return;
        }
        // Choose a source and target vertex.
        Set<Integer> neighborsU = neighborsOf(u);
        Set<Integer> neighborsV = neighborsOf(v);
        boolean sourceIsU = neighborsU.size() < neighborsV.size();
        int source = sourceIsU ? u : v;
        int target = sourceIsU ? v : u;
        // Remove the edge, remove the source vertex. (it 'merges' into target)
        this.edges.remove(edge);
        this.vertices.remove(source);
        // Set of edges still contains edges to just removed source vertex. Find such, and 'redirect' them to target.
        Set<Edge> edgesToRedirect = new HashSet<>();
        for (Edge e : this.edges) {
            List<Integer> eEndpoints = e.getEndpoints();
            if (eEndpoints.contains(source)) {
                edgesToRedirect.add(e);
            }
        }
        for (Edge e : edgesToRedirect) {
            List<Integer> eEndpoints = e.getEndpoints();
            if (eEndpoints.get(0).equals(source)) {
                eEndpoints.set(0, target);
            } else if (eEndpoints.get(1).equals(source)) {
                eEndpoints.set(1, target);
            }
        }
    }

    // ----- Paths -----------------------------------------------------------------------------------------------------

    /**
     * Retrieves the shortest path between two vertices.
     * @param source the source vertex.
     * @param target the target vertex.
     * @return a list of vertices, representing the shortest path in the graph between source and target.
     */
    public List<Integer> path(int source, int target) {
        Map<Integer, Integer> parents = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        // Breadth-first search; keep track of parent nodes to retrieve path later
        queue.add(source);
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            if (vertex == target) {
                break;
            }
            visited.add(vertex);
            for (int neighbor : neighborsOf(vertex)) {
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
        while (vertex != source) {
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
    public int distance(int u, int v) {
        List<Integer> path = path(u, v);
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
    public int eccentricity(int vertex) {
        int eccentricity = 0;
        for (int v : vertices) {
            List<Integer> path = path(vertex, v);
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
        for (int v : vertices) {
            diameter = Math.max(diameter, eccentricity(v));
        }
        return diameter;
    }

    // ----- Miscellaneous ---------------------------------------------------------------------------------------------

    /**
     * Creates a duplicate of the graph. This method creates new Edge instances, but keeps the instances of the vertices.
     * @return the duplicate of this graph.
     */
    public Graph duplicate() {
        Graph graph = new Graph();
        for (int vertex : vertices) {
            graph.addVertex(vertex);
        }
        for (Edge edge : edges) {
            graph.addEdge(new Edge(edge.getEndpoints().get(0), edge.getEndpoints().get(1)));
        }
        return graph;
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

    /**
     * Computes a list of this graph's components.
     * @return a list of graphs, each a component, and a different object from this graph.
     */
    public List<Graph> components() {
        List<Graph> components = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        for (int vertex : vertices) {
            if (visited.contains(vertex)) {
                continue;
            }
            queue.add(vertex);
            Graph component = new Graph();
            component.addVertex(vertex);
            while (!queue.isEmpty()) {
                int visiting = queue.remove();
                visited.add(visiting);
                for (int neighbor : neighborsOf(visiting)) {
                    if (!component.hasEdgeWithEndpoints(visiting, neighbor)) {
                        component.addEdge(visiting, neighbor);
                    }
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }

    @Override
    public String toString() {
        return "(V=" + vertices + ", E=" + edges + ")";
    }

}
