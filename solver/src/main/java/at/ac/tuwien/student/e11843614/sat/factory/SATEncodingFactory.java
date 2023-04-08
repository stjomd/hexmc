package at.ac.tuwien.student.e11843614.sat.factory;

import at.ac.tuwien.student.e11843614.graph.Graph;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;

/**
 * A class that is responsible for constructing a SAT encoding of a graph.
 */
public abstract class SATEncodingFactory {

    /**
     * Constructs a SAT encoding of a graph with a formula that is satisfiable iff branch-width of the graph is <= w.
     * @param graph the graph.
     * @param w the target branch-width.
     * @return a SAT encoding for this graph that contains a formula which is satisfiable if bw(graph) <= w.
     */
    public static SATEncoding forBranchWidth(Graph<Integer> graph, int w) {
        return SATEncodingFactoryForBranchWidth.of(graph, w);
    }

    /**
     * Constructs a SAT encoding of a graph with a formula that is satisfiable iff clique-width of the graph is <= k.
     * @param graph the graph.
     * @param k the target clique-width.
     * @return a SAT encoding for this graph that contains a formula which is satisfiable if cw(graph) <= k.
     */
    public static SATEncoding forCliqueWidth(Graph<Integer> graph, int k) {
        return SATEncodingFactoryForCliqueWidth.of(graph, k);
    }

}
