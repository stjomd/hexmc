package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.DerivationFactory;
import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.example.GraphExamples;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sat4j.specs.TimeoutException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Carving")
public class CarvingTests {

    @Nested
    @DisplayName("Derivation Tests")
    public class CarvingDerivationTests {

        private Graph graph;
        private CarvingDerivation derivation;

        @BeforeEach
        public void beforeAll() throws TimeoutException {
            graph = GraphExamples.petersen();
            derivation = DerivationFactory.carving(graph);
            assert derivation != null;
        }

        @AfterEach
        public void afterAll() {
            graph = null;
            derivation = null;
        }

        @Test
        @DisplayName("D1")
        public void d1() {
            int l = derivation.size();
            // P_1 has |V(G)| equivalence classes, each consisting of one element
            assertEquals(graph.vertices().size(), derivation.getLevel(1).size(), "P_1 has more than |V(G)| sets");
            for (Set<Integer> ec : derivation.getLevel(1).equivalenceClasses()) {
                assertEquals(1, ec.size(), "An equivalence class in P_1 has more than 1 edge");
            }
            // P_l has 1 equivalence class which contains all edges
            assertEquals(1, derivation.getLevel(l).size(), "P_l does not have 1 equivalence class");
            Set<Integer> ec = derivation.getLevel(l).equivalenceClasses().iterator().next();
            assertTrue(ec.containsAll(graph.vertices()), "The equivalence class in P_l doesn't contain all vertices");
        }

        @Test
        @DisplayName("D2")
        public void d2() {
            int l = derivation.size();
            for (int i = 1; i < l - 2; i++) {
                assertTrue(derivation.getLevel(i).isBinaryRefinementOf(derivation.getLevel(i + 1)),
                    "P_" + i + " is not a 2-ary refinement of P_" + (i+1));
            }
        }

        @Test
        @DisplayName("D3")
        public void d3() {
            int l = derivation.size();
            assertTrue(derivation.getLevel(l - 1).isTernaryRefinementOf(derivation.getLevel(l)),
                "P_" + (l-1) + " is not a 3-ary refinement of P_" + l);
        }

    }

    @Nested
    @DisplayName("Decomposition Tests")
    public class CarvingDecompositionTests {

        private Graph graph;
        private TreeNode<Set<Integer>> exact;

        @BeforeEach
        public void beforeEach() throws TimeoutException {
            // Exact
            graph = GraphExamples.example();
            exact = DecompositionFactory.carving(graph);
        }

        @AfterEach
        public void afterEach() {
            graph = null;
            exact = null;
        }

        @Test
        @DisplayName("Derivation to valid decomposition")
        public void exact() {
            checkIfValidCarvingDecomposition(exact, graph);
        }

        private void checkIfValidCarvingDecomposition(TreeNode<Set<Integer>> decomposition, Graph graph) {
            Queue<TreeNode<Set<Integer>>> queue = new LinkedList<>();
            Set<Integer> vertices = new HashSet<>();
            queue.add(decomposition);
            while (!queue.isEmpty()) {
                TreeNode<Set<Integer>> node = queue.remove();
                if (node.object().size() > 1) {
                    // Internal nodes have degree 3.
                    assertEquals(3, node.degree(), "Internal node has degree " + node.degree());
                } else {
                    // Leaves must have degree 1.
                    assertEquals(1, node.degree(), "Leaf has degree " + node.degree());
                    vertices.addAll(node.object());
                }
                queue.addAll(node.children());
            }
            // Carving decomposition must contain exactly all vertices of the graph.
            assertEquals(graph.vertices(), vertices, "Carving decomposition does not contain all vertices of the graph");
        }

    }

}
