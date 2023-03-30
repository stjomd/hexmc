package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.graph.Graph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class GraphTest {

    @Test
    @DisplayName("Contraction modifies the graph correctly")
    public void contraction() {
        Graph<Integer> graph = new Graph<>();
        Edge<Integer> contractedEdge = new Edge<>(3, 4);
        Edge<Integer> redirectedEdge = new Edge<>(2, 3);
        graph.addEdge(1, 2);
        graph.addEdge(redirectedEdge);
        graph.addEdge(contractedEdge);
        graph.addEdge(4, 1);
        graph.addEdge(4, 5);
        // G = ({1, 2, 3, 4, 5}, {12, 23, 34, 41, 45})
        assertEquals(5, graph.getVertices().size());
        assertEquals(5, graph.getEdges().size());
        assertEquals(2, redirectedEdge.getEndpoints().get(0));
        assertEquals(3, redirectedEdge.getEndpoints().get(1));
        graph.contractEdge(contractedEdge);
        // G = ({1, 2, 4, 5}, {12, 24, 41, 45})
        assertEquals(4, graph.getVertices().size());
        assertEquals(4, graph.getEdges().size());
        assertFalse(graph.getEdges().contains(contractedEdge));
        // Reference to redirectedEdge keeps, endpoint changes
        assertTrue(graph.getEdges().contains(redirectedEdge));
        assertEquals(2, redirectedEdge.getEndpoints().get(0));
        assertEquals(4, redirectedEdge.getEndpoints().get(1));
    }

}
