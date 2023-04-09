package at.ac.tuwien.student.e11843614.struct.graph;

import java.util.LinkedList;
import java.util.List;

/**
 * A class that represents an edge between two vertices.
 * @param <T> the class of this edge's endpoints (vertices).
 */
public class Edge<T> {

    private final List<T> endpoints;

    public Edge (T u, T v) {
        this.endpoints = new LinkedList<>();
        this.endpoints.add(u);
        this.endpoints.add(v);
    }

    public List<T> getEndpoints() {
        return endpoints;
    }

    @Override
    public String toString() {
        return "<" + endpoints.get(0).toString() + ", " + endpoints.get(1).toString() + ">";
    }

}
