package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.branch.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.branch.CarvingDerivationFactory;
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
            derivation = CarvingDerivationFactory.carving(6, () -> {
                Graph petersen = GraphExamples.petersen();
                graph = petersen;
                return petersen;
            });
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
            assertEquals(graph.getVertices().size(), derivation.getLevel(1).size(), "P_1 has more than |V(G)| sets");
            for (Set<Integer> ec : derivation.getLevel(1).getEquivalenceClasses()) {
                assertEquals(1, ec.size(), "An equivalence class in P_1 has more than 1 edge");
            }
            // P_l has 1 equivalence class which contains all edges
            assertEquals(1, derivation.getLevel(l).size(), "P_l does not have 1 equivalence class");
            Set<Integer> ec = derivation.getLevel(l).getEquivalenceClasses().iterator().next();
            assertTrue(ec.containsAll(graph.getVertices()), "The equivalence class in P_l doesn't contain all vertices");
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
    public class CarvingDecompositionTest {

        private Graph graph;
        private TreeNode<Integer> exact;

        @BeforeEach
        public void beforeEach() throws TimeoutException {
            // Exact
            CarvingDerivation derivation = CarvingDerivationFactory.carving(8, () -> {
                Graph example = GraphExamples.example();
                graph = example;
                return example;
            });
            assert derivation != null;
            exact = DecompositionFactory.carving(derivation);
        }

        @AfterEach
        public void afterEach() {
            graph = null;
            exact = null;
        }

        @Test
        @DisplayName("Derivation to valid decomposition")
        public void exact() {
            checkIfValidBranchDecomposition(exact, graph);
        }

        private void checkIfValidBranchDecomposition(TreeNode<Integer> decomposition, Graph graph) {
            Queue<TreeNode<Integer>> queue = new LinkedList<>();
            Set<Integer> vertices = new HashSet<>();
            queue.add(decomposition);
            while (!queue.isEmpty()) {
                TreeNode<Integer> node = queue.remove();
                if (node.getObject() == null) {
                    // Internal nodes have no vertex, and have degree 3.
                    assertEquals(3, node.degree(), "Internal node has degree " + node.degree());
                } else {
                    // Leaves must have a vertex stored, and have degree 1.
                    assertEquals(1, node.degree(), "Leaf has degree " + node.degree());
                    vertices.add(node.getObject());
                }
                queue.addAll(node.getChildren());
            }
            // Branch decomposition must contain exactly all vertices of the graph.
            assertEquals(graph.getVertices(), vertices, "Branch decomposition does not contain all vertices of the graph");
        }

    }

}
