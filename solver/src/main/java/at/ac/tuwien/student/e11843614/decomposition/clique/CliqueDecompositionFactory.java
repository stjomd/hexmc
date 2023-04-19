package at.ac.tuwien.student.e11843614.decomposition.clique;

import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueOperation;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueEdgeCreation;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueSingleton;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueRecoloring;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueUnion;
import at.ac.tuwien.student.e11843614.struct.Partition;
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
                // At union node right now
                CliqueUnion nodeOperation = (CliqueUnion) node.getObject();
                // Make sure we go through all children of the union node (mutations invalidate iterator of node.getChildren())
                Set<TreeNode<CliqueOperation>> visited = new HashSet<>();
                while (!visited.containsAll(node.getChildren())) {
                    for (TreeNode<CliqueOperation> child : node.getChildren()) {
                        if (visited.contains(child)) {
                            continue;
                        }
                        visited.add(child);
                        // ^^ Workaround for invalidating iterator
                        boolean childIsLeaf = false;
                        if (child.getObject() instanceof CliqueRecoloring) {
                            continue;
                        } else if (child.getObject() instanceof CliqueSingleton) {
                            childIsLeaf = true;
                        }
                        // Try possible color combinations. If this child is a leaf, it only stores a vertex with color
                        // assigned to 1. Therefore, we can save some loop iterations.
                        int fromColorBound = (childIsLeaf) ? 1 : width;
                        colors: for (int fromColor = 1; fromColor <= fromColorBound; fromColor++) {
                            for (int toColor = 1; toColor <= width; toColor++) {
                                if (fromColor == toColor) {
                                    continue;
                                }
                                Partition<Integer> grpBefore = grp(node);
                                TreeNode<CliqueOperation> recoloringNode = child.insertAbove(
                                    new CliqueRecoloring(fromColor, toColor)
                                );
                                Partition<Integer> grpAfter = grp(node);
                                // In 'node' is currently a union node under which we've just added a recoloring node.
                                // Consider the graph associated with 'node'. grp is a partition of its vertices s.t.
                                // two vertices are in same group/eq.class if they share the same color.
                                // We check two conditions. Firstly that the recoloring node changes anything, otherwise
                                // there is no point in adding it. Secondly, after adding the recoloring node, we check
                                // that grp is a subset of grp(T_i) where i is the level of 'node'.
                                boolean recoloringWithoutEffect = grpBefore.equals(grpAfter);
                                boolean fulfilsCondition = derivation.getGroups(nodeOperation.getLevel())
                                    .getEquivalenceClasses().containsAll(grpAfter.getEquivalenceClasses());
                                // If any of these conditions fails, we remove the node, and try another color pair.
                                if (recoloringWithoutEffect || !fulfilsCondition) {
                                    recoloringNode.contract();
                                } else {
                                    break colors;
                                }
                            }
                        }
                        // node.getChildren iterator invalid at this point.
                        break;
                    }
                }
            }
        }
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
                // TODO: avoids creating an edge node below an already existing edge node. Probably wrong
                if (node.getParent() != null && node.getParent().getObject() instanceof CliqueEdgeCreation) {
                    continue;
                }
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
