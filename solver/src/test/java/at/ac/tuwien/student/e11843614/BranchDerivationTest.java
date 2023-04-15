package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.derivation.BranchDerivation;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BranchDerivationTest {

    private Graph graph;
    private BranchDerivation derivation;

    @BeforeEach
    public void beforeEach() throws TimeoutException {
        graph = GraphExamples.petersen();
        SATEncoding encoding = SATEncodingFactory.forBranchWidth(graph, 4);
        Set<Variable> model = SATSolver.getSatisfyingAssignment(encoding);
        derivation = new BranchDerivation(model, encoding);
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
        assertEquals(graph.getEdges().size(), derivation.getLevel(1).size());
        for (Set<Edge> ec : derivation.getLevel(1).getEquivalenceClasses()) {
            assertEquals(1, ec.size());
        }
        // P_l has 1 equivalence class which contains all edges
        assertEquals(1, derivation.getLevel(l).size());
        Set<Edge> ec = derivation.getLevel(l).getEquivalenceClasses().iterator().next();
        assertTrue(ec.containsAll(graph.getEdges()));
    }

    @Test
    @DisplayName("D2")
    public void d2() {
        int l = derivation.size();
        for (int i = 1; i < l - 2; i++) {
            assertTrue(derivation.getLevel(i).isBinaryRefinementOf(derivation.getLevel(i + 1)));
        }
    }

    @Test
    @DisplayName("D3")
    public void d3() {
        int l = derivation.size();
        assertTrue(derivation.getLevel(l - 1).isTernaryRefinementOf(derivation.getLevel(l)));
    }

}
