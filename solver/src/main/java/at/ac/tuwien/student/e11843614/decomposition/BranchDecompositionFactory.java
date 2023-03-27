package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Graph;

public abstract class BranchDecompositionFactory {

    /**
     * Constructs an approximation of a branch decomposition according to a heuristic.
     * @param graph the graph to construct a branch decomposition of.
     * @return the root node of the branch decomposition.
     */
    public static BranchDecompositionNode heuristic(Graph graph) {
        // Create a star
        BranchDecompositionNode node = new BranchDecompositionNode();
        for (Graph.Edge edge : graph.getEdges()) {
            node.addChild(new BranchDecompositionNode(edge));
        }
        // TODO
        return node;
    }

}
