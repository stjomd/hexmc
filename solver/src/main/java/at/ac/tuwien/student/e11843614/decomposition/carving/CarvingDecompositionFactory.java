package at.ac.tuwien.student.e11843614.decomposition.carving;

import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;

import java.util.HashSet;
import java.util.Set;

public abstract class CarvingDecompositionFactory {

    /**
     * Constructs a carving decomposition from a derivation.
     * @param derivation the derivation.
     * @return a carving decomposition.
     */
    public static TreeNode<Set<Integer>> from(CarvingDerivation derivation) {
        // First create a tree that stores the corresponding eq. classes.
        Set<Integer> firstEC = derivation.getLevel(derivation.size()).iterator().next();
        TreeNode<Set<Integer>> root = new TreeNode<>(firstEC);
        // Each equivalence class corresponds to a node.
        Set<Set<Integer>> added = new HashSet<>();
        added.add(firstEC);
        for (int i = derivation.size() - 1; i >= 1; i--) {
            for (Set<Integer> ec : derivation.getLevel(i)) {
                // Look for the node in the current tree that contains the smallest, strict superset of ec.
                // Traverse in breadth first order, then the smallest superset will be the last superset.
                TreeNode<Set<Integer>> target = null;
                for (TreeNode<Set<Integer>> node : root) {
                    if (node.object().containsAll(ec) && node.object().size() > ec.size()) {
                        target = node;
                    }
                }
                // Don't add node if this ec was added already (avoids duplicate nodes)
                if (target != null && !added.contains(ec)) {
                    // Add child to target.
                    target.addChild(new TreeNode<>(ec));
                    added.add(ec);
                }
            }
        }
        return root;
    }

}
