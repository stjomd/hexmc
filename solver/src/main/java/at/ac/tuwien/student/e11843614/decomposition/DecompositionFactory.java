package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueSingleton;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueUnion;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import org.sat4j.specs.TimeoutException;

import java.util.HashSet;
import java.util.Set;

public abstract class DecompositionFactory {

    /**
     * Constructs a carving decomposition of a graph.
     * @param graph the graph.
     * @return a carving decomposition, or null if it does not exist.
     */
    public static TreeNode<Set<Integer>> carving(Graph graph) throws TimeoutException {
        CarvingDerivation derivation = DerivationFactory.carving(graph);
        if (derivation != null) {
            return CarvingDecompositionFactory.from(derivation);
        } else {
            return null;
        }
    }

    /**
     * Constructs a branch decomposition for ps-width (binary tree containing vertices of the graph). The resulting
     * branch decomposition is not optimal in terms of ps-width.
     * @param graph the graph. For ps-width applications an incidence graph of the formula.
     * @return a branch decomposition for ps-width (a binary tree containing vertices of the graph), or null if it does
     * not exist.
     */
    public static TreeNode<Set<Integer>> pswBranch(Graph graph) throws TimeoutException {
        // We construct a carving decomposition and then transform it into a binary tree. But, if there is only one
        // vertex, a carving decomposition does not exist, while a branch decomposition does.
        if (graph.vertices().size() == 1) {
            return new TreeNode<>(Set.of(graph.vertices().iterator().next()));
        }
        // Next, determine a carving decomposition.
        TreeNode<Set<Integer>> carving = carving(graph);
        if (carving == null) {
            return null;
        } else if (carving.children().size() < 3) {
            // No need to transform into binary tree
            return carving;
        }
        // Transform into binary tree. First determine the 'nodeToMove', which will be the subtree with minimum height.
        int minHeight = Integer.MAX_VALUE;
        TreeNode<Set<Integer>> nodeToMove = null;
        for (TreeNode<Set<Integer>> child : carving.children()) {
            int height = child.getHeight();
            if (height < minHeight) {
                minHeight = height;
                nodeToMove = child;
            }
        }
        // Remove 'nodeToMove' from children of the root.
        carving.removeChild(nodeToMove);
        // Go in breadth-first order through the carving. The first leaf we find will have the smallest distance to root.
        // This will be our target where we will move 'nodeToMove'.
        TreeNode<Set<Integer>> target = null;
        for (TreeNode<Set<Integer>> node : carving) {
            if (node.children().isEmpty()) {
                target = node;
                break;
            }
        }
        // Now, 'target' is a leaf. Insert a union node above that will have as children the 'target' and 'nodeToMove'.
        if (target != null && nodeToMove != null) {
            Set<Integer> set = new HashSet<>(target.object());
            set.addAll(nodeToMove.object());
            TreeNode<Set<Integer>> internal = target.insertAbove(set);
            internal.addChild(nodeToMove);
            // Update sets above
            TreeNode<Set<Integer>> current = internal.parent();
            while (current != null) {
                current.object().addAll(set);
                current = current.parent();
            }
        }
        return carving;
    }

    /**
     * Construct a clique decomposition (parse tree for clique-width) of a graph.
     * @param graph the graph.
     * @return a clique decomposition, or null if it does not exist.
     */
    public static TreeNode<CliqueOperation> clique(Graph graph) throws TimeoutException {
        // There is no decomposition of the graph if there are no vertices
        if (graph.vertices().isEmpty()) {
            return null;
        }
        // The derivation factory will check starting with cw = 2. Check for cw = 1 here.
        // Clique-width is 1 if every component of the graph is a singleton (alternatively, there are no edges)
        if (graph.edges().isEmpty()) {
            // If there is one vertex, just return the singleton
            if (graph.vertices().size() == 1) {
                int vertex = graph.vertices().iterator().next();
                return new TreeNode<>(new CliqueSingleton(vertex, 1));
            }
            // Otherwise we just have to join all vertices by union
            TreeNode<CliqueOperation> root = new TreeNode<>(new CliqueUnion(graph.vertices(), 1));
            for (int vertex : graph.vertices()) {
                TreeNode<CliqueOperation> child = new TreeNode<>(new CliqueSingleton(vertex, 1));
                root.addChild(child);
            }
            CliqueDecompositionFactory.normalize(root);
            return root;
        }
        // Otherwise use the derivation factory
        CliqueDerivation derivation = DerivationFactory.clique(graph);
        return CliqueDecompositionFactory.from(derivation, graph);
    }

}
