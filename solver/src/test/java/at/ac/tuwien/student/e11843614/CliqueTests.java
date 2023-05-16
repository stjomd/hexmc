package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueEdgeCreation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueRecoloring;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueSingleton;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueUnion;
import at.ac.tuwien.student.e11843614.example.GraphExamples;
import at.ac.tuwien.student.e11843614.example.PartitionChecks;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.SATSolver;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.sat.factory.SATEncodingFactory;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sat4j.specs.TimeoutException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Clique")
public class CliqueTests {

    @Nested
    @DisplayName("Derivation Tests")
    public class CliqueDerivationTests {

        private Graph graph;
        private CliqueDerivation derivation;

        @BeforeEach
        public void beforeEach() throws TimeoutException {
            graph = GraphExamples.example();
            SATEncoding encoding = SATEncodingFactory.forCliqueWidth(graph, 5);
            Set<Variable> model = SATSolver.getSatisfyingAssignment(encoding);
            derivation = new CliqueDerivation(model, encoding);
        }

        @AfterEach
        public void afterEach() {
            graph = null;
            derivation = null;
        }

        @Test
        @DisplayName("D1")
        public void d1() {
            int t = derivation.size() - 1;
            assertAll(
                () -> assertEquals(graph.vertices().size(), derivation.cmp(0).size(), "|cmp(T_0)| is not |V|"),
                () -> assertEquals(graph.vertices().size(), derivation.grp(0).size(), "|grp(T_0)| is not |V|"),
                () -> assertEquals(1, derivation.cmp(t).size(), "|cmp(T_t)| is not 1")
            );
        }

        @Test
        @DisplayName("D2")
        public void d2() {
            for (int i = 0; i < derivation.size(); i++) {
                assertTrue(
                    PartitionChecks.isRefinement(derivation.grp(i), derivation.cmp(i)),
                    "grp(T_" + i + ") is not a refinement of cmp(T_" + i + ")"
                );
            }
        }

        @Test
        @DisplayName("D3")
        public void d3() {
            for (int i = 1; i < derivation.size(); i++) {
                assertTrue(
                    PartitionChecks.isRefinement(derivation.cmp(i - 1), derivation.cmp(i)),
                    "cmp(T_" + (i-1) + ") is not a refinement of cmp(T_" + i + ")"
                );
            }
        }

        @Test
        @DisplayName("D4")
        public void d4() {
            for (int i = 1; i < derivation.size(); i++) {
                assertTrue(
                    PartitionChecks.isRefinement(derivation.grp(i - 1), derivation.grp(i)),
                    "grp(T_" + (i-1) + ") is not a refinement of grp(T_" + i + ")"
                );
            }
        }

        @Test
        @DisplayName("Edge Property")
        public void edgeProperty() {
            for (int i = 1; i < derivation.size(); i++) {
                for (Edge edge : graph.edges()) {
                    List<Integer> endpoints = edge.endpoints();
                    boolean inSameGroup = false;
                    for (Set<Integer> group : derivation.grp(i).equivalenceClasses()) {
                        if (group.containsAll(endpoints)) {
                            inSameGroup = true;
                            break;
                        }
                    }
                    if (inSameGroup) {
                        boolean inSameComponent = false;
                        for (Set<Integer> component : derivation.cmp(i - 1).equivalenceClasses()) {
                            if (component.containsAll(endpoints)) {
                                inSameComponent = true;
                                break;
                            }
                        }
                        assertTrue(
                            inSameComponent,
                            String.format("%d, %d are both in same group in T_%d, but aren't in same component in T_%d",
                                endpoints.get(0), endpoints.get(1), i, i - 1)
                        );
                    }
                }
            }
        }

