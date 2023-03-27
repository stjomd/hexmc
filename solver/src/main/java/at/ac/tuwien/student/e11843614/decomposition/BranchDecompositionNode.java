package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Graph;
import at.ac.tuwien.student.e11843614.graph.TreeNode;

public class BranchDecompositionNode extends TreeNode {

    private final Graph.Edge edge;

    public BranchDecompositionNode() {
        this.edge = null;
    }
    public BranchDecompositionNode(Graph.Edge edge) {
        this.edge = edge;
    }

    @Override
    public String toString() {
        return String.format("[BDNode, %s, children=%s]", edge, getChildren());
    }
}
