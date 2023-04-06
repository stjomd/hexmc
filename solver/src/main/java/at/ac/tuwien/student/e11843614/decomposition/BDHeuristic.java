package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public abstract class BDHeuristic {

    /**
     * Computes a minimum vertex cut in the graph, i.e. a set of vertices that, when removed from the graph, decouples
     * it into (at least) two components.
     * @param graph the graph.
     * @param s a vertex.
     * @param t a vertex.
     * @return the minimum vertex s-t-cut.
     */
    public static Set<Integer> minimumVertexCut(Graph<Integer> graph, Integer s, Integer t) {
        // FIXME: Doesn't make sense if there is an edge between s and t?
        if (graph.hasEdgeWithEndpoints(s, t)) {
            return Set.of(s);
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
