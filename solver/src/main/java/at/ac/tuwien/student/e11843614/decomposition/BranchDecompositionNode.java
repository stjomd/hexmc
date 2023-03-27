package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Graph;
import at.ac.tuwien.student.e11843614.graph.TreeNode;

/**
 * A class representing a node of the branch decomposition tree, mapped to a graph's edge.
 */
public class BranchDecompositionNode extends TreeNode {

    private final Graph.Edge edge;

    public BranchDecompositionNode() {
        this.edge = null;
    }
    public BranchDecompositionNode(Graph.Edge edge) {
        this.edge = edge;
    }

    /**
     * Returns the edge associated with this node.
     * @return the edge.
     */
    public Graph.Edge getEdge() {
        return edge;
    }

    @Override
    public String toString() {
        return String.format("(%s, ch=%s)", edge, getChildren());
    }

}
