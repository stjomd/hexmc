package at.ac.tuwien.student.e11843614.decomposition.clique;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueEdgeCreation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueSingleton;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueRecoloring;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueUnion;
import at.ac.tuwien.student.e11843614.decomposition.clique.recoloring.ChildrenRecoloringIterator;
import at.ac.tuwien.student.e11843614.decomposition.clique.recoloring.EdgeRecoloringIterator;
import at.ac.tuwien.student.e11843614.struct.Partition;
import at.ac.tuwien.student.e11843614.struct.SubsetIterator;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CliqueDecompositionFactory {

    /**
     * Constructs a normalized clique decomposition (parse tree for clique-width) of a graph. Normalized in the context
     * means that union nodes have exactly two children.
     * @param derivation the derivation.
     * @param graph the graph.
     * @return the root node of the parse tree.
     */
    public static TreeNode<CliqueOperation> from(CliqueDerivation derivation, Graph graph) {
        TreeNode<CliqueOperation> root = createTreeWithLeavesAndUnion(derivation);
        addRecoloringNodes(root, derivation);
        for (Edge edge : graph.edges()) {
            addEdgeCreationNode(root, edge);
            // A node might have been inserted above root
            while (root.parent() != null) {
                root = root.parent();
            }
        }
        normalize(root);
        return root;
    }

    /**
     * Transforms a clique decomposition into one where graphs under union nodes use disjoint sets of colors.
     * @param root a normalized clique decomposition, where union nodes have exactly two children.
     */
    public static void makeDisjointColorSets(TreeNode<CliqueOperation> root) {
        // Determine the maximum color label
        int shift = 0;
        for (TreeNode<CliqueOperation> node : root) {
            if (!(node.object() instanceof CliqueRecoloring)) {
                continue;
            }
            CliqueRecoloring recoloring = (CliqueRecoloring) node.object();
            shift = Math.max(shift, Math.max(recoloring.getFrom(), recoloring.getTo()));
        }
        // Go through union nodes in post-order fashion
        Iterator<TreeNode<CliqueOperation>> iterator = root.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<CliqueOperation> node = iterator.next();
            if (!(node.object() instanceof CliqueUnion)) {
                continue;
            }
            // At this point we assume that union nodes have two children
            Iterator<TreeNode<CliqueOperation>> childIterator = node.children().iterator();
            TreeNode<CliqueOperation> left = childIterator.next();
            TreeNode<CliqueOperation> right = childIterator.next();
            // Determine which colors intersect
            Set<Integer> intersection = new HashSet<>(colorMap(left).keySet());
            intersection.retainAll(colorMap(right).keySet());
            if (intersection.isEmpty()) {
                continue;
            }
            // Insert recoloring nodes above right child
            for (Integer color : intersection) {
                right.insertAbove(new CliqueRecoloring(color, color + shift));
            }
            // Insert recoloring nodes that revert the change above current union node
            for (Integer color : intersection) {
                node.insertAbove(new CliqueRecoloring(color + shift, color));
            }
        }
        // Normalize again
        colorSingletons(root);
    }

    // ----- Node Insertion --------------------------------------------------------------------------------------------

    /**
     * Creates an initial parse tree, containing leaf/singleton and union nodes.
     * @param derivation the derivation.
     * @return the root node of the parse tree.
     */
    private static TreeNode<CliqueOperation> createTreeWithLeavesAndUnion(CliqueDerivation derivation) {
        // At the root node we store cmp(T_t)
        int t = derivation.size() - 1;
        Set<Integer> rootCmp = derivation.cmp(t).iterator().next();
        CliqueOperation rootContents = new CliqueUnion(rootCmp, t);
        TreeNode<CliqueOperation> root = new TreeNode<>(rootContents);
        // Each component corresponds to either a union or a leaf node.
        for (int i = t - 1; i >= 0; i--) {
            for (Set<Integer> cmp : derivation.cmp(i)) {
                // Look for the union node in the current tree that contains the smallest superset of cmp.
                // Traverse in breadth first order, then the smallest superset will be the last superset.
                TreeNode<CliqueOperation> target = null;
                for (TreeNode<CliqueOperation> node : root) {
                    if (node.object() instanceof CliqueUnion) {
                        CliqueUnion contents = (CliqueUnion) node.object();
                        if (contents.getComponent().containsAll(cmp)) {
                            target = node;
                        }
                    }
                }
                if (target != null) {
                    int level = ((CliqueUnion) target.object()).getLevel();
                    // Add child to target. If target.level = 1, we add a leaf, otherwise a union node.
                    CliqueOperation contents;
                    if (level == 1) {
                        int vertex = cmp.iterator().next();
                        contents = new CliqueSingleton(vertex, 1);
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
        int width = derivation.getWidth();
        Iterator<TreeNode<CliqueOperation>> iterator = root.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<CliqueOperation> node = iterator.next();
            if (node.object() instanceof CliqueUnion) {
                // If the conditions are already fulfilled, we don't have to insert anything.
                if (fulfilsColorConditions(node, derivation)) {
                    Logger.debug(node.object() + " fulfils color conditions, skip");
                    continue;
                }
                // If not, we try to paint the nodes.
                boolean painted;
                // Optimization: it's possible that, if this is a union node at level 1, that all singletons just have
                // to be painted different colors. Attempt this first. If this satisfies all conditions, we can move on
                // to other nodes.
                painted = attemptFirstLevelRecolorings(node, derivation);
                if (painted) {
                    continue;
                }
                // Another optimization: often it's enough to only insert recoloring nodes above one of the children.
                // Attempt that and if that fails, move on to full brute forcing.
                painted = attemptEdgeRecolorings(node, derivation, width);
                if (painted) {
                    continue;
                }
                // As a last resort, brute force.
                bruteforceRecolorings(node, derivation, width);
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
        CliqueUnion nodeOperation = (CliqueUnion) node.object();
        if (nodeOperation.getLevel() != 1) {
            return false;
        }
        // Try to paint singletons different colors
        int i = 1;
        Set<TreeNode<CliqueOperation>> addedRecoloringNodes = new HashSet<>();
        Set<TreeNode<CliqueOperation>> children = new HashSet<>(node.children());
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
        Logger.debug(node.object() + " is being recolored using one-child brute force");
        StopWatch stopwatch = StopWatch.createStarted();
        Set<TreeNode<CliqueOperation>> children = new HashSet<>(node.children());
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
                        stopwatch.stop();
                        Logger.debug(node.object() + " had recoloring nodes inserted above one child, in time: " + stopwatch.formatTime());
                        return true;
                    } else {
                        for (TreeNode<CliqueOperation> addedNode : addedRecoloringNodes) {
                            addedNode.contract();
                        }
                    }
                }
            }
        }
        stopwatch.stop();
        Logger.debug(node.object() + " unsuccessful one-child brute force attempt took time: " + stopwatch.formatTime());
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
    @SuppressWarnings("UnusedReturnValue")
    private static boolean bruteforceRecolorings(TreeNode<CliqueOperation> node, CliqueDerivation derivation, int width) {
        Logger.debug(node.object() + " attempting full brute force");
        StopWatch stopwatch = StopWatch.createStarted();
        Iterator<List<TreeNode<CliqueOperation>>> childSubsetIterator = new SubsetIterator<>(node.children());
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
                    stopwatch.stop();
                    Logger.debug(node.object() + " has been repainted using full brute force, in time: " + stopwatch.formatTime());
                    return true;
                } else {
                    for (TreeNode<CliqueOperation> addedNode : addedRecoloringNodes) {
                        addedNode.contract();
                    }
                }
            }
        }
        stopwatch.stop();
        Logger.warn(node.object() + " has not been repainted using full brute force, in time: " + stopwatch.formatTime());
        return false;
    }

    /**
     * Adds an edge creation node for specified edge.
     * @param root the root of the tree.
     * @param edge the edge to create a node for.
     */
    private static void addEdgeCreationNode(TreeNode<CliqueOperation> root, Edge edge) {
        int u = edge.endpoints().get(0);
        int v = edge.endpoints().get(1);
        // Go through the nodes in breadth-first fashion, look at union nodes with component including both u, v; store
        // the node with the smallest level. Since we're traversing in breadth-first fashion, the last passing node will
        // be the one with the smallest level. In colorMap, we store a map from colors to vertices.
        int targetLevel = Integer.MAX_VALUE;
        TreeNode<CliqueOperation> target = null;
        Map<Integer, List<CliqueSingleton>> targetColorMap = null;
        for (TreeNode<CliqueOperation> node : root) {
            if (node.object() instanceof CliqueUnion) {
                CliqueUnion operation = ((CliqueUnion) node.object());
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
                // We don't have to insert edges(i -> j) if there is already a node edges(i -> j) or edges(j -> i) above.
                // Go up from target until recoloring/union node or null is reached. Look at edge creation nodes on the way.
                TreeNode<CliqueOperation> current = target;
                current = current.parent();
                while (current != null) {
                    if (current.object() instanceof CliqueRecoloring) {
                        break;
                    } else if (current.object() instanceof CliqueUnion) {
                        break;
                    } else if (current.object() instanceof CliqueEdgeCreation) {
                        CliqueEdgeCreation operation = (CliqueEdgeCreation) current.object();
                        if ((operation.getFrom() == colorU && operation.getTo() == colorV)
                            || (operation.getFrom() == colorV && operation.getTo() == colorU)) {
                            // A node that adds this edge to the graph is already in
                            return;
                        }
                    }
                    current = current.parent();
                }
                target.insertAbove(new CliqueEdgeCreation(colorU, colorV));
            }
        }
    }

    // ----- Normalization ---------------------------------------------------------------------------------------------

    /**
     * Normalizes the clique decomposition, i.e. transforms it into a tree without redundant nodes.
     * @param root the root of the tree.
     */
    private static void normalize(TreeNode<CliqueOperation> root) {
        // Firstly, we can reduce union paths. If there is a path = (... - union - union - ...), a union is redundant.
        reduceUnionPaths(root);
        // Singletons always have color set to 1, and if required have a recoloring node above. We can spare a few
        // recoloring nodes if we change the color of the singleton.
        colorSingletons(root);
        // Union nodes might have more than two children. In that case, transform the subtree into an equivalent one
        // that has two children.
        binarizeUnionNodes(root);
    }

    /**
     * Checks if there are union paths (... - union - union - ...) and reduces those.
     * @param root the root of the tree.
     */
    private static void reduceUnionPaths(TreeNode<CliqueOperation> root) {
        boolean reducable = true;
        while (reducable) {
            reducable = false;
            for (TreeNode<CliqueOperation> node : root) {
                if (!(node.object() instanceof CliqueUnion)) {
                    continue;
                }
                // If a union node only has one child, it is unnecessary
                if (node.children().size() == 1) {
                    node.contract();
                    reducable = true;
                }
                // To avoid errors due to concurrent iteration and mutation
                if (reducable) {
                    break;
                }
            }
        }
    }

    /**
     * Checks if there are recoloring nodes above singletons and moves the color information into singletons.
     * @param root the root of the tree.
     */
    private static void colorSingletons(TreeNode<CliqueOperation> root) {
        for (TreeNode<CliqueOperation> node : root) {
            if (!(node.object() instanceof CliqueSingleton)) {
                continue;
            }
            CliqueSingleton singleton = (CliqueSingleton) node.object();
            // Check if the parent of this singleton is a recoloring node. If so, color the singleton, and remove the
            // recoloring node.
            if (node.parent() != null) {
                TreeNode<CliqueOperation> parent = node.parent();
                if (!(parent.object() instanceof CliqueRecoloring)) {
                    continue;
                }
                CliqueRecoloring recoloring = (CliqueRecoloring) parent.object();
                singleton.setColor(recoloring.getTo());
                parent.contract();
            }
        }
    }

    /**
     * Checks if there are union nodes with more than two children and transforms the tree into an equivalent one where
     * union nodes have exactly two children.
     * @param root the root of the tree.
     */
    private static void binarizeUnionNodes(TreeNode<CliqueOperation> root) {
        // Go through union nodes that have > 2 children
        Iterator<TreeNode<CliqueOperation>> iterator = root.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<CliqueOperation> node = iterator.next();
            if (!(node.object() instanceof CliqueUnion) || node.children().size() <= 2) {
                continue;
            }
            // While this node has more than two children, proceed as follows. Pick two children, say a, b, and detach
            // them from this node. Create a new union node c, add a, b to its children. Then add c as a child to node.
            while (node.children().size() > 2) {
                Iterator<TreeNode<CliqueOperation>> childIterator = node.children().iterator();
                TreeNode<CliqueOperation> a = childIterator.next();
                TreeNode<CliqueOperation> b = childIterator.next();
                a.detach();
                b.detach();
                CliqueUnion operation = new CliqueUnion(Set.of(), -1);
                TreeNode<CliqueOperation> c = new TreeNode<>(operation);
                c.addChild(a);
                c.addChild(b);
                node.addChild(c);
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
        CliqueUnion nodeOperation = (CliqueUnion) node.object();
        Partition<Integer> grp = grp(node);
        return derivation.grp(nodeOperation.getLevel()).equivalenceClasses()
            .containsAll(grp.equivalenceClasses());
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
            if (subnode.object() instanceof CliqueSingleton) {
                leaves.add(subnode);
            }
        }
        // Color -> Subset of leaves with such color
        Map<Integer, List<CliqueSingleton>> map = new HashMap<>();
        // For each leaf, backtrack back to node, taking recoloring nodes on the way into account.
        for (TreeNode<CliqueOperation> leaf : leaves) {
            CliqueSingleton content = ((CliqueSingleton) leaf.object());
            int color = content.getColor();
            TreeNode<CliqueOperation> currentNode = leaf;
            while (currentNode != node) {
                if (currentNode.object() instanceof CliqueRecoloring) {
                    CliqueRecoloring op = ((CliqueRecoloring) currentNode.object());
                    if (color == op.getFrom()) {
                        color = op.getTo();
                    }
                }
                currentNode = currentNode.parent();
            }
            // Possible that currentNode is now a recoloring node too, don't forget to consider it
            if (currentNode.object() instanceof CliqueRecoloring) {
                CliqueRecoloring op = ((CliqueRecoloring) currentNode.object());
                if (color == op.getFrom()) {
                    color = op.getTo();
                }
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
