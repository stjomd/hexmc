package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BranchDecompositionHeuristic {

    private final static int ALPHA_STEPS = 10;

    /**
     * Constructs an approximation of a branch decomposition according to a heuristic.
     * @param graph the graph to construct a branch decomposition of.
     * @return the root node of the branch decomposition.
     */
    public static TreeNode<Edge<Integer>> of(Graph<Integer> graph) {
        Logger.debug("Computing a branch decomposition using a heuristic");
        Logger.debug("Performing initial split");
        TreeNode<Edge<Integer>> bd = initialPartialDecomposition(graph);
        Logger.debug("Performing subsequent splits");
        TreeNode<Edge<Integer>> internalNode = getNodeWithDegreeLargerThan(3, bd);
        while (internalNode != null) {
            Logger.debug("\nObtained an internal node with degree > 3, performing split");
            split(internalNode, graph);
            internalNode = getNodeWithDegreeLargerThan(3, bd);
        }
        Logger.debug("Computed a branch decomposition using a heuristic");
        return bd;
    }

    /**
     * Constructs an initial partial branch decomposition.
     * @param graph the graph.
     * @return a partial branch decomposition, or null, if a branch decomposition doesn't exist.
     */
    private static TreeNode<Edge<Integer>> initialPartialDecomposition(Graph<Integer> graph) {
        // Special cases
        int size = graph.getEdges().size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            Edge<Integer> edge = graph.getEdges().iterator().next();
            return new TreeNode<>(edge);
        } else if (size == 2) {
            Iterator<Edge<Integer>> iterator = graph.getEdges().iterator();
            TreeNode<Edge<Integer>> node = new TreeNode<>(iterator.next());
            node.addChild(new TreeNode<>(iterator.next()));
        } else if (size == 3) {
            TreeNode<Edge<Integer>> node = new TreeNode<>();
            for (Edge<Integer> edge : graph.getEdges()) {
                node.addChild(new TreeNode<>(edge));
            }
            return node;
        }
        // Create a star
        TreeNode<Edge<Integer>> root = new TreeNode<>();
        for (Edge<Integer> edge : graph.getEdges()) {
            root.addChild(new TreeNode<>(edge));
        }
        // Initial separation
        // Separate nodes and store one part. Remove those nodes from the star.
        // Create another star with those nodes, and join the two stars.
        List<Graph<Integer>> separation = separation(graph, graph, graph.getVertices());
        List<Set<Edge<Integer>>> edgeSets = prepareEdgeSets(root, separation);
        if (edgeSets.get(0).size() < 2) {
            Logger.warn("E(A) has < 2 edges; moved one over from E(B)");
            Edge<Integer> edge = edgeSets.get(1).iterator().next();
            edgeSets.get(0).add(edge);
            edgeSets.get(1).remove(edge);
        } else if (edgeSets.get(1).size() < 2) {
            Logger.warn("E(B) has < 2 edges; moved one over from E(A)");
            Edge<Integer> edge = edgeSets.get(0).iterator().next();
            edgeSets.get(1).add(edge);
            edgeSets.get(0).remove(edge);
        }
        // Create a new star with children corresponding to edges in E(B); store children in a set to remove them later
        // from root
        TreeNode<Edge<Integer>> newStar = new TreeNode<>();
        Set<TreeNode<Edge<Integer>>> newChildren = new HashSet<>();
        for (TreeNode<Edge<Integer>> child : root.getChildren()) {
            if (edgeSets.get(1).contains(child.getObject())) {
                newStar.addChild(new TreeNode<>(child.getObject()));
                newChildren.add(child);
            }
        }
        // Now remove nodes with edges in E(B) from root. As such root will only contain edges in E(A)
        for (TreeNode<Edge<Integer>> node : newChildren) {
            root.removeChild(node);
        }
        // At this point we have to stars, root with E(A) and newStar with E(B). Join the two.
        root.addChild(newStar);
        return root;
    }

    /**
     * Performs a split of the partial branch decomposition.
     * @param a an internal node of degree larger than 3.
     * @param graph the graph for which a branch decomposition is being computed.
     */
    private static void split(TreeNode<Edge<Integer>> a, Graph<Integer> graph) {
        // By construction, a has exactly one neighbor that is not a leaf (deg > 1). We call the neighbor 'b'.
        TreeNode<Edge<Integer>> b = null;
        for (TreeNode<Edge<Integer>> child : a.getChildren()) {
            if (child.getDegree() > 1) {
                b = child;
            }
        }
        // If b is not among children, then it must be the parent of a.
        if (b == null) {
            b = a.getParent();
        }
        // The edge e is <a, b>. e separates the graph's edges. One separation is a's children, the other is b's,
        // evtl. minus a and b themselves. We need the endpoints of the edges in both partitions.
        Set<Integer> separationA = new HashSet<>(), separationB = new HashSet<>();
        for (TreeNode<Edge<Integer>> child : a.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getObject().getEndpoints();
                separationA.add(endpoints.get(0));
                separationA.add(endpoints.get(1));
            }
        }
        for (TreeNode<Edge<Integer>> child : b.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getObject().getEndpoints();
                separationB.add(endpoints.get(0));
                separationB.add(endpoints.get(1));
            }
        }
        // The set of load vertices mid(e) is the intersection of separationA and separationB.
        Set<Integer> mid = new HashSet<>(separationA);
        mid.retainAll(separationB);
        // associatedGraph (G_a). We obtain it by selecting the children of a that are leaves, and adding
        // their edges to the associatedGraph.
        Graph<Integer> associatedGraph = new Graph<>();
        for (TreeNode<Edge<Integer>> child : a.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getObject().getEndpoints();
                associatedGraph.addEdge(endpoints.get(0), endpoints.get(1)); // creates new Edge instances
            }
        }
        Logger.debug("Associated graph: " + associatedGraph);
        // Now we need to find a separation (X,Y) of associatedGraph.
        List<Graph<Integer>> sep = separation(graph, associatedGraph, mid);
        Logger.debug("Best X = " + sep.get(0));
        Logger.debug("Best Y = " + sep.get(1));
        List<Set<Edge<Integer>>> edgeSets = prepareEdgeSets(a, sep);
        Logger.debug("E(X) = " + edgeSets.get(0));
        Logger.debug("E(Y) = " + edgeSets.get(1));
        // Now eX and eY are a partition of the edges in the associated graph.
        distributeLeaves(a, edgeSets.get(0), edgeSets.get(1));
    }

    /**
     * For a given internal node and a separation of the associated graph, returns the disjoint sets eX and eY of edges.
     * @param a an internal node of degree larger than 3.
     * @param separation a separation of the associated graph.
     * @return a list of two disjoint sets, eX and eY.
     */
    private static List<Set<Edge<Integer>>> prepareEdgeSets(TreeNode<Edge<Integer>> a, List<Graph<Integer>> separation) {
        // X and Y are subgraphs which overlap in separation nodes.
        Set<Edge<Integer>> edgesInA = new HashSet<>();
        for (TreeNode<Edge<Integer>> node : a.getChildren()) {
            if (node.getDegree() == 1) {
                edgesInA.add(node.getObject());
            }
        }
        Set<Edge<Integer>> eX = new HashSet<>();
        Set<Edge<Integer>> eY = new HashSet<>();
        for (Edge<Integer> edge : edgesInA) {
            Integer u = edge.getEndpoints().get(0);
            Integer v = edge.getEndpoints().get(1);
            if (separation.get(0).hasEdgeWithEndpoints(u, v)) {
                eX.add(edge);
            } else if (separation.get(1).hasEdgeWithEndpoints(u, v)) {
                eY.add(edge);
            }
        }
        // If one of eX or eY is empty, move one edge over.
        if (eX.isEmpty()) {
            Logger.warn("E(X) was empty; moved one edge over from E(Y)");
            Edge<Integer> edge = eY.iterator().next();
            eX.add(edge);
            eY.remove(edge);
        } else if (eY.isEmpty()) {
            Logger.warn("E(Y) was empty; moved one edge over from E(X)");
            Edge<Integer> edge = eX.iterator().next();
            eY.add(edge);
            eX.remove(edge);
        }
        // Let |eX| <= |eY|.
        if (eX.size() > eY.size()) {
            Set<Edge<Integer>> temp = eX;
            //noinspection ReassignedVariable,SuspiciousNameCombination
            eX = eY;
            eY = temp;
        }
        return List.of(eX, eY);
    }

    /**
     * For a specified internal node, distributes its leaves to new internal nodes.
     * @param a an internal node of degree larger than 3.
     * @param eX a set of edges.
     * @param eY a set of edges.
     */
    private static void distributeLeaves(TreeNode<Edge<Integer>> a, Set<Edge<Integer>> eX, Set<Edge<Integer>> eY) {
        // Store a's leaves.
        Set<TreeNode<Edge<Integer>>> leaves = new HashSet<>();
        for (TreeNode<Edge<Integer>> node : a.getChildren()) {
            if (node.getDegree() == 1) {
                leaves.add(node);
            }
        }
        if (eX.size() == 1) {
            // Right now, node a has leaves eX U eY. After the split, a has two children.
            // One child is a leaf x with the edge in eX. Other child is an internal node y with edges in eY.
            TreeNode<Edge<Integer>> x = new TreeNode<>(eX.iterator().next());
            TreeNode<Edge<Integer>> y = new TreeNode<>();
            // Add children to y.
            for (Edge<Integer> edge : eY) {
                y.addChild(new TreeNode<>(edge));
            }
            // Remove a's leaves.
            for (TreeNode<Edge<Integer>> leaf : leaves) {
                a.removeChild(leaf);
            }
            // Add x, y.
            a.addChild(x);
            a.addChild(y);
        } else {
            // Right now, node a has leaves eX U eY. After the split, a has two children.
            // Both are internal nodes x,y. Each has edges from the corresponding set.
            TreeNode<Edge<Integer>> x = new TreeNode<>();
            TreeNode<Edge<Integer>> y = new TreeNode<>();
            // Add children to the internal nodes.
            for (TreeNode<Edge<Integer>> leaf : leaves) {
                Edge<Integer> edge = leaf.getObject();
                if (eX.contains(edge)) {
                    x.addChild(new TreeNode<>(edge));
                } else if (eY.contains(edge)) {
                    y.addChild(new TreeNode<>(edge));
                }
            }
            // Remove a's leaves.
            for (TreeNode<Edge<Integer>> leaf : leaves) {
                a.removeChild(leaf);
            }
            // Add x, y.
            a.addChild(x);
            a.addChild(y);
        }
    }

    /**
     * Computes a separation (X,Y) of the associated graph.
     * @param graph the graph G.
     * @param associatedGraph the graph G_a associated with the leaves of node a.
     * @param mid the load vertices set of e.
     * @return A separation (list of two elements), (X, Y), where |X| <= |Y|.
     */
    private static List<Graph<Integer>> separation(Graph<Integer> graph, Graph<Integer> associatedGraph, Set<Integer> mid) {
        // Linking nodes are vertices of associatedGraph that are also in mid.
        Set<Integer> linkingNodes = new HashSet<>(associatedGraph.getVertices());
        linkingNodes.retainAll(mid);
        // Source nodes are vertices that are in associatedGraph with eccentricity = diameter, as well as linking nodes.
        Set<Integer> sourceNodes = new HashSet<>(linkingNodes);
        int diameter = associatedGraph.diameter();
        for (Integer vertex : associatedGraph.getVertices()) {
            if (associatedGraph.eccentricity(vertex) == diameter) {
                sourceNodes.add(vertex);
            }
        }
        // Iterate over source nodes and alphas, determine work/play pairs.
        int owork = Integer.MAX_VALUE;
        int oplay = Integer.MIN_VALUE;
        Graph<Integer> oSepX = null;
        Graph<Integer> oSepY = null;
        for (Integer chosenSourceNode : sourceNodes) {
            for (double alpha = 0.01; alpha < 1; alpha += 1.0/ALPHA_STEPS) {
                // Choose a source node. Sort vertices of associatedGraph in non-decreasing order acc. to their distance to it.
                List<Integer> sortedVertices = associatedGraph.getVertices().stream()
                    .sorted(Comparator.comparing(v -> associatedGraph.distance(v, chosenSourceNode)))
                    .collect(Collectors.toList());
                // Create setA, setB of vertices. Each has 'amount' vertices.
                int size = sortedVertices.size();
                int amount = (int) (alpha * (size - 1)) + 1;
                // Fix to make setA and setB not overlap
                boolean preventedOverlapping = false;
                if (amount > size / 2) {
                    preventedOverlapping = true;
                    amount = Math.min(amount, size / 2);
                }
                // Add the first 'amount' vertices to setA, the last 'amount' vertices to setB
                Set<Integer> setA = new HashSet<>(), setB = new HashSet<>();
                for (int i = 0; i < amount; i++) {
                    setA.add(sortedVertices.get(i));
                    setB.add(sortedVertices.get(size - 1 - i));
                }
                // Compute the minor of G with setA identified to v_A, setB identified to v_B.
                Graph<Integer> minor = minor(graph, setA, setB);
                // v_A, v_B are named in 'minor' after some vertex from setA or setB respectively.
                Integer vA = null, vB = null;
                for (Integer v : minor.getVertices()) {
                    if (setA.contains(v)) {
                        vA = v;
                    } else if (setB.contains(v)) {
                        vB = v;
                    }
                    if (vA != null && vB != null) {
                        break;
                    }
                }
                // Minor might have edges between vA, vB. Which makes the minimum vertex cut undefined. => delete
                Set<Edge<Integer>> minorEdgesToRemove = new HashSet<>();
                for (Edge<Integer> edge : minor.getEdges()) {
                    if (edge.getEndpoints().contains(vA) && edge.getEndpoints().contains(vB)) {
                        minorEdgesToRemove.add(edge);
                    }
                }
                if (!minorEdgesToRemove.isEmpty()) {
                    for (Edge<Integer> edge : minorEdgesToRemove) {
                        minor.removeEdge(edge);
                    }
                }
                // Now find the minimum vertex cut in minor intersecting all v_A,v_B-paths. We call the vertices in the
                // min vertex cut 'separation nodes'.
                Set<Integer> separationNodes = minimumVertexCut(minor, vA, vB);
                // Nodes that are both linking and separation nodes are labeled 'share nodes'.
                Set<Integer> shareNodes = new HashSet<>(separationNodes);
                shareNodes.retainAll(linkingNodes);
                // (X, Y) is a separation of G_a. X, Y are subgraphs of G_a.
                // First remove separation nodes.
                Graph<Integer> separatedGraph = associatedGraph.duplicate();
                for (Integer vertex : separationNodes) {
                    separatedGraph.removeVertex(vertex);
                }
                List<Graph<Integer>> components = separatedGraph.components();
                // Determine the components X and Y.
                Graph<Integer> componentX = null; // component that contains vA
                Graph<Integer> componentY = null; // component that contains vB
                for (Graph<Integer> cmp : components) {
                    if (cmp.getVertices().contains(vA)) {
                        componentX = cmp;
                    } else if (cmp.getVertices().contains(vB)) {
                        componentY = cmp;
                    }
                }
                // Bug fix for case when an edge gets "lost". Suppose separatedGraph has 2 components, and X has both
                // vA, vB and Y has other vertices/edges. Then Y is null and the component without vA, vB is forgotten.
                if (components.size() == 2 && componentY == null) {
                    for (Graph<Integer> cmp : components) {
                        if (!cmp.getVertices().contains(vA) && !cmp.getVertices().contains(vB)) {
                            componentY = cmp;
                        }
                    }
                }
                // If one is null, create an empty one.
                if (componentX == null) {
                    componentX = new Graph<>();
                    for (Integer sepNode : separationNodes) {
                        componentX.addVertex(sepNode);
                    }
                }
                if (componentY == null) {
                    componentY = new Graph<>();
                    for (Integer sepNode : separationNodes) {
                        componentY.addVertex(sepNode);
                    }
                }
                // If there are > 2 components, merge the rest into the smallest of X or Y
                boolean mergedComponents = false;
                if (components.size() > 2) {
                    mergedComponents = true;
                    Graph<Integer> smallest = (componentX.getVertices().size() < componentY.getVertices().size())
                        ? componentX : componentY;
                    for (Graph<Integer> cmp : components) {
                        if (cmp != componentX && cmp != componentY) {
                            // Add vertices...
                            for (Integer vertex : cmp.getVertices()) {
                                smallest.addVertex(vertex);
                            }
                            // and edges.
                            for (Edge<Integer> edge : cmp.getEdges()) {
                                smallest.addEdge(edge);
                            }
                        }
                    }
                }
                // Add the separation nodes to the two components, as well as corresponding edges
                for (Integer separationNode : separationNodes) {
                    componentX.addVertex(separationNode);
                    componentY.addVertex(separationNode);
                }
                for (Edge<Integer> edge : associatedGraph.getEdges()) {
                    Integer u = edge.getEndpoints().get(0);
                    Integer v = edge.getEndpoints().get(1);
                    // Add edges between separation nodes
                    if (separationNodes.contains(u) && separationNodes.contains(v)) {
                        componentX.addEdge(u, v);
                        componentY.addEdge(v, u);
                        continue;
                    }
                    // Add edges that as one endpoint have a separation node.
                    if (separationNodes.contains(u)) {
                        if (componentX.getVertices().contains(v)) {
                            componentX.addEdge(u, v);
                        } else if (componentY.getVertices().contains(v)) {
                            componentY.addEdge(u, v);
                        }
                    } else if (separationNodes.contains(v)) {
                        if (componentX.getVertices().contains(v)) {
                            componentX.addEdge(u, v);
                        } else if (componentY.getVertices().contains(v)) {
                            componentY.addEdge(u, v);
                        }
                    }
                }
                // Linking nodes on one side of the cut but not separation nodes are labeled 'side nodes'.
                Set<Integer> sideNodes = new HashSet<>(componentX.getVertices());
                sideNodes.retainAll(linkingNodes);
                // Define work and play values
                int work = Math.max(
                    sideNodes.size() + separationNodes.size(),
                    linkingNodes.size() - sideNodes.size() - shareNodes.size() + separationNodes.size()
                );
                int play = Math.min(
                    sideNodes.size() + separationNodes.size(),
                    linkingNodes.size() - sideNodes.size() - shareNodes.size() + separationNodes.size()
                );
                // Store optimal pair
                boolean output = false;
                if (work < owork) {
                    output = true;
                    owork = work;
                    oplay = play;
                    oSepX = componentX;
                    oSepY = componentY;
                } else if (work == owork && play > oplay) {
                    output = true;
                    oplay = play;
                    oSepX = componentX;
                    oSepY = componentY;
                }
                // Output debug information for the new best pair
                if (output) {
                    Logger.debug("\nNew best pair (work = " + work + ", play = " + play + ")");
                    Logger.debug("Source node = " + chosenSourceNode + ", alpha = " + alpha);
                    if (preventedOverlapping) {
                        Logger.warn("Prevented A, B from overlapping");
                    }
                    Logger.debug("A = " + setA + ", B = " + setB);
                    if (!minorEdgesToRemove.isEmpty()) {
                        Logger.warn("Removed vA-vB edges in minor for vA = " + vA + ", vB = " + vB);
                    }
                    Logger.debug("Minor = " + minor);
                    Logger.debug("vA = " + vA + ", vB = " + vB);
                    Logger.debug("Minimum vertex cut = " + separationNodes);
                    if (mergedComponents) {
                        Logger.warn("Separated graph has more than 2 components. Merged into the smallest of X and Y.");
                    }
                    Logger.debug("X = " + componentX);
                    Logger.debug("Y = " + componentY);
                }
            }
        }
        Logger.debug("\nFinished determining the best (work, play) pair for this split");
        // Return a list where |oSepX| <= |oSepY|.
        if (oSepX.getEdges().size() <= oSepY.getEdges().size()) {
            return List.of(oSepX, oSepY);
        } else {
            return List.of(oSepY, oSepX);
        }
    }

    /**
     * Computes a minor of the specified graph while contracting edges in such a way that all vertices in setA
     * end up merged into one vertex, and all vertices in setB end up merged into another vertex.
     * @param graph the graph.
     * @param setA a set of vertices.
     * @param setB a set of vertices.
     * @return the minor of the graph, with both partitions identified to some two vertices.
     */
    private static Graph<Integer> minor(Graph<Integer> graph, Set<Integer> setA, Set<Integer> setB) {
        Graph<Integer> minor = graph.duplicate();
        boolean minorable = true;
        while (minorable) {
            minorable = false;
            // Among edges, look for possibilities to contract edges/merge vertices from one partition together.
            Set<Edge<Integer>> edges = minor.getEdges();
            for (Edge<Integer> edge : edges) {
                List<Integer> endpoints = edge.getEndpoints();
                // Avoid loops
                if (endpoints.get(0).equals(endpoints.get(1))) {
                    continue;
                }
                boolean hasCandidates = setA.containsAll(endpoints) || setB.containsAll(endpoints);
                if (hasCandidates) {
                    minor.contractEdge(edge);
                    minorable = true;
                    break;
                }
            }
        }
        return minor;
    }

    /**
     * Computes a minimum vertex cut in the graph, i.e. a set of vertices that, when removed from the graph, decouples
     * it into (at least) two components. If there is an edge between s and t, returns an empty vertex cut.
     * @param graph the graph.
     * @param s a vertex.
     * @param t a vertex.
     * @return the minimum vertex s-t-cut.
     */
    private static Set<Integer> minimumVertexCut(Graph<Integer> graph, Integer s, Integer t) {
        if (graph.hasEdgeWithEndpoints(s, t)) {
            return Set.of();
        }
        // FIXME: brute force at this point.
        List<Integer> vertices = new ArrayList<>(graph.getVertices());
        // s-t cut: may not remove s or t
        vertices.remove(s);
        vertices.remove(t);
        // inclusion[j] = true <=> vertices[j] is in the candidate for vertex cut
        List<Boolean> inclusion = new ArrayList<>();
        for (int j = 0; j < vertices.size(); j++) {
            inclusion.add(true);
        }
        // Iterate over all subsets of vertices \ {s,t}, check if subset is a vertex cut. Store the minimal one.
        Set<Integer> vertexCut = new HashSet<>();
        int minVertexCutSize = Integer.MAX_VALUE;
        int inclusionSum = Integer.MAX_VALUE;
        while (inclusionSum > 0) {
            // Get subset
            Graph<Integer> graphWithRemovedVertices = graph.duplicate();
            Set<Integer> includedVertices = getSubset(vertices, inclusion);
            for (Integer vertex : includedVertices) {
                graphWithRemovedVertices.removeVertex(vertex);
            }
            // Check if the subset is a vertex cut.
            List<Graph<Integer>> components = graphWithRemovedVertices.components();
            if (components.size() > 1) {
                // s, t must lie in different components.
                Graph<Integer> componentWithS = null, componentWithT = null;
                for (Graph<Integer> component : components) {
                    if (component.getVertices().contains(s) && !component.getVertices().contains(t)) {
                        componentWithS = component;
                    } else if (component.getVertices().contains(t) && !component.getVertices().contains(s)) {
                        componentWithT = component;
                    }
                }
                if (componentWithS != null && componentWithT != null && componentWithS != componentWithT) {
                    // Found a vertex cut
                    if (minVertexCutSize > includedVertices.size()) {
                        minVertexCutSize = includedVertices.size();
                        vertexCut = includedVertices;
                        if (minVertexCutSize == 1) {
                            break;
                        }
                    }
                }
            }
            // Count the amount of falses for the loop condition. When sum is 0, we went through all subsets
            inclusionSum = 0;
            for (Boolean bool : inclusion) {
                if (bool.equals(false)) {
                    inclusionSum++;
                }
            }
        }
        return vertexCut;
    }

    /**
     * Returns a subset of the specified list, and increments the inclusion list.
     * @param list the list to obtain a subset of.
     * @param inclusion an inclusion list, a list of booleans, which indicates which elements go into the subset.
     * @return the subset of list.
     */
    private static Set<Integer> getSubset(List<Integer> list, List<Boolean> inclusion) {
        Set<Integer> subset = new HashSet<>();
        // Update inclusion
        int j = inclusion.size() - 1;
        while (j >= 0 && inclusion.get(j).equals(false)) {
            inclusion.set(j, true);
            j--;
        }
        if (j >= 0) {
            inclusion.set(j, false);
        }
        // Build subset
        for (int k = 0; k < list.size(); k++) {
            if (inclusion.get(k).equals(true)) {
                subset.add(list.get(k));
            }
        }
        return subset;
    }

    /**
     * Finds a node with degree larger than specified degree.
     * @param degree the degree.
     * @param root the root node.
     * @return the node with degree larger than specified degree.
     */
    private static TreeNode<Edge<Integer>> getNodeWithDegreeLargerThan(int degree, TreeNode<Edge<Integer>> root) {
        Queue<TreeNode<Edge<Integer>>> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TreeNode<Edge<Integer>> node = queue.remove();
            if (node.getDegree() > degree) {
                return node;
            }
            queue.addAll(node.getChildren());
        }
        return null;
    }

}
