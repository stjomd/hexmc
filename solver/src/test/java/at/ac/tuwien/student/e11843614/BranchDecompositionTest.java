package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.BranchDecompositionHeuristic;

import at.ac.tuwien.student.e11843614.decomposition.TreeNode;
import at.ac.tuwien.student.e11843614.example.GraphExamples;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BranchDecompositionTest {

    private Graph graph;
    private TreeNode<Edge> heuristic;

    @BeforeEach
    public void beforeEach() {
        // a1, b2, c3, d4, e5, f6, g7, h8, i9, j10, k11
        graph = GraphExamples.example();
        heuristic = BranchDecompositionHeuristic.of(graph);
    }

    @AfterEach
    public void afterEach() {
        graph = null;
        heuristic = null;
    }

    @Test
    @DisplayName("Heuristic produces valid branch decomposition")
    public void heuristicShouldProduceValidBranchDecomposition() {
        Queue<TreeNode<Edge>> queue = new LinkedList<>();
        Set<Edge> edges = new HashSet<>();
        queue.add(heuristic);
        while (!queue.isEmpty()) {
            TreeNode<Edge> node = queue.remove();
            if (node.getObject() == null) {
                // Internal nodes have no edge, and have degree 3.
                assertEquals(3, node.getDegree());
            } else {
                // Leaves must have an edge, and have degree 1.
                assertEquals(1, node.getDegree());
                edges.add(node.getObject());
            }
            queue.addAll(node.getChildren());
        }
        // Branch decomposition must contain exactly all edges of the graph.
        assertEquals(graph.getEdges(), edges);
    }

}
