package at.ac.tuwien.student.e11843614.struct.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class TreeNodeDepthIterator<T> implements Iterator<TreeNode<T>> {

    private final Stack<TreeNode<T>> stack = new Stack<>();

    public TreeNodeDepthIterator(TreeNode<T> node) {
        stack.add(node);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public TreeNode<T> next() {
        if (hasNext()) {
            TreeNode<T> node = stack.pop();
            stack.addAll(node.getChildren());
            return node;
        }
        throw new NoSuchElementException("Tree contains no more nodes.");
    }

}
