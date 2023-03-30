package at.ac.tuwien.student.e11843614.graph;

import java.util.List;

/**
 * A class that represents an edge between two vertices.
 * @param <T> the class of this edge's endpoints (vertices).
 */
public class Edge<T> {

    private final List<T> endpoints;

    public Edge (T v, T u) {
        this.endpoints = List.of(v, u);
    }

    public List<T> getEndpoints() {
        return endpoints;
    }

    @Override
    public String toString() {
        return "<" + endpoints.get(0).toString() + ", " + endpoints.get(1).toString() + ">";
    }

}
