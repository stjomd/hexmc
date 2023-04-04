package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BranchDecompositionFactory {

    private final static int ALPHA_STEPS = 10;

    /**
     * Constructs an approximation of a branch decomposition according to a heuristic.
     * @param graph the graph to construct a branch decomposition of.
     * @return the root node of the branch decomposition.
     */
    public static BranchDecompositionNode heuristic(Graph<Integer> graph) {
        BranchDecompositionNode bd = heuristicInitialSeparation(graph);
        BranchDecompositionNode internalNode = getNodeWithDegreeLargerThan(3, bd);
        while (internalNode != null) {
            split(internalNode, graph);
            internalNode = getNodeWithDegreeLargerThan(3, bd);
        }
        return bd;
    }

    // Constructs T_{i+1} from T_i wer
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
        assert (b != null && b.getDegree() > 1);
        // The edge e is <a, b>. e partitions the graph's edges. One partition is a's children, the other is b's,
        // evtl. minus a and b themselves. We need the endpoints of the edges in both partitions.
        Set<Integer> partA = new HashSet<>(), partB = new HashSet<>();
        for (BranchDecompositionNode child : a.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getEdge().getEndpoints();
                partA.add(endpoints.get(0));
                partA.add(endpoints.get(1));
            }
        }
        for (BranchDecompositionNode child : b.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getEdge().getEndpoints();
                partB.add(endpoints.get(0));
                partB.add(endpoints.get(1));
            }
        }
        // The set of linking nodes mid(e) is the intersection of partA and partB.
        Set<Integer> mid = new HashSet<>(partA);
        mid.retainAll(partB);
        // associatedGraph is G_a in the paper. We obtain it by selecting the children of a that are leaves, and adding
        // their edges to the associatedGraph.
        Graph<Integer> associatedGraph = new Graph<>();
        for (BranchDecompositionNode child : a.getChildren()) {
            if (child.getDegree() == 1) {
                List<Integer> endpoints = child.getEdge().getEndpoints();
                associatedGraph.addEdge(endpoints.get(0), endpoints.get(1));
            }
        }
        // Now we need to find a separation (X,Y) of associatedGraph.
        List<Set<Integer>> sep = separation(graph, associatedGraph, mid);
        // Get the corresponding edges, E(X) and E(Y).
        Set<Edge<Integer>> eX = new HashSet<>();
        Set<Edge<Integer>> eY = new HashSet<>();
        for (Integer x : sep.get(0)) {
            for (Edge<Integer> edge : graph.getEdges()) {
                if (edge.getEndpoints().contains(x)) {
                    eX.add(edge);
                }
            }
        }
        for (Integer y : sep.get(1)) {
            for (Edge<Integer> edge : graph.getEdges()) {
                if (edge.getEndpoints().contains(y)) {
                    eY.add(edge);
                }
            }
        }
        // Split
        if (sep.get(0).size() > 1) { // if |X| > 1
            // Create new internal nodes x, y.
            // x has leaves corresponding to eX, y has leaves corresponding to eY.
            BranchDecompositionNode x = new BranchDecompositionNode();
            BranchDecompositionNode y = new BranchDecompositionNode();
            // Store leaves of a, remove a's children except b (other internal node), add the leaves to x/y.
            Set<BranchDecompositionNode> leaves = new HashSet<>();
            for (BranchDecompositionNode node : a.getChildren()) {
                if (node.getDegree() == 1) {
                    BranchDecompositionNode decoupledLeaf = new BranchDecompositionNode(node.getEdge());
                    leaves.add(decoupledLeaf);
                }
            }
            // Remove children except b
            Set<BranchDecompositionNode> setOfB = new HashSet<>();
            setOfB.add(b);
            a.getChildren().retainAll(setOfB);
            // Add leaves to x/y
            for (BranchDecompositionNode leaf : leaves) {
                if (eX.contains(leaf.getEdge())) {
                    x.addChild(leaf);
                } else if (eY.contains(leaf.getEdge())) {
                    y.addChild(leaf);
                }
            }
            // Make x,y children of a
            a.addChild(x);
            a.addChild(y);
        } else { // |X| = 1
            assert (eX.size() == 1);
            // Create new internal node y that has leaves corresponding to eY. a only keeps one leaf corr. to eX.
            BranchDecompositionNode y = new BranchDecompositionNode();
            // Store leaves of a
            Set<BranchDecompositionNode> leaves = new HashSet<>();
            for (BranchDecompositionNode node : a.getChildren()) {
                if (node.getDegree() == 1) {
                    BranchDecompositionNode decoupledLeaf = new BranchDecompositionNode(node.getEdge());
                    leaves.add(decoupledLeaf);
                }
            }
            // Remove children except b
            Set<BranchDecompositionNode> setOfB = new HashSet<>();
            setOfB.add(b);
            a.getChildren().retainAll(setOfB);
            // Re-add the leaf corresponding to eX
            BranchDecompositionNode xLeaf = null;
            for (BranchDecompositionNode leaf : leaves) {
                if (eX.contains(leaf.getEdge())) {
                    xLeaf = leaf;
                }
            }
            assert (xLeaf != null);
            a.addChild(xLeaf);
            leaves.remove(xLeaf);
            // Add the rest of the leaves to y
            for (BranchDecompositionNode leaf : leaves) {
                y.addChild(leaf);
            }
            // Add y to the children of a
            a.addChild(y);
        }
    }

    private static List<Set<Integer>> separation(Graph<Integer> graph, Graph<Integer> associatedGraph, Set<Integer> mid) {
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
        Set<Integer> oSepX = new HashSet<>();
        Set<Integer> oSepY = new HashSet<>();
        for (Integer chosenSourceNode : sourceNodes) {
            for (double alpha = 0.01; alpha < 1; alpha += 1.0/ALPHA_STEPS) {
                // Choose a source node. Sort vertices of associatedGraph in non-decreasing order acc. to their distance to it.
                List<Integer> sortedVertices = associatedGraph.getVertices().stream()
                    .sorted(Comparator.comparing(v -> associatedGraph.distance(v, chosenSourceNode)))
                    .collect(Collectors.toList());
                // Partition the sorted vertices of associatedGraph into partA, partB using the cutoff index.
                int cutoff = (int) (alpha * (sortedVertices.size() - 1)) + 1;
                Set<Integer> partA = new HashSet<>(), partB = new HashSet<>();
                int i = 0;
                for (Integer vertex : sortedVertices) {
                    if (i < cutoff) {
                        partA.add(vertex);
                    } else {
                        partB.add(vertex);
                    }
                    i++;
                }
                // Compute the minor of G with partA identified to v_A, partB identified to v_B.
                Graph<Integer> minor = minor(graph, partA, partB);
                // v_A, v_B are named in 'minor' after some vertex from partA or partB respectively.
                Integer vA = null, vB = null;
                for (Integer v : minor.getVertices()) {
                    if (partA.contains(v)) {
                        vA = v;
                    } else if (partB.contains(v)) {
                        vB = v;
                    }
                    if (vA != null && vB != null) {
                        break;
                    }
                }
                assert (vA != null && vB != null);
                // Now find the minimum vertex cut intersecting all v_A,v_B-paths. We call the vertices in the min
                // vertex cut 'separation nodes'.
                Set<Integer> separationNodes = minimumVertexCut(minor, vA, vB);
                // Nodes that are both linking and separation nodes are labeled 'share nodes'.
                Set<Integer> shareNodes = new HashSet<>(separationNodes);
                shareNodes.retainAll(linkingNodes);
                // Linking nodes on one side of the cut (in one component) but not separation nodes are labeled 'side nodes'.
                // TODO: not sure of the formulation?
                Graph<Integer> separatedGraph = minor.duplicate();
                for (Integer node : separationNodes) {
                    separatedGraph.removeVertex(node);
                }
                Set<Integer> sideNodes = new HashSet<>(separatedGraph.getVertices());
                sideNodes.retainAll(linkingNodes);
                // Compute separation (X,Y). partA is a subset of X, partB is a subset of Y. Intersection of X and Y is
                // the set of separation nodes. Vertices in vA,vB-paths before separation nodes are in X, after in Y.
                Set<Integer> sepX = new HashSet<>(partA);
                Set<Integer> sepY = new HashSet<>(partB);
                sepX.addAll(separationNodes);
                sepY.addAll(separationNodes);
                for (List<Integer> path : minor.allPaths(vA, vB)) {
                    // If the path starts in partA, we add all vertices in the path until the separation node to sepX...
                    boolean startsInPartA = partA.contains(path.get(0));
                    int j = 0;
                    while (!separationNodes.contains(path.get(j))) {
                        if (startsInPartA) {
                            sepX.add(path.get(j));
                        } else {
                            sepY.add(path.get(j));
                        }
                        j++;
                    }
                    // And all vertices after to sepY.
                    while (j < path.size()) {
                        if (startsInPartA) {
                            sepY.add(path.get(j));
                        } else {
                            sepX.add(path.get(j));
                        }
                        j++;
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
                    oSepX = sepX;
                    oSepY = sepY;
                } else if (work == owork && play > oplay) {
                    oplay = play;
                    oSepX = sepX;
                    oSepY = sepY;
                }
            }
        }
        // Return a list where |oSepX| <= |oSepY|.
        if (oSepX.size() <= oSepY.size()) {
            return List.of(oSepX, oSepY);
        } else {
            return List.of(oSepY, oSepX);
        }
    }

    // minor H of G with A,B identified to vA,vB
    private static Graph<Integer> minor(Graph<Integer> graph, Set<Integer> partitionA, Set<Integer> partitionB) {
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
                boolean hasCandidates = (partitionA.contains(endpoints.get(0)) && partitionA.contains(endpoints.get(1)))
                    || (partitionB.contains(endpoints.get(0)) && partitionB.contains(endpoints.get(1)));
                if (hasCandidates) {
                    minor.contractEdge(edge);
                    minorable = true;
                    break;
                }
            }
        }
        return minor;
    }

    // finds s-t cut
    private static Set<Integer> minimumVertexCut(Graph<Integer> graph, Integer s, Integer t) {
        // FIXME: doesn't seem to make sense if minor only has 2 vertices. Since any cut results in 1 comp?
        // FIXME: brute force at this point.
        List<Integer> vertices = new ArrayList<>(graph.getVertices());
        List<Boolean> inclusion = new ArrayList<>();
        for (int j = 0; j < vertices.size(); j++) {
            inclusion.add(true);
        }
        // inclusion defines the subset of vertices. If no path between vA,vB exists, we found a vertex cut.
        // Keep looking for the one of the smallest size.
        Set<Integer> vertexCut = new HashSet<>();
        int minVertexCutSize = Integer.MAX_VALUE;
        int inclusionSum = Integer.MAX_VALUE;
        while (inclusionSum > 0) {
            // Update the inclusion list
            int j = inclusion.size() - 1;
            while (j >= 0 && inclusion.get(j).equals(false)) {
                inclusion.set(j, true);
                j--;
            }
            if (j >= 0) {
                inclusion.set(j, false);
            }
            // Perform
            Graph<Integer> copy = graph.duplicate();
            Set<Integer> includedVertices = new HashSet<>();
            for (int k = 0; k < vertices.size(); k++) {
                if (inclusion.get(k).equals(true)) {
                    includedVertices.add(vertices.get(k));
                }
            }
            for (Integer vertex : includedVertices) {
                copy.removeVertex(vertex);
            }
            if (copy.path(s, t) == null) {
                // Found a vertex cut
                if (minVertexCutSize > includedVertices.size()) {
                    minVertexCutSize = includedVertices.size();
                    vertexCut = includedVertices;
                }
            }
            // Count the amount of falses
            inclusionSum = 0;
            for (Boolean bool : inclusion) {
                if (bool.equals(false)) {
                    inclusionSum++;
                }
            }
        }
        return vertexCut;
    }

    private static BranchDecompositionNode heuristicInitialSeparation(Graph<Integer> graph) {
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

    private static BranchDecompositionNode getNodeWithDegreeLargerThan(int degree, BranchDecompositionNode root) {
        if (root.getDegree() > degree) {
            return root;
        }
        for (BranchDecompositionNode child : root.getChildren()) {
            getNodeWithDegreeLargerThan(degree, child);
        }
        return null;
    }

}
