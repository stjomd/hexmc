package at.ac.tuwien.student.e11843614.struct.graph;

import java.util.LinkedList;
import java.util.List;

/**
 * A class that represents an edge between two vertices.
 */
public class Edge {

    private final List<Integer> endpoints;

    public Edge (int u, int v) {
        this.endpoints = new LinkedList<>();
        this.endpoints.add(u);
        this.endpoints.add(v);
    }

    public List<Integer> getEndpoints() {
        return endpoints;
    }

    @Override
    public String toString() {
        return "<" + endpoints.get(0).toString() + ", " + endpoints.get(1).toString() + ">";
    }

}
