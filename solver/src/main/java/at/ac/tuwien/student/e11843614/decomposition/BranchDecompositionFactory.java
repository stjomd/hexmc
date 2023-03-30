package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Graph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BranchDecompositionFactory {

    /**
     * Constructs an approximation of a branch decomposition according to a heuristic.
     * @param graph the graph to construct a branch decomposition of.
     * @return the root node of the branch decomposition.
     */
    public static BranchDecompositionNode heuristic(Graph graph) {
        BranchDecompositionNode bd = heuristicInitialSeparation(graph);
        BranchDecompositionNode a = getNodeWithDegreeLargerThan(3, bd);
        assert (a != null && a.getEdge() == null);
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
                int[] endpoints = child.getEdge().getEndpoints();
                partA.add(endpoints[0]);
                partA.add(endpoints[1]);
            }
        }
        for (BranchDecompositionNode child : b.getChildren()) {
            if (child.getDegree() == 1) {
                int[] endpoints = child.getEdge().getEndpoints();
                partB.add(endpoints[0]);
                partB.add(endpoints[1]);
            }
        }
        // The set of linking nodes mid(e) is the intersection of partA and partB.
        Set<Integer> mid = new HashSet<>(partA);
        mid.retainAll(partB);
        // associatedGraph is G_a in the paper. We obtain it by selecting the children of a that are leaves, and adding
        // their edges to the associatedGraph.
        Graph associatedGraph = new Graph();
        for (BranchDecompositionNode child : a.getChildren()) {
            if (child.getDegree() == 1) {
                int[] endpoints = child.getEdge().getEndpoints();
                associatedGraph.addEdge(endpoints[0], endpoints[1]);
            }
        }
        System.out.println(associatedGraph);
        // Now we need to find a separation (X,Y) of associatedGraph.
        List<Set<Integer>> sep = separation(graph, associatedGraph, mid);
        return bd;
    }

    private static List<Set<Integer>> separation(Graph graph, Graph associatedGraph, Set<Integer> mid) {
        // Linking nodes are vertices of associatedGraph that are also in mid.
        Set<Integer> linkingNodes = new HashSet<>(associatedGraph.getVertices());
        linkingNodes.retainAll(mid);
        System.out.println("linking: " + linkingNodes);
        // Source nodes are vertices that are in associatedGraph with eccentricity = diameter, as well as linking nodes.
        Set<Integer> sourceNodes = new HashSet<>(linkingNodes);
        int diameter = associatedGraph.diameter();
        for (Integer vertex : associatedGraph.getVertices()) {
            if (associatedGraph.eccentricity(vertex) == diameter) {
                sourceNodes.add(vertex);
            }
        }
        System.out.println("source: " + sourceNodes);
        // TODO: Iterate over all source nodes, and alpha, beginning here:
        // Choose a source node. Sort vertices of associatedGraph in non-decreasing order acc. to their distance to it.
        Integer chosenSourceNode = null;
        Iterator<Integer> iterator = associatedGraph.getVertices().iterator();
        int index = (int) (Math.random() * associatedGraph.getVertices().size());
        for (int i = 0; i <= index; i++) {
            if (iterator.hasNext()) {
                chosenSourceNode = iterator.next();
            }
        }
        assert (chosenSourceNode != null);
        Integer finalChosenSourceNode = chosenSourceNode; // required for the closure in .sorted()
        List<Integer> sortedVertices = associatedGraph.getVertices().stream()
            .sorted(Comparator.comparing(v -> associatedGraph.distance(v, finalChosenSourceNode)))
            .collect(Collectors.toList());
        // Choose a random value to cut the sortedVertices list.
        int cutoff = (int) (Math.random() * sortedVertices.size());
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
        // TODO: H minor of G with partA identified to v_A, partB identified to v_B -??
        // TODO: find smallest vertex cut in H intersecting all v_A,v_B-paths.
        // means v_A,v_B in diff partitions
        return List.of();
    }

    private static BranchDecompositionNode heuristicInitialSeparation(Graph graph) {
        // Create a star
        BranchDecompositionNode root = new BranchDecompositionNode();
        for (Graph.Edge edge : graph.getEdges()) {
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
