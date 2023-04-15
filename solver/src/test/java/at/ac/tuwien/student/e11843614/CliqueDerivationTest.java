package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.derivation.CliqueDerivation;
import at.ac.tuwien.student.e11843614.example.GraphExamples;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.factory.SATEncodingFactory;
import at.ac.tuwien.student.e11843614.sat.SATSolver;
import at.ac.tuwien.student.e11843614.sat.Variable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sat4j.specs.TimeoutException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CliqueDerivationTest {

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
            () -> assertEquals(graph.getVertices().size(), derivation.getComponents(0).size()),
            () -> assertEquals(graph.getVertices().size(), derivation.getGroups(0).size()),
            () -> assertEquals(1, derivation.getComponents(t).size())
        );
    }

    @Test
    @DisplayName("D2")
    public void d2() {
        for (int i = 0; i < derivation.size(); i++) {
            assertTrue(
                derivation.getGroups(i)
                    .isRefinementOf(derivation.getComponents(i))
            );
        }
    }

    @Test
    @DisplayName("D3")
    public void d3() {
        for (int i = 1; i < derivation.size(); i++) {
            assertTrue(
                derivation.getComponents(i - 1)
                    .isRefinementOf(derivation.getComponents(i))
            );
        }
    }

    @Test
    @DisplayName("D4")
    public void d4() {
        for (int i = 1; i < derivation.size(); i++) {
            assertTrue(
                derivation.getGroups(i - 1)
                    .isRefinementOf(derivation.getGroups(i))
            );
        }
    }

    @Test
    @DisplayName("Edge Property")
    public void edgeProperty() {
        for (int i = 1; i < derivation.size(); i++) {
            for (Edge edge : graph.getEdges()) {
                List<Integer> endpoints = edge.getEndpoints();
                boolean inSameGroup = false;
                for (Set<Integer> group : derivation.getGroups(i).getEquivalenceClasses()) {
                    if (group.containsAll(endpoints)) {
                        inSameGroup = true;
                        break;
                    }
                }
                if (inSameGroup) {
                    boolean inSameComponent = false;
                    for (Set<Integer> component : derivation.getComponents(i - 1).getEquivalenceClasses()) {
                        if (component.containsAll(endpoints)) {
                            inSameComponent = true;
                            break;
                        }
                    }
                    assertTrue(inSameComponent);
                }
            }
        }
    }

    @Test
    @DisplayName("Neighborhood Property")
    public void neighborhoodProperty() {
        for (int i = 1; i < derivation.size(); i++) {
            for (Integer u : graph.getVertices()) {
                for (Integer v : graph.getVertices()) {
                    for (Integer w : graph.getVertices()) {
                        if (graph.hasEdgeWithEndpoints(u, v) && !graph.hasEdgeWithEndpoints(u, w)) {
                            boolean inSameGroup = false;
                            for (Set<Integer> group : derivation.getGroups(i).getEquivalenceClasses()) {
                                if (group.contains(v) && group.contains(w)) {
                                    inSameGroup = true;
                                    break;
                                }
                            }
                            if (inSameGroup) {
                                boolean inSameComponent = false;
                                for (Set<Integer> component : derivation.getComponents(i - 1).getEquivalenceClasses()) {
                                    if (component.contains(u) && component.contains(v)) {
                                        inSameComponent = true;
                                        break;
                                    }
                                }
                                assertTrue(inSameComponent);
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
            for (Integer u : graph.getVertices()) {
                for (Integer v : graph.getVertices()) {
                    for (Integer w : graph.getVertices()) {
                        for (Integer x : graph.getVertices()) {
                            if (graph.hasEdgeWithEndpoints(u, v) && graph.hasEdgeWithEndpoints(u, w)
                                && graph.hasEdgeWithEndpoints(v, x) && !graph.hasEdgeWithEndpoints(w, x)) {
                                boolean uxInSameGroup = false;
                                boolean vwInSameGroup = false;
                                for (Set<Integer> group : derivation.getGroups(i).getEquivalenceClasses()) {
                                    if (group.contains(u) && group.contains(x))
                                        uxInSameGroup = true;
                                    if (group.contains(v) && group.contains(w))
                                        vwInSameGroup = true;
                                    if (uxInSameGroup && vwInSameGroup)
                                        break;
                                }
                                if (uxInSameGroup && vwInSameGroup) {
                                    boolean uvInSameComponent = false;
                                    for (Set<Integer> component : derivation.getComponents(i - 1).getEquivalenceClasses()) {
                                        if (component.contains(u) && component.contains(v)) {
                                            uvInSameComponent = true;
                                            break;
                                        }
                                    }
                                    assertTrue(uvInSameComponent);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