        @Test
        @DisplayName("Neighborhood Property")
        public void neighborhoodProperty() {
            for (int i = 1; i < derivation.size(); i++) {
                for (Integer u : graph.vertices()) {
                    for (Integer v : graph.vertices()) {
                        for (Integer w : graph.vertices()) {
                            if (graph.hasEdgeWithEndpoints(u, v) && !graph.hasEdgeWithEndpoints(u, w)) {
                                boolean inSameGroup = false;
                                for (Set<Integer> group : derivation.grp(i).equivalenceClasses()) {
                                    if (group.contains(v) && group.contains(w)) {
                                        inSameGroup = true;
                                        break;
                                    }
                                }
                                if (inSameGroup) {
                                    boolean inSameComponent = false;
                                    for (Set<Integer> component : derivation.cmp(i - 1).equivalenceClasses()) {
                                        if (component.contains(u) && component.contains(v)) {
                                            inSameComponent = true;
                                            break;
                                        }
                                    }
                                    assertTrue(
                                        inSameComponent,
                                        String.format("%d, %d are both in same group in T_%d, but %d, %d aren't both in same component in T_%d",
                                            v, w, i, u, v, i - 1)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Path Property")
        public void pathProperty() {
            for (int i = 1; i < derivation.size(); i++) {
                for (Integer u : graph.vertices()) {
                    for (Integer v : graph.vertices()) {
                        for (Integer w : graph.vertices()) {
                            for (Integer x : graph.vertices()) {
                                if (graph.hasEdgeWithEndpoints(u, v) && graph.hasEdgeWithEndpoints(u, w)
                                    && graph.hasEdgeWithEndpoints(v, x) && !graph.hasEdgeWithEndpoints(w, x)) {
                                    boolean uxInSameGroup = false;
                                    boolean vwInSameGroup = false;
                                    for (Set<Integer> group : derivation.grp(i).equivalenceClasses()) {
                                        if (group.contains(u) && group.contains(x))
                                            uxInSameGroup = true;
                                        if (group.contains(v) && group.contains(w))
                                            vwInSameGroup = true;
                                        if (uxInSameGroup && vwInSameGroup)
                                            break;
                                    }
                                    if (uxInSameGroup && vwInSameGroup) {
                                        boolean uvInSameComponent = false;
                                        for (Set<Integer> component : derivation.cmp(i - 1).equivalenceClasses()) {
                                            if (component.contains(u) && component.contains(v)) {
                                                uvInSameComponent = true;
                                                break;
                                            }
                                        }
                                        assertTrue(
                                            uvInSameComponent,
                                            String.format("%d, %d are both in same group in T_%d; %d, %d are both in same "
                                                    + "group in T_%d; but %d, %d aren't both in same component in T_%d",
                                                u, x, i, v, w, i, u, v, i - 1)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Nested
    @DisplayName("Decomposition Tests")
    public class CliqueDecompositionTests {

        @Test
        @DisplayName("Clique decomposition is valid")
        public void decompositionShouldBeValid() throws TimeoutException {
            Graph graph = GraphExamples.triangularPrism();
            TreeNode<CliqueOperation> decomposition = DecompositionFactory.clique(graph, false);
            checkIfValidDecomposition(decomposition, graph);
        }

        @Test
        @DisplayName("Clique decomposition with disjoint color sets is valid")
        public void decompositionWithDisjointColorSetsShouldBeValid() throws TimeoutException {
            Graph graph = GraphExamples.triangularPrism();
            TreeNode<CliqueOperation> decomposition = DecompositionFactory.clique(graph, true);
            checkIfValidDecomposition(decomposition, graph);
            // For every union node, check which colors appear under its children
            for (TreeNode<CliqueOperation> node : decomposition) {
                if (!(node.object() instanceof CliqueUnion)) {
                    continue;
                }
                Iterator<TreeNode<CliqueOperation>> childrenIterator = node.children().iterator();
                TreeNode<CliqueOperation> child1 = childrenIterator.next();
                TreeNode<CliqueOperation> child2 = childrenIterator.next();
                Set<Integer> set1 = computeColorMap(child1).keySet();
                Set<Integer> set2 = computeColorMap(child2).keySet();
                // set1 and set2 must be disjoint
                set1.retainAll(set2);
                assertTrue(set1.isEmpty());
            }
        }

        private Map<Integer, Set<Integer>> computeColorMap(TreeNode<CliqueOperation> node) {
            Map<Integer, Set<Integer>> map = new HashMap<>();
            Iterator<TreeNode<CliqueOperation>> iterator = node.depthIterator();
            while (iterator.hasNext()) {
                TreeNode<CliqueOperation> leaf = iterator.next();
                if (!(leaf.object() instanceof CliqueSingleton)) {
                    continue;
                }
                CliqueSingleton singleton = (CliqueSingleton) leaf.object();
                // For each singleton, backtrack to 'node', determine which color it has.
                TreeNode<CliqueOperation> current = leaf;
                int color = singleton.color();
                while (current != node) {
                    current = current.parent();
                    if (current.object() instanceof CliqueRecoloring) {
                        CliqueRecoloring recoloring = (CliqueRecoloring) current.object();
                        if (color == recoloring.from()) {
                            color = recoloring.to();
                        }
                    }
                }
                // If 'node' is a recoloring node, take it into account too
                if (current.object() instanceof CliqueRecoloring) {
                    CliqueRecoloring recoloring = (CliqueRecoloring) current.object();
                    if (color == recoloring.from()) {
                        color = recoloring.to();
                    }
                }
                // Add to the map
                if (!map.containsKey(color)) {
                    map.put(color, new HashSet<>());
                }
                map.get(color).add(singleton.vertex());
            }
            return map;
        }

        private void checkIfValidDecomposition(TreeNode<CliqueOperation> decomposition, Graph graph) {
            if (decomposition == null) {
                return;
            }
            Set<Integer> visitedVertices = new HashSet<>();
            Set<Edge> createdEdges = new HashSet<>();
            for (TreeNode<CliqueOperation> node : decomposition) {
                if (node.object() instanceof CliqueSingleton) {
                    CliqueSingleton singleton = (CliqueSingleton) node.object();
                    // Singletons must be leaves
                    assertTrue(node.children().isEmpty());
                    assertNotNull(node.parent());
                    visitedVertices.add(singleton.vertex());
                } else if (node.object() instanceof CliqueUnion) {
                    // Union nodes must have exactly two children
                    assertEquals(2, node.children().size());
                } else if (node.object() instanceof CliqueRecoloring) {
                    CliqueRecoloring recoloring = (CliqueRecoloring) node.object();
                    // Recoloring nodes must have exactly one child, and have different indices
                    assertEquals(1, node.children().size());
                    assertNotEquals(recoloring.from(), recoloring.to());
                } else if (node.object() instanceof CliqueEdgeCreation) {
                    CliqueEdgeCreation edgeCreation = (CliqueEdgeCreation) node.object();
                    // Edge creation nodes must have exactly one child, and have different indices
                    assertEquals(1, node.children().size());
                    assertNotEquals(edgeCreation.from(), edgeCreation.to());
                    // Compute the edges created by this node
                    Map<Integer, Set<Integer>> colorMap = computeColorMap(node);
                    Set<Integer> sourceVertices = colorMap.get(edgeCreation.from());
                    Set<Integer> targetVertices = colorMap.get(edgeCreation.to());
                    for (Integer source : sourceVertices) {
                        for (Integer target : targetVertices) {
                            createdEdges.add(new Edge(source, target));
                        }
                    }
                }
            }
            // Decomposition must contain all vertices of the graph
            assertTrue(visitedVertices.containsAll(graph.vertices()));
            assertTrue(graph.vertices().containsAll(visitedVertices));
            // Decomposition must create all edges of the graph
            Set<Edge> checkedEdges = new HashSet<>();
            for (Edge edge : createdEdges) {
                List<Integer> endpoints = edge.endpoints();
                // Find an equivalent edge in the graph
                boolean found = false;
                for (Edge graphEdge : graph.edges()) {
                    List<Integer> graphEndpoints = graphEdge.endpoints();
                    if (graphEndpoints.containsAll(endpoints)) {
                        // Found the edge
                        checkedEdges.add(graphEdge);
                        found = true;
                        break;
                    }
                }
                assertTrue(found, String.format("Edge %s created by the k-expression does not appear in the graph", edge));
            }
            assertTrue(checkedEdges.containsAll(graph.edges()));
            assertTrue(graph.edges().containsAll(checkedEdges));
        }

    }

}
