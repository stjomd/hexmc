package at.ac.tuwien.student.e11843614.decomposition.clique;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueOperation;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueEdgeCreation;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueSingleton;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueRecoloring;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueUnion;
import at.ac.tuwien.student.e11843614.decomposition.clique.recoloring.ChildrenRecoloringIterator;
import at.ac.tuwien.student.e11843614.decomposition.clique.recoloring.EdgeRecoloringIterator;
import at.ac.tuwien.student.e11843614.struct.Partition;
import at.ac.tuwien.student.e11843614.struct.SubsetIterator;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CliqueDecompositionFactory {

    /**
     * Constructs a clique decomposition (parse tree for clique-width) of a graph.
     * @param derivation the derivation.
     * @param graph the graph.
     * @return the root node of the parse tree.
     */
    public static TreeNode<CliqueOperation> from(CliqueDerivation derivation, Graph graph) {
        TreeNode<CliqueOperation> root = createTreeWithLeavesAndUnion(derivation);
        addRecoloringNodes(root, derivation);
        for (Edge edge : graph.getEdges()) {
            addEdgeCreationNode(root, edge);
            // A node might have been inserted above root
            while (root.getParent() != null) {
                root = root.getParent();
            }
        }
        return root;
    }

    /**
     * Creates an initial parse tree, containing leaf/singleton and union nodes.
     * @param derivation the derivation.
     * @return the root node of the parse tree.
     */
    private static TreeNode<CliqueOperation> createTreeWithLeavesAndUnion(CliqueDerivation derivation) {
        // At the root node we store cmp(T_t)
        int t = derivation.size() - 1;
        Set<Integer> rootCmp = derivation.getComponents(t).iterator().next();
        CliqueOperation rootContents = new CliqueUnion(rootCmp, t);
        TreeNode<CliqueOperation> root = new TreeNode<>(rootContents);
        // Each component corresponds to either a union or a leaf node.
        for (int i = t - 1; i >= 0; i--) {
            for (Set<Integer> cmp : derivation.getComponents(i)) {
                // Look for the union node in the current tree that contains the smallest superset of cmp.
                // Traverse in breadth first order, then the smallest superset will be the last superset.
                TreeNode<CliqueOperation> target = null;
                for (TreeNode<CliqueOperation> node : root) {
                    if (node.getObject() instanceof CliqueUnion) {
                        CliqueUnion contents = (CliqueUnion) node.getObject();
                        if (contents.getComponent().containsAll(cmp)) {
                            target = node;
                        }
                    }
                }
                if (target != null) {
                    int level = ((CliqueUnion) target.getObject()).getLevel();
                    // Add child to target. If target.level = 1, we add a leaf, otherwise a union node.
                    CliqueOperation contents;
                    if (level == 1) {
                        int vertex = cmp.iterator().next();
                        contents = new CliqueSingleton(cmp, i, vertex, 1);
                    } else {
                        contents = new CliqueUnion(cmp, i);
                    }
                    target.addChild(new TreeNode<>(contents));
                }
            }
        }
        return root;
    }

    /**
     * Adds recoloring nodes in the tree.
     * @param root the root of the tree.
     * @param derivation the derivation.
     */
    private static void addRecoloringNodes(TreeNode<CliqueOperation> root, CliqueDerivation derivation) {
        int width = derivation.width();
        Iterator<TreeNode<CliqueOperation>> iterator = root.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<CliqueOperation> node = iterator.next();
            if (node.getObject() instanceof CliqueUnion) {
                // If the conditions are already fulfilled, we don't have to insert anything.
                if (fulfilsColorConditions(node, derivation)) {
                    Logger.debug(node.getObject() + " fulfils color conditions, skip");
                    continue;
                }
                // If not, we try to paint the nodes.
                boolean painted;
                // Optimization: it's possible that, if this is a union node at level 1, that all singletons just have
                // to be painted different colors. Attempt this first. If this satisfies all conditions, we can move on
                // to other nodes.
                painted = attemptFirstLevelRecolorings(node, derivation);
                if (painted) {
                    Logger.debug(node.getObject() + " had its children painted different colors");
                    continue;
                }
                // Another optimization: often it's enough to only insert recoloring nodes above one of the children.
                // Attempt that and if that fails, move on to full brute forcing.
                Logger.debug(node.getObject() + " is being recolored using one-child-brute-force");
                painted = attemptEdgeRecolorings(node, derivation, width);
                if (painted) {
                    Logger.debug(node.getObject() + " had recoloring nodes inserted above one child");
                    continue;
                }
                // As a last resort, brute force.
                Logger.debug(node.getObject() + " requires full brute force");
                bruteforceRecolorings(node, derivation, width);
                Logger.debug(node.getObject() + " has been repainted using full brute force");
            }
        }
    }

    /**
     * Attempts to recolor the children of a union node at level 1 such that all singletons have different color.
     * If this does not fulfil the conditions, does nothing.
     * @param node a union node at level 1.
     * @param derivation the derivation.
     * @return true, if the recoloring nodes were added, and false otherwise.
     */
    private static boolean attemptFirstLevelRecolorings(TreeNode<CliqueOperation> node, CliqueDerivation derivation) {
        CliqueUnion nodeOperation = (CliqueUnion) node.getObject();
        if (nodeOperation.getLevel() != 1) {
            return false;
        }
        // Try to paint singletons different colors
        int i = 1;
        Set<TreeNode<CliqueOperation>> addedRecoloringNodes = new HashSet<>();
        Set<TreeNode<CliqueOperation>> children = new HashSet<>(node.getChildren());
        for (TreeNode<CliqueOperation> child : children) {
            if (i == 1) {
                i++;
                continue;
            }
            TreeNode<CliqueOperation> rec = child.insertAbove(new CliqueRecoloring(1, i));
            addedRecoloringNodes.add(rec);
            i++;
        }
        // Check conditions and revert if they fail
        if (fulfilsColorConditions(node, derivation)) {
            return true;
        } else {
            for (TreeNode<CliqueOperation> addedNode : addedRecoloringNodes) {
                addedNode.contract();
            }
            return false;
        }
    }

    /**
     * Attempts to insert recoloring nodes above one child of this node (for each child). If this does not fulfil
     * the conditions, does nothing.
     * @param node a union node.
     * @param derivation the derivation.
     * @return true, if the recoloring nodes were added, and false otherwise.
     */
    private static boolean attemptEdgeRecolorings(TreeNode<CliqueOperation> node, CliqueDerivation derivation, int width) {
        Set<TreeNode<CliqueOperation>> children = new HashSet<>(node.getChildren());
        for (TreeNode<CliqueOperation> child : children) {
            // Attempt to insert nodes above this child. We can insert from 1 to k nodes.
            for (int amount = 1; amount <= width; amount++) {
                Iterator<List<CliqueRecoloring>> recoloringIterator = new EdgeRecoloringIterator(amount, width);
                while (recoloringIterator.hasNext()) {
                    List<CliqueRecoloring> recolorings = recoloringIterator.next();
                    Set<TreeNode<CliqueOperation>> addedRecoloringNodes = new HashSet<>();
                    for (CliqueRecoloring recoloring : recolorings) {
                        TreeNode<CliqueOperation> newNode = child.insertAbove(recoloring);
                        addedRecoloringNodes.add(newNode);
                    }
                    // Check if conditions are fulfilled, and if necessary, revert
                    if (fulfilsColorConditions(node, derivation)) {
                        return true;
                    } else {
                        for (TreeNode<CliqueOperation> addedNode : addedRecoloringNodes) {
                            addedNode.contract();
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Tries all possible combinations of recoloring nodes over all possible subsets of the union node's children and
     * inserts them above the node's children, such that the conditions are fulfilled. Very inefficient.
     * @param node a union node, above whose children recoloring nodes will be inserted.
     * @param derivation the respective derivation.
     * @param width the width of this derivation.
     * @return true, if recoloring nodes were added, and false otherwise.
     */
    private static boolean bruteforceRecolorings(TreeNode<CliqueOperation> node, CliqueDerivation derivation, int width) {
        Iterator<List<TreeNode<CliqueOperation>>> childSubsetIterator = new SubsetIterator<>(node.getChildren());
        while (childSubsetIterator.hasNext()) {
            List<TreeNode<CliqueOperation>> childSubset = childSubsetIterator.next();
            Iterator<List<List<CliqueRecoloring>>> recoloringIterator = new ChildrenRecoloringIterator(childSubset.size(), width);
            while (recoloringIterator.hasNext()) {
                List<List<CliqueRecoloring>> recolorings = recoloringIterator.next();
                // Add recoloring nodes specified in 'recolorings'
                Set<TreeNode<CliqueOperation>> addedRecoloringNodes = new HashSet<>();
                for (int i = 0; i < childSubset.size(); i++) {
                    for (CliqueRecoloring recoloring : recolorings.get(i)) {
                        TreeNode<CliqueOperation> newNode = childSubset.get(i).insertAbove(recoloring);
                        addedRecoloringNodes.add(newNode);
                    }
                }
                // Check conditions, and if they fail, revert
                if (fulfilsColorConditions(node, derivation)) {
                    return true;
                } else {
                    for (TreeNode<CliqueOperation> addedNode : addedRecoloringNodes) {
                        addedNode.contract();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Adds an edge creation node for specified edge.
     * @param root the root of the tree.
     * @param edge the edge to create a node for.
     */
    private static void addEdgeCreationNode(TreeNode<CliqueOperation> root, Edge edge) {
        int u = edge.getEndpoints().get(0);
        int v = edge.getEndpoints().get(1);
        // Go through the nodes in breadth-first fashion, look at union nodes with component including both u, v; store
        // the node with the smallest level. Since we're traversing in breadth-first fashion, the last passing node will
        // be the one with the smallest level. In colorMap, we store a map from colors to vertices.
        int targetLevel = Integer.MAX_VALUE;
        TreeNode<CliqueOperation> target = null;
        Map<Integer, List<CliqueSingleton>> targetColorMap = null;
        for (TreeNode<CliqueOperation> node : root) {
            if (node.getObject() instanceof CliqueUnion) {
                CliqueUnion operation = ((CliqueUnion) node.getObject());
                Map<Integer, List<CliqueSingleton>> colorMap = colorMap(node);
                // The target vertex must have min level, and contain both u and v.
                boolean containsU = false, containsV = false;
                check: for (List<CliqueSingleton> group : colorMap.values()) {
                    for (CliqueSingleton leaf : group) {
                        if (leaf.getVertex() == u) {
                            containsU = true;
                        } else if (leaf.getVertex() == v) {
                            containsV = true;
                        }
                        if (containsU && containsV) {
                            break check;
                        }
                    }
                }
                if (containsU && containsV && operation.getLevel() < targetLevel) {
                    target = node;
                    targetLevel = operation.getLevel();
                    targetColorMap = colorMap;
                }
            }
        }
        // If we found a target, insert an edge creation node above
        if (target != null) {
            // Determine the colors of u and v
            int colorU = 0, colorV = 0;
            search: for (int color : targetColorMap.keySet()) {
                List<CliqueSingleton> group = targetColorMap.get(color);
                for (CliqueSingleton leaf : group) {
                    if (leaf.getVertex() == u) {
                        colorU = color;
                    } else if (leaf.getVertex() == v) {
                        colorV = color;
                    }
                    if (colorU != 0 && colorV != 0) {
                        break search;
                    }
                }
            }
            // If u and v have different colors, add the edge creation node
            if (colorU != 0 && colorV != 0 && colorU != colorV) {
                target.insertAbove(new CliqueEdgeCreation(colorU, colorV));
            }
        }
    }

    // ----- Helpers ---------------------------------------------------------------------------------------------------

    /**
     * Checks if this union node satisfies the color conditions.
     * @param node a union node.
     * @param derivation the derivation.
     * @return true, if the conditions are satisfied, and false otherwise.
     */
    private static boolean fulfilsColorConditions(TreeNode<CliqueOperation> node, CliqueDerivation derivation) {
        CliqueUnion nodeOperation = (CliqueUnion) node.getObject();
        Partition<Integer> grp = grp(node);
        return derivation.getGroups(nodeOperation.getLevel()).getEquivalenceClasses()
            .containsAll(grp.getEquivalenceClasses());
    }

    /**
     * Computes a map from colors to vertices/singletons in a graph associated with node.
     * @param node the node of the tree.
     * @return the map.
     */
    private static Map<Integer, List<CliqueSingleton>> colorMap(TreeNode<CliqueOperation> node) {
        // first store all leaf nodes. Then, for each leaf, backtrack to the top node while updating the color.
        Set<TreeNode<CliqueOperation>> leaves = new HashSet<>();
        for (TreeNode<CliqueOperation> subnode : node) {
            if (subnode.getObject() instanceof CliqueSingleton) {
                leaves.add(subnode);
            }
        }
        // Color -> Subset of leaves with such color
        Map<Integer, List<CliqueSingleton>> map = new HashMap<>();
        // For each leaf, backtrack back to node, taking recoloring nodes on the way into account.
        for (TreeNode<CliqueOperation> leaf : leaves) {
            CliqueSingleton content = ((CliqueSingleton) leaf.getObject());
            int color = content.getColor();
            TreeNode<CliqueOperation> currentNode = leaf;
            while (currentNode != node) {
                if (currentNode.getObject() instanceof CliqueRecoloring) {
                    CliqueRecoloring op = ((CliqueRecoloring) currentNode.getObject());
                    if (color == op.getFrom()) {
                        color = op.getTo();
                    }
                }
                currentNode = currentNode.getParent();
            }
            // Color is now determined; store the leaf in the map.
            if (!map.containsKey(color)) {
                map.put(color, new ArrayList<>());
            }
            map.get(color).add(content);
        }
        return map;
    }

    /**
     * Computes grp(G_node), a partition of vertices of the graph associated with node, such that two vertices are in
     * the same equivalence class if they share the same color.
     * @param node the node of the tree.
     * @return the partition.
     */
    private static Partition<Integer> grp(TreeNode<CliqueOperation> node) {
        Map<Integer, List<CliqueSingleton>> map = colorMap(node);
        // Now the map represents a partition of leaves; transform it into a Partition instance.
        Partition<Integer> partition = new Partition<>();
        for (List<CliqueSingleton> group : map.values()) {
            if (group.size() == 1) {
                partition.add(group.get(0).getVertex());
            } else {
                for (int i = 1; i < group.size(); i++) {
                    partition.add(group.get(i - 1).getVertex(), group.get(i).getVertex());
                }
            }
        }
        return partition;
    }

}
