package at.ac.tuwien.student.e11843614.decomposition.clique;

import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueDecompositionContents;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueDecompositionEdgeCreation;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueDecompositionLeaf;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueDecompositionRecoloring;
import at.ac.tuwien.student.e11843614.decomposition.clique.contents.CliqueDecompositionUnion;
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

    public static TreeNode<CliqueDecompositionContents> from(CliqueDerivation derivation, Graph graph) {
        TreeNode<CliqueDecompositionContents> root = createTreeWithLeavesAndUnion(derivation);
        addRecoloringNodes(root, derivation);
        for (Edge edge : graph.getEdges()) {
            addEdgeCreationNodes(root, edge);
            while (root.getParent() != null) {
                root = root.getParent();
            }
        }
        return root;
    }

    private static TreeNode<CliqueDecompositionContents> createTreeWithLeavesAndUnion(CliqueDerivation derivation) {
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
        return root;
    }

    private static void addRecoloringNodes(TreeNode<CliqueDecompositionContents> root, CliqueDerivation derivation) {
        int added = 0;
        int width = derivation.width();
        Iterator<TreeNode<CliqueDecompositionContents>> iterator = root.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<CliqueDecompositionContents> node = iterator.next();
            //System.out.println("visiting " + node.getObject());
            if (node.getObject() instanceof CliqueDecompositionUnion) {
                CliqueDecompositionUnion nodeOperation = (CliqueDecompositionUnion) node.getObject();
                // Make sure we go through all children (mutations invalidate iterator of node.getChildren())
                Set<TreeNode<CliqueDecompositionContents>> visited = new HashSet<>();
                childLoop: while (!visited.containsAll(node.getChildren())) {
                    for (TreeNode<CliqueDecompositionContents> child : node.getChildren()) {
                        if (visited.contains(child)) {
                            continue;
                        }
                        visited.add(child);
                        if (child.getObject() instanceof CliqueDecompositionRecoloring) {
                            continue;
                        }
                        // We add a recoloring node and test the condition. If it fails we remove the node.
                        for (int color = 2; color <= width; color++) {
                            TreeNode<CliqueDecompositionContents> recoloringNode = child.insertAbove(
                                new CliqueDecompositionRecoloring(1, color)
                            );
                            Partition<Integer> grpGr = grp(node);
                            // check if grp(G_q') subset of grp(T_i)
                            if (!derivation.getGroups(nodeOperation.getLevel()).getEquivalenceClasses()
                                .containsAll(grpGr.getEquivalenceClasses())) {
                                // Revert: remove the recoloring node, and try another color
                                recoloringNode.contract();
                            } else {
                                added++;
//                                if (added >= width) {
//                                    return;
//                                }
                                break childLoop; // not sure
                            }
                        }
                        // node.getChildren iterator invalid at this point.
                        break;
                    }
                }
            }
        }
    }

    private static void addEdgeCreationNodes(TreeNode<CliqueDecompositionContents> root, Edge edge) {
        int u = edge.getEndpoints().get(0);
        int v = edge.getEndpoints().get(1);
        System.out.println("edge: " + edge);
        // go through in breadth-first fashion, find union node with smallest i
        int targetLevel = Integer.MAX_VALUE;
        TreeNode<CliqueDecompositionContents> target = null;
        Map<Integer, List<CliqueDecompositionLeaf>> targetColorMap = null;
        for (TreeNode<CliqueDecompositionContents> node : root) {
            if (node.getObject() instanceof CliqueDecompositionUnion) {
                CliqueDecompositionUnion operation = ((CliqueDecompositionUnion) node.getObject());
                Map<Integer, List<CliqueDecompositionLeaf>> colorMap = colorMap(node);
                // The target vertex must have min level, and contain both u and v.
                boolean containsU = false, containsV = false;
                check: for (List<CliqueDecompositionLeaf> group : colorMap.values()) {
                    for (CliqueDecompositionLeaf leaf : group) {
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
        // If we found a target, insert an edge creation node above.
        if (target != null) {
            System.out.println("target: " + target.getObject());
            // u, v have different labels, determine those.
            int colorU = 0, colorV = 0;
            search: for (int color : targetColorMap.keySet()) {
                List<CliqueDecompositionLeaf> group = targetColorMap.get(color);
                for (CliqueDecompositionLeaf leaf : group) {
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
            if (colorU != 0 && colorV != 0 && colorU != colorV) {
                target.insertAbove(new CliqueDecompositionEdgeCreation(colorU, colorV));
            }
        }
    }

    private static Map<Integer, List<CliqueDecompositionLeaf>> colorMap(TreeNode<CliqueDecompositionContents> node) {
        // first store all leaf nodes. Then, for each leaf, backtrack to the top node while updating the color.
        Set<TreeNode<CliqueDecompositionContents>> leaves = new HashSet<>();
        for (TreeNode<CliqueDecompositionContents> subnode : node) {
            if (subnode.getObject() instanceof CliqueDecompositionLeaf) {
                leaves.add(subnode);
            }
        }
        // Color -> Subset of leaves with such color
        Map<Integer, List<CliqueDecompositionLeaf>> map = new HashMap<>();
        // For each leaf, backtrack back to node, taking recoloring nodes on the way into account.
        for (TreeNode<CliqueDecompositionContents> leaf : leaves) {
            CliqueDecompositionLeaf content = ((CliqueDecompositionLeaf) leaf.getObject());
            int color = content.getColor();
            TreeNode<CliqueDecompositionContents> currentNode = leaf;
            while (currentNode != node) {
                if (currentNode.getObject() instanceof CliqueDecompositionRecoloring) {
                    CliqueDecompositionRecoloring op = ((CliqueDecompositionRecoloring) currentNode.getObject());
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
    private static Partition<Integer> grp(TreeNode<CliqueDecompositionContents> node) {
        // Obtain grp(G_node). Idea: first store all leaf nodes. Then, for each leaf, backtrack to the top node while
        // updating the color.
        Set<TreeNode<CliqueDecompositionContents>> leaves = new HashSet<>();
        for (TreeNode<CliqueDecompositionContents> subnode : node) {
            if (subnode.getObject() instanceof CliqueDecompositionLeaf) {
                leaves.add(subnode);
            }
        }
        // Color -> Subset of leaves with such color
        Map<Integer, List<CliqueDecompositionLeaf>> map = new HashMap<>();
        // For each leaf, backtrack back to node, taking recoloring nodes on the way into account.
        for (TreeNode<CliqueDecompositionContents> leaf : leaves) {
            CliqueDecompositionLeaf content = ((CliqueDecompositionLeaf) leaf.getObject());
            int color = content.getColor();
            TreeNode<CliqueDecompositionContents> currentNode = leaf;
            while (currentNode != node) {
                if (currentNode.getObject() instanceof CliqueDecompositionRecoloring) {
                    CliqueDecompositionRecoloring op = ((CliqueDecompositionRecoloring) currentNode.getObject());
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
        // Now the map represents a partition of leaves; transform it into a Partition instance.
        Partition<Integer> partition = new Partition<>();
        for (List<CliqueDecompositionLeaf> group : map.values()) {
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
