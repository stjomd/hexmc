package at.ac.tuwien.student.e11843614.sat.factory;

import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;

/**
 * A class that is responsible for constructing a SAT encoding of a graph.
 */
public abstract class SATEncodingFactory {

    /**
     * Constructs a SAT encoding of a graph with a formula that is satisfiable iff carving-width of the graph is <= w.
     * @param graph the graph.
     * @param w the target carving-width.
     * @return a SAT encoding for this graph that contains a formula which is satisfiable if crw(graph) <= w.
     */
    public static SATEncoding forCarvingWidth(Graph graph, int w) {
        return SATEncodingFactoryForCarvingWidth.of(graph, w);
    }

    /**
     * Constructs a SAT encoding of a graph with a formula that is satisfiable iff clique-width of the graph is <= k.
     * @param graph the graph.
     * @param k the target clique-width.
     * @return a SAT encoding for this graph that contains a formula which is satisfiable if cw(graph) <= k.
     */
    public static SATEncoding forCliqueWidth(Graph graph, int k) {
        return SATEncodingFactoryForCliqueWidth.of(graph, k);
    }

}
