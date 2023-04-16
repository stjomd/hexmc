package at.ac.tuwien.student.e11843614.struct.tree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An object that represents a node of a tree.
 */
public class TreeNode<T> implements Iterable<TreeNode<T>> {

    private T object;
    private TreeNode<T> parent = null;
    private final Set<TreeNode<T>> children = new HashSet<>();

    public TreeNode() {
        this.object = null;
    }
    public TreeNode(T object) {
        this.object = object;
    }

    /**
     * Sets the object stored in this node.
     * @param object the object to be stored.
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * Returns the object associated with this node.
     * @return the object.
     */
    public T getObject() {
        return object;
    }

    /**
     * Adds a child to this node.
     * @param node the new child node.
     */
    public void addChild(TreeNode<T> node) {
        this.children.add(node);
        node.parent = this;
    }

    /**
     * Removes a node from the set of this node's children.
     * Does nothing if the node was not contained in the set of this node's children.
     * @param node the child to be removed.
     */
    public void removeChild(TreeNode<T> node) {
        this.children.remove(node);
        node.parent = null;
    }

    /**
     * Detaches this node from its parent: this node loses its parent, the parent node loses this child.
     */
    public void detach() {
        if (parent != null) {
            parent.children.remove(this);
            this.parent = null;
        }
    }

    /**
     * Returns the parent of this node.
     * @return the parent of this node.
     */
    public TreeNode<T> getParent() {
        return parent;
    }

    /**
     * Returns the set of this node's children.
     * @return the set of children.
     */
    public Set<TreeNode<T>> getChildren() {
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
        StringBuilder builder = new StringBuilder();
        buildString(builder, "", "");
        return builder.toString();
    }

    // Inspired by https://stackoverflow.com/a/8948691
    private void buildString(StringBuilder builder, String currentPrefix, String nextPrefix) {
        builder.append(currentPrefix).append(object).append('\n');
        Iterator<TreeNode<T>> iterator = children.iterator();
        while (iterator.hasNext()) {
            TreeNode<T> child = iterator.next();
            if (iterator.hasNext()) {
                child.buildString(builder, nextPrefix + "├── ", nextPrefix + "│   ");
            } else {
                child.buildString(builder, nextPrefix + "└── ", nextPrefix + "    ");
            }
        }
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        return new TreeNodeIterator<>(this);
    }

}
