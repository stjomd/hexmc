package at.ac.tuwien.student.e11843614.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * A class representing a node of a tree.
 */
public class TreeNode {

    private Set<TreeNode> children = new HashSet<>();

    public TreeNode() {}

    public TreeNode(Set<TreeNode> children) {
        this.children = children;
    }

    /**
     * Adds a child to this node.
     * @param node the child node.
     */
    public void addChild(TreeNode node) {
        this.children.add(node);
    }

    /**
     * Returns the set of this node's children.
     * @return the set of children.
     */
    public Set<TreeNode> getChildren() {
        return children;
    }

}
