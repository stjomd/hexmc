package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Graph;

public abstract class BranchDecompositionFactory {

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
