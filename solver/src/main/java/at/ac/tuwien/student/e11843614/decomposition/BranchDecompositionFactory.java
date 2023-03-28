package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return bd;
    }

    private static List<Set<Integer>> separation(Graph graph, Graph associatedGraph) {
        // TODO
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
