package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.decomposition.branch.BranchDecompositionHeuristic;
import at.ac.tuwien.student.e11843614.decomposition.branch.BranchDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueDecompositionContents;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueDecompositionLeaf;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueDecompositionUnion;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;

import java.util.HashSet;
import java.util.Set;

public class DecompositionFactory {

    /**
     * Constructs a branch decomposition using a heuristic.
     * @param graph the graph.
     * @return a branch decomposition.
     */
    public static TreeNode<Edge> branchHeuristic(Graph graph) {
        return BranchDecompositionHeuristic.of(graph);
    }

    /**
     * Constructs a branch decomposition from a derivation.
     * @param derivation the derivation.
     * @return a branch decomposition.
     */
    public static TreeNode<Edge> branch(BranchDerivation derivation) {
        // First create a tree that stores the corresponding eq. classes.
        Set<Edge> firstEC = derivation.getLevel(derivation.size()).iterator().next();
        TreeNode<Set<Edge>> helper = new TreeNode<>(firstEC);
        // Each equivalence class corresponds to a node.
        Set<Set<Edge>> added = new HashSet<>();
        added.add(firstEC);
        for (int i = derivation.size() - 1; i >= 1; i--) {
            for (Set<Edge> ec : derivation.getLevel(i)) {
                // Look for the node in the current tree that contains the smallest, strict superset of ec.
                // Traverse in breadth first order, then the smallest superset will be the last superset.
                TreeNode<Set<Edge>> target = null;
                for (TreeNode<Set<Edge>> node : helper) {
                    if (node.getObject().containsAll(ec) && node.getObject().size() > ec.size()) {
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
        TreeNode<Edge> root = new TreeNode<>();
        transformHelperTree(helper, root);
        // The branch decomposition is the child of the resulting tree.
        root = root.getChildren().iterator().next();
        root.detach();
        return root;
    }

    /**
     * Construct a clique decomposition (parse tree for clique-width) from a derivation.
     * @param derivation the derivation.
     * @return a clique decomposition.
     */
    public static TreeNode<CliqueDecompositionContents> clique(CliqueDerivation derivation) {
        // At the root node we store cmp(T_t)
        int t = derivation.size() - 1;
        Set<Integer> rootCmp = derivation.getComponents(t).iterator().next();
        CliqueDecompositionContents rootContents = new CliqueDecompositionUnion(rootCmp, t);
        TreeNode<CliqueDecompositionContents> root = new TreeNode<>(rootContents);
        // Each component corresponds to either a union or a leaf node.
        Set<Set<Integer>> added = new HashSet<>();
        added.add(rootCmp);
        for (int i = t - 1; i >= 0; i--) {
            for (Set<Integer> cmp : derivation.getComponents(i)) {
                // Look for the union node in the current tree that contains the smallest, strict superset of cmp.
                // Traverse in breadth first order, then the smallest superset will be the last superset.
                TreeNode<CliqueDecompositionContents> target = null;
                for (TreeNode<CliqueDecompositionContents> node : root) {
                    if (node.getObject() instanceof CliqueDecompositionUnion) {
                        CliqueDecompositionUnion contents = (CliqueDecompositionUnion) node.getObject();
                        if (contents.getComponent().containsAll(cmp) && contents.getComponent().size() > cmp.size()) {
                            target = node;
                        }
                    }
                }
                if (target != null && !added.contains(cmp)) {
                    // Add child to target. If |cmp|=1, we add a leaf, otherwise a union node.
                    CliqueDecompositionContents contents;
                    if (cmp.size() == 1) {
                        int vertex = cmp.iterator().next();
                        contents = new CliqueDecompositionLeaf(cmp, i, vertex, 1);
                    } else {
                        contents = new CliqueDecompositionUnion(cmp, i);
                    }
                    target.addChild(new TreeNode<>(contents));
                    added.add(cmp);
                }
            }
        }
        // At this point we have a tree with leaf and union nodes.
        // TODO: add recoloring and edge creation nodes.
        return root;
    }

    /**
     * Transforms a helper tree storing equivalence classes into a branch decomposition.
     * @param helper the helper tree storing equivalence classes.
     * @param decomposition the tree where the branch decomposition will be stored. This node will have one child,
     *                      which will be the branch decomposition.
     */
    private static void transformHelperTree(TreeNode<Set<Edge>> helper, TreeNode<Edge> decomposition) {
        if (helper == null) {
            return;
        }
        TreeNode<Edge> decompositionNode = new TreeNode<>();
        if (helper.getObject().size() == 1) {
            decompositionNode.setObject(helper.getObject().iterator().next());
        }
        decomposition.addChild(decompositionNode);
        for (TreeNode<Set<Edge>> child : helper.getChildren()) {
            transformHelperTree(child, decompositionNode);
        }
    }

}
