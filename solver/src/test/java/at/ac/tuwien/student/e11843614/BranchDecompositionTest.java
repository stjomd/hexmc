package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.decomposition.BranchDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.BranchDecompositionHeuristic;

import at.ac.tuwien.student.e11843614.decomposition.derivation.BranchDerivation;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.SATSolver;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.sat.factory.SATEncodingFactory;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import at.ac.tuwien.student.e11843614.example.GraphExamples;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sat4j.specs.TimeoutException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BranchDecompositionTest {

    private Graph graph;
    private TreeNode<Edge> heuristic;
    private TreeNode<Edge> exact;

    @BeforeEach
    public void beforeEach() throws TimeoutException {
        graph = GraphExamples.example();
        // Heuristic
        heuristic = BranchDecompositionFactory.heuristic(graph);
        // Exact
        SATEncoding sat = SATEncodingFactory.forBranchWidth(graph, 4);
        Set<Variable> assignment = SATSolver.getSatisfyingAssignment(sat);
        BranchDerivation derivation = new BranchDerivation(assignment, sat);
        exact = BranchDecompositionFactory.from(derivation);
        System.out.println(exact);
    }

    @AfterEach
    public void afterEach() {
        graph = null;
        heuristic = null;
        exact = null;
    }

    @Test
    @DisplayName("Heuristic is valid")
    public void heuristic() {
        checkIfValidBranchDecomposition(heuristic, graph);
    }

    @Test
    @DisplayName("Branch derivation to valid decomposition")
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
