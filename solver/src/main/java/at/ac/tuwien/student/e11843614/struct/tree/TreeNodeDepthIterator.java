package at.ac.tuwien.student.e11843614.struct.tree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

public class TreeNodeDepthIterator<T> implements Iterator<TreeNode<T>> {

    private final Stack<TreeNode<T>> stack = new Stack<>();
    private final Set<TreeNode<T>> visited = new HashSet<>();

    public TreeNodeDepthIterator(TreeNode<T> node) {
        dive(node);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public TreeNode<T> next() {
        if (hasNext()) {
            TreeNode<T> node = stack.pop();
            if (!node.getChildren().isEmpty()) {
                for (TreeNode<T> child : node.getChildren()) {
                    dive(child);
                }
            }
            return node;
        }
        throw new NoSuchElementException("Tree contains no more nodes.");
    }

    private void dive(TreeNode<T> node) {
        TreeNode<T> current = node;
        while (current != null) {
            if (!visited.contains(current)) {
                stack.push(current);
                visited.add(current);
            }
            Iterator<TreeNode<T>> iterator = current.getChildren().iterator();
            if (iterator.hasNext()) {
                current = iterator.next();
            } else {
                current = null;
            }
        }
    }

}
