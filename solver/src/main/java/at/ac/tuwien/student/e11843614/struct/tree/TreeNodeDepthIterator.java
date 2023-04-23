package at.ac.tuwien.student.e11843614.struct.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that traverses a tree in post-order fashion.
 * @param <T> the type of objects stored in the tree.
 */
public class TreeNodeDepthIterator<T> implements Iterator<TreeNode<T>> {

    private final Deque<TreeNode<T>> deque = new ArrayDeque<>();

    public TreeNodeDepthIterator(TreeNode<T> node) {
        dive(node);
    }

    @Override
    public boolean hasNext() {
        return !deque.isEmpty();
    }

    @Override
    public TreeNode<T> next() {
        if (hasNext()) {
            return deque.pop();
        }
        throw new NoSuchElementException("Tree contains no more nodes.");
    }

    private void dive(TreeNode<T> node) {
        if (node == null) {
            return;
        }
        deque.push(node);
        for (TreeNode<T> child : node.children()) {
            dive(child);
        }
    }

}
