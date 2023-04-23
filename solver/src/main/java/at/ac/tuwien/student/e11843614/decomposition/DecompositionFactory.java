package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;

import java.util.HashSet;
import java.util.Set;

public class DecompositionFactory {

    /**
     * Constructs a carving decomposition from a derivation.
     * @param derivation the derivation.
     * @return a carving decomposition.
     */
    public static TreeNode<Integer> carving(CarvingDerivation derivation) {
        // First create a tree that stores the corresponding eq. classes.
        Set<Integer> firstEC = derivation.getLevel(derivation.size()).iterator().next();
        TreeNode<Set<Integer>> helper = new TreeNode<>(firstEC);
        // Each equivalence class corresponds to a node.
        Set<Set<Integer>> added = new HashSet<>();
        added.add(firstEC);
        for (int i = derivation.size() - 1; i >= 1; i--) {
            for (Set<Integer> ec : derivation.getLevel(i)) {
                // Look for the node in the current tree that contains the smallest, strict superset of ec.
                // Traverse in breadth first order, then the smallest superset will be the last superset.
                TreeNode<Set<Integer>> target = null;
                for (TreeNode<Set<Integer>> node : helper) {
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
        // Transform helper tree to a branch decomposition with the same structure.
        TreeNode<Integer> root = new TreeNode<>();
        transformHelperTree(helper, root);
        // The branch decomposition is the child of the resulting tree.
        root = root.children().iterator().next();
        root.detach();
        return root;
    }

    /**
     * Construct a clique decomposition (parse tree for clique-width) from a derivation.
     * @param derivation the derivation.
     * @return a clique decomposition.
     */
    public static TreeNode<CliqueOperation> clique(CliqueDerivation derivation, Graph graph) {
        return CliqueDecompositionFactory.from(derivation, graph);
    }

    /**
     * Transforms a helper tree storing equivalence classes into a branch decomposition.
     * @param helper the helper tree storing equivalence classes.
     * @param decomposition the tree where the branch decomposition will be stored. This node will have one child,
     *                      which will be the branch decomposition.
     */
    private static void transformHelperTree(TreeNode<Set<Integer>> helper, TreeNode<Integer> decomposition) {
        if (helper == null) {
            return;
        }
        TreeNode<Integer> decompositionNode = new TreeNode<>();
        if (helper.object().size() == 1) {
            decompositionNode.setObject(helper.object().iterator().next());
        }
        decomposition.addChild(decompositionNode);
        for (TreeNode<Set<Integer>> child : helper.children()) {
            transformHelperTree(child, decompositionNode);
        }
    }

}
