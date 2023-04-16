package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.branch.BranchDerivation;
import at.ac.tuwien.student.e11843614.decomposition.branch.BranchDerivationFactory;
import at.ac.tuwien.student.e11843614.example.GraphExamples;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
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

@DisplayName("Branchwidth")
public class BranchwidthTest {

    @Nested
    @DisplayName("Derivation Tests")
    public class BranchDerivationTest {

        private Graph graph;
        private BranchDerivation derivation;

        @BeforeEach
        public void beforeEach() throws TimeoutException {
            derivation = BranchDerivationFactory.branch(4, () -> {
                Graph petersen = GraphExamples.petersen();
                graph = petersen;
                return petersen;
            });
        }

        @AfterEach
        public void afterEach() {
            graph = null;
            derivation = null;
        }

        @Test
        @DisplayName("D1")
        public void d1() {
            int l = derivation.size();
            // P_1 has |E(G)| equivalence classes, each consisting of one element
            assertEquals(graph.getEdges().size(), derivation.getLevel(1).size(), "P_1 has more than |E(G)| sets");
            for (Set<Edge> ec : derivation.getLevel(1).getEquivalenceClasses()) {
                assertEquals(1, ec.size(), "An equivalence class in P_1 has more than 1 edge");
            }
            // P_l has 1 equivalence class which contains all edges
            assertEquals(1, derivation.getLevel(l).size(), "P_l does not have 1 equivalence class");
            Set<Edge> ec = derivation.getLevel(l).getEquivalenceClasses().iterator().next();
            assertTrue(ec.containsAll(graph.getEdges()), "The equivalence class in P_l doesn't contain all edges");
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
    public class BranchDecompositionTest {

        private Graph graph;
        private TreeNode<Edge> exact;
        private TreeNode<Edge> heuristic;

        @BeforeEach
        public void beforeEach() throws TimeoutException {
            // Exact
            BranchDerivation derivation = BranchDerivationFactory.branch(4, () -> {
                Graph example = GraphExamples.example();
                graph = example;
                return example;
            });
            assert derivation != null;
            exact = DecompositionFactory.branch(derivation);
            // Heuristic
            heuristic = DecompositionFactory.branchHeuristic(graph);
        }

        @AfterEach
        public void afterEach() {
            graph = null;
            exact = null;
            heuristic = null;
        }

        @Test
        @DisplayName("Heuristic is valid")
        public void heuristic() {
            checkIfValidBranchDecomposition(heuristic, graph);
        }

        @Test
        @DisplayName("Derivation to valid decomposition")
        public void exact() {
            checkIfValidBranchDecomposition(exact, graph);
        }

        private void checkIfValidBranchDecomposition(TreeNode<Edge> decomposition, Graph graph) {
            Queue<TreeNode<Edge>> queue = new LinkedList<>();
            Set<Edge> edges = new HashSet<>();
            queue.add(decomposition);
            while (!queue.isEmpty()) {
                TreeNode<Edge> node = queue.remove();
                if (node.getObject() == null) {
                    // Internal nodes have no edge, and have degree 3.
                    assertEquals(3, node.getDegree(), "Internal node has degree " + node.getDegree());
                } else {
                    // Leaves must have an edge, and have degree 1.
                    assertEquals(1, node.getDegree(), "Leaf has degree " + node.getDegree());
                    edges.add(node.getObject());
                }
                queue.addAll(node.getChildren());
            }
            // Branch decomposition must contain exactly all edges of the graph.
            assertEquals(graph.getEdges(), edges, "Branch decomposition does not contain all edges of the graph");
        }

    }

}
