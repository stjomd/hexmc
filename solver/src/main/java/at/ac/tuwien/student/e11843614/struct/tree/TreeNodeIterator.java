package at.ac.tuwien.student.e11843614.struct.tree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * An iterator that traverses the tree in breadth first order, and returns subtrees.
 * @param <T> the type of elements in the tree.
 */
public class TreeNodeIterator<T> implements Iterator<TreeNode<T>> {

    private final Queue<TreeNode<T>> queue = new LinkedList<>();

    public TreeNodeIterator(TreeNode<T> node) {
        queue.add(node);
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public TreeNode<T> next() {
        if (hasNext()) {
            TreeNode<T> node = queue.remove();
            queue.addAll(node.getChildren());
            return node;
        }
        throw new NoSuchElementException("Tree contains no more nodes.");
    }

}
