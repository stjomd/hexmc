package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.BranchDecompositionHeuristic;
import at.ac.tuwien.student.e11843614.decomposition.BranchDecompositionNode;
import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.graph.Graph;
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

    private Graph<Integer> graph;
    private BranchDecompositionNode heuristic;

    @BeforeEach
    public void beforeEach() {
        // a1, b2, c3, d4, e5, f6, g7, h8, i9, j10, k11
        graph = new Graph<>();
        graph.addEdge(1, 4); // ad
        graph.addEdge(1, 8); // ah
        graph.addEdge(1, 2); // ab
        graph.addEdge(1, 3); // ac
        graph.addEdge(4, 5); // de
        graph.addEdge(2, 5); // be
        graph.addEdge(2, 6); // bf
        graph.addEdge(3, 6); // cf
        graph.addEdge(3, 7); // cg
        graph.addEdge(7, 8); // gh
        graph.addEdge(5, 9); // ei
        graph.addEdge(6, 9); // fi
        graph.addEdge(6, 10); // fj
        graph.addEdge(7, 10); // gj
        graph.addEdge(4, 11); // dk
        graph.addEdge(8, 11); // hk
        graph.addEdge(9, 11); // ik
        graph.addEdge(10, 11); // jk
        heuristic = BranchDecompositionHeuristic.heuristic(graph);
    }

    @AfterEach
    public void afterEach() {
        graph = null;
        heuristic = null;
    }

    @Test
    @DisplayName("Heuristic produces valid branch decomposition")
    public void heuristicShouldProduceValidBranchDecomposition() {
        Queue<BranchDecompositionNode> queue = new LinkedList<>();
        Set<Edge<Integer>> edges = new HashSet<>();
        queue.add(heuristic);
        while (!queue.isEmpty()) {
            BranchDecompositionNode node = queue.remove();
            if (node.getEdge() == null) {
                // Internal nodes have no edge, and have degree 3.
                assertEquals(3, node.getDegree());
            } else {
                // Leaves must have an edge, and have degree 1.
                assertEquals(1, node.getDegree());
                edges.add(node.getEdge());
            }
            queue.addAll(node.getChildren());
        }
        // Branch decomposition must contain exactly all edges of the graph.
        assertEquals(graph.getEdges(), edges);
    }

}
