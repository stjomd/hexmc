package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Edge;

import java.util.HashSet;
import java.util.Set;

/**
 * A class representing a node of the branch decomposition tree, mapped to a graph's edge.
 */
public class BranchDecompositionNode {

    private Edge<Integer> edge;
    private BranchDecompositionNode parent = null;
    private final Set<BranchDecompositionNode> children = new HashSet<>();

    public BranchDecompositionNode() {
        this.edge = null;
    }
    public BranchDecompositionNode(Edge<Integer> edge) {
        this.edge = edge;
    }

    /**
     * Sets the edge of this node.
     * @param edge the edge to be stored.
     */
    public void setEdge(Edge<Integer> edge) {
        this.edge = edge;
    }

    /**
     * Returns the edge associated with this node.
     * @return the edge.
     */
    public Edge<Integer> getEdge() {
        return edge;
    }

    /**
     * Adds a child to this node.
     * @param node the new child node.
     */
    public void addChild(BranchDecompositionNode node) {
        this.children.add(node);
        node.parent = this;
    }

    /**
     * Removes a node from the set of this node's children.
     * Does nothing if the node was not contained in the set of this node's children.
     * @param node the child to be removed.
     */
    public void removeChild(BranchDecompositionNode node) {
        this.children.remove(node);
        node.parent = null;
    }

    /**
     * Returns the parent of this node.
     * @return the parent of this node.
     */
    public BranchDecompositionNode getParent() {
        return parent;
    }

    /**
     * Returns the set of this node's children.
     * @return the set of children.
     */
    public Set<BranchDecompositionNode> getChildren() {
        return children;
    }

    /**
     * Returns the degree of this node, i.e. the amount of nodes it is adjacent to.
     * @return the degree.
     */
    public int getDegree() {
        int parentDegree = (parent == null) ? 0 : 1;
        return parentDegree + children.size();
    }

    @Override
    public String toString() {
        return String.format("(%s, ch=%s)", edge, getChildren());
    }

}
