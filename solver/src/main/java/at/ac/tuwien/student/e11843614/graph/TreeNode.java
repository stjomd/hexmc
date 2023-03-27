package at.ac.tuwien.student.e11843614.graph;

import java.util.HashSet;
import java.util.Set;

public class TreeNode {

    private Set<TreeNode> children = new HashSet<>();

    public TreeNode() {}

    public TreeNode(Set<TreeNode> children) {
        this.children = children;
    }

    public void addChild(TreeNode node) {
        this.children.add(node);
    }

    public Set<TreeNode> getChildren() {
        return children;
    }

}
