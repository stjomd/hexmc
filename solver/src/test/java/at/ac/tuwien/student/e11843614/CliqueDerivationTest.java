package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.derivation.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.derivation.Template;
import at.ac.tuwien.student.e11843614.graph.Graph;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.SATEncodingFactory;
import at.ac.tuwien.student.e11843614.sat.SATSolver;
import at.ac.tuwien.student.e11843614.sat.Variable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CliqueDerivationTest {

    private Graph<Integer> graph;
    private CliqueDerivation derivation;

    @BeforeEach
    public void beforeEach() throws TimeoutException {
        graph = new Graph<>();
        graph.addEdge(1, 4);
        graph.addEdge(2, 4);
        graph.addEdge(3, 4);
        SATEncoding encoding = SATEncodingFactory.forCliqueWidth(graph, 2);
        Set<Variable> model = SATSolver.getModel(encoding);
        derivation = new CliqueDerivation(model, encoding);
        //derivation = exampleDerivation();
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
            () -> assertEquals(graph.getVertices().size(), derivation.getTemplate(0).getComponents().size()),
            () -> assertEquals(graph.getVertices().size(), derivation.getTemplate(0).getGroups().size()),
            () -> assertEquals(1, derivation.getTemplate(t).getComponents().size())
        );
    }

    @Test
    @DisplayName("D2")
    public void d2() {
        for (int i = 0; i < derivation.size(); i++) {
            Template template = derivation.getTemplate(i);
            System.out.println("cmp: " + template.getComponents());
            System.out.println("grp: " + template.getGroups());
            System.out.println();
            assertTrue(template.getGroups().isRefinementOf(template.getComponents()));
        }
    }

    @Test
    @DisplayName("D3")
    public void d3() {
        for (int i = 1; i < derivation.size(); i++) {
            Template t1 = derivation.getTemplate(i - 1);
            Template t2 = derivation.getTemplate(i);
            assertTrue(t1.getComponents().isRefinementOf(t2.getComponents()));
        }
    }

    @Test
    @DisplayName("D4")
    public void d4() {
        for (int i = 1; i < derivation.size(); i++) {
            Template t1 = derivation.getTemplate(i - 1);
            Template t2 = derivation.getTemplate(i);
            assertTrue(t1.getGroups().isRefinementOf(t2.getGroups()));
        }
    }

    @Test
    public void s() {
        Template template = derivation.getTemplate(1);
        System.out.println("cmp: " + template.getComponents());
        System.out.println("grp: " + template.getGroups());
        System.out.println("grp refinement of cmp: " + template.getGroups().isRefinementOf(template.getComponents()));
    }

    // TODO: TBR
    private CliqueDerivation exampleDerivation() {
        CliqueDerivation cd = new CliqueDerivation(3);
        // 0
        cd.getComponents(0).add(1); cd.getComponents(0).add(2);
        cd.getComponents(0).add(3); cd.getComponents(0).add(4);
        cd.getGroups(0).add(1); cd.getGroups(0).add(2);
        cd.getGroups(0).add(3); cd.getGroups(0).add(4);
        // 1
        cd.getComponents(1).add(1, 2); cd.getComponents(1).add(3);
        cd.getComponents(1).add(4);
        cd.getGroups(1).add(1); cd.getGroups(1).add(2);
        cd.getGroups(1).add(3); cd.getGroups(1).add(4);
        // 2
        cd.getComponents(2).add(1, 2); cd.getComponents(2).add(2, 3);
        cd.getComponents(2).add(4);
        cd.getGroups(2).add(1); cd.getGroups(2).add(2);
        cd.getGroups(2).add(3); cd.getGroups(2).add(4);
        // 3
        cd.getComponents(3).add(1, 2); cd.getComponents(3).add(2, 3);
        cd.getComponents(3).add(3, 4);
        cd.getGroups(3).add(1, 2); cd.getGroups(3).add(3);
        cd.getGroups(3).add(4);
        return cd;
    }

}
