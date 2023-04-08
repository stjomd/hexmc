package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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
    public static BranchDecompositionNode of(Graph<Integer> graph) {
        BranchDecompositionNode bd = initialPartialDecomposition(graph);
        BranchDecompositionNode internalNode = getNodeWithDegreeLargerThan(3, bd);
        while (internalNode != null) {
            split(internalNode, graph);
            internalNode = getNodeWithDegreeLargerThan(3, bd);
        }
        return bd;
    }

    /**
     * Constructs an initial partial branch decomposition.
     * @param graph the graph.
     * @return a partial branch decomposition.
     */
    private static BranchDecompositionNode initialPartialDecomposition(Graph<Integer> graph) {
        // Create a star
        BranchDecompositionNode root = new BranchDecompositionNode();
        for (Edge<Integer> edge : graph.getEdges()) {
            root.addChild(new BranchDecompositionNode(edge));
        }
        // Initial separation
        // Separate nodes and store one part. Remove those nodes from the star.
        // Create another star with those nodes, and join the two stars.
        assert (root.getChildren().size() >= 4);
        Set<BranchDecompositionNode> initialSeparation = new HashSet<>();
        int i = 0;
        for (BranchDecompositionNode child : root.getChildren()) {
            if (i >= root.getChildren().size() / 2) {
                break;
            }
            initialSeparation.add(child);
            i++;
        }
        for (BranchDecompositionNode node : initialSeparation) {
            root.removeChild(node);
        }
        BranchDecompositionNode initialNewStar = new BranchDecompositionNode();
        for (BranchDecompositionNode node : initialSeparation) {
            initialNewStar.addChild(node);
        }
        root.addChild(initialNewStar);
        return root;
    }

    /**
     * Performs a split of the partial branch decomposition.
     * @param a an internal node of degree larger than 3.
     * @param graph the graph for which a branch decomposition is being computed.
     */
    private static void split(BranchDecompositionNode a, Graph<Integer> graph) {
        // By construction, a has exactly one neighbor that is not a leaf (deg > 1). We call the neighbor 'b'.
        BranchDecompositionNode b = null;
        for (BranchDecompositionNode child : a.getChildren()) {
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
        for (BranchDecompositionNode child : a.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getEdge().getEndpoints();
                separationA.add(endpoints.get(0));
                separationA.add(endpoints.get(1));
            }
        }
        for (BranchDecompositionNode child : b.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getEdge().getEndpoints();
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
        for (BranchDecompositionNode child : a.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getEdge().getEndpoints();
                associatedGraph.addEdge(endpoints.get(0), endpoints.get(1)); // creates new Edge instances
            }
        }
        // Now we need to find a separation (X,Y) of associatedGraph.
        List<Graph<Integer>> sep = separation(graph, associatedGraph, mid);
        List<Set<Edge<Integer>>> edgeSets = prepareEdgeSets(a, sep);
        // Now eX and eY are a partition of the edges in the associated graph.
        distributeLeaves(a, edgeSets.get(0), edgeSets.get(1));
    }

    /**
     * For a given internal node and a separation of the associated graph, returns the disjoint sets eX and eY of edges.
     * @param a an internal node of degree larger than 3.
     * @param separation a separation of the associated graph.
     * @return a list of two disjoint sets, eX and eY.
     */
    private static List<Set<Edge<Integer>>> prepareEdgeSets(BranchDecompositionNode a, List<Graph<Integer>> separation) {
        // X and Y are subgraphs which overlap in separation nodes. Both X and Y have Edge instances decoupled from
        // the associated graph. To simplify things further, we build sets E(X) and E(Y) that contain edge instances
        // from the associated graph.
        Set<Edge<Integer>> edgesInA = new HashSet<>();
        for (BranchDecompositionNode node : a.getChildren()) {
            if (node.getDegree() == 1) {
                edgesInA.add(node.getEdge());
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
        // eX and eY should, united, include all edges of the associated graph. If not, add the rest to eX.
        Set<Edge<Integer>> rest = new HashSet<>(edgesInA);
        rest.removeAll(eX);
        rest.removeAll(eX);
        eX.addAll(rest);
        // Make eX and eY disjoint
        Set<Edge<Integer>> intersect = new HashSet<>(eX);
        intersect.retainAll(eY);
        if (eX.size() > eY.size()) {
            eX.removeAll(intersect);
        } else {
            eY.removeAll(intersect);
        }
        // If one of eX or eY is empty, move one edge over.
        if (eX.isEmpty()) {
            Edge<Integer> edge = eY.iterator().next();
            eX.add(edge);
            eY.remove(edge);
        } else if (eY.isEmpty()) {
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
    private static void distributeLeaves(BranchDecompositionNode a, Set<Edge<Integer>> eX, Set<Edge<Integer>> eY) {
        // Store a's leaves.
        Set<BranchDecompositionNode> leaves = new HashSet<>();
        for (BranchDecompositionNode node : a.getChildren()) {
            if (node.getDegree() == 1) {
                leaves.add(node);
            }
        }
        if (eX.size() == 1) {
            // Right now, node a has leaves eX U eY. After the split, a has two children.
            // One child is a leaf x with the edge in eX. Other child is an internal node y with edges in eY.
            BranchDecompositionNode x = new BranchDecompositionNode(eX.iterator().next());
            BranchDecompositionNode y = new BranchDecompositionNode();
            // Add children to y.
            for (Edge<Integer> edge : eY) {
                y.addChild(new BranchDecompositionNode(edge));
            }
            // Remove a's leaves.
            for (BranchDecompositionNode leaf : leaves) {
                a.removeChild(leaf);
            }
            // Add x, y.
            a.addChild(x);
            a.addChild(y);
        } else {
            // Right now, node a has leaves eX U eY. After the split, a has two children.
            // Both are internal nodes x,y. Each has edges from the corresponding set.
            BranchDecompositionNode x = new BranchDecompositionNode();
            BranchDecompositionNode y = new BranchDecompositionNode();
            // Add children to the internal nodes.
            for (BranchDecompositionNode leaf : leaves) {
                Edge<Integer> edge = leaf.getEdge();
                if (eX.contains(edge)) {
                    x.addChild(new BranchDecompositionNode(edge));
                } else if (eY.contains(edge)) {
                    y.addChild(new BranchDecompositionNode(edge));
                }
            }
            // Remove a's leaves.
            for (BranchDecompositionNode leaf : leaves) {
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
                // TODO: fix to make setA and setB not overlap
                amount = Math.min(amount, size / 2);
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
                for (Edge<Integer> edge : minorEdgesToRemove) {
                    minor.removeEdge(edge);
                }
                // Now find the minimum vertex cut in minor intersecting all v_A,v_B-paths. We call the vertices in the
                // min vertex cut 'separation nodes'.
                Set<Integer> separationNodes = minimumVertexCut(minor, vA, vB);
                // Nodes that are both linking and separation nodes are labeled 'share nodes'.
                Set<Integer> shareNodes = new HashSet<>(separationNodes);
                shareNodes.retainAll(linkingNodes);
                // Linking nodes on one side of the cut (in one component) but not separation nodes are labeled 'side nodes'.
                // TODO: not sure of the formulation?
                Graph<Integer> separatedMinor = minor.duplicate();
                for (Integer node : separationNodes) {
                    separatedMinor.removeVertex(node);
                }
                Set<Integer> sideNodes = new HashSet<>(separatedMinor.getVertices());
                sideNodes.retainAll(linkingNodes);
                // (X, Y) is a separation of G_a. X, Y are subgraphs of G_a.
                // First remove separation nodes, which results in a graph with >1 components.
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
                // If there are > 2 components, merge the rest into the smallest of X or Y
                if (components.size() > 2) {
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
                if (work < owork) {
                    owork = work;
                    oplay = play;
                    oSepX = componentX;
                    oSepY = componentY;
                } else if (work == owork && play > oplay) {
                    oplay = play;
                    oSepX = componentX;
                    oSepY = componentY;
                }
            }
        }
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
    public static Set<Integer> minimumVertexCut(Graph<Integer> graph, Integer s, Integer t) {
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
    public static BranchDecompositionNode getNodeWithDegreeLargerThan(int degree, BranchDecompositionNode root) {
        Queue<BranchDecompositionNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            BranchDecompositionNode node = queue.remove();
            if (node.getDegree() > degree) {
                return node;
            }
            queue.addAll(node.getChildren());
        }
        return null;
    }

}
