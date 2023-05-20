package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.exception.TimeoutException;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.SATSolver;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.sat.factory.SATEncodingFactory;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;

import java.util.Set;

public abstract class DerivationFactory {

    /**
     * Computes a carving derivation of a graph.
     * @param graph the graph.
     * @return an optimal carving derivation of the graph, or null if such doesn't exist.
     * @throws TimeoutException if the SAT solver takes too long.
     */
    public static CarvingDerivation carving(Graph graph) throws TimeoutException {
        if (graph.vertices().size() <= 1) {
            // graph has no carving
            return null;
        }
        CarvingDerivation derivation = null;
        for (int w = 1; w <= graph.edges().size(); w++) {
            SATEncoding encoding = SATEncodingFactory.forCarvingWidth(graph, w);
            Set<Variable> truths = SATSolver.getSatisfyingAssignment(encoding);
            if (!truths.isEmpty()) {
                Logger.debug("Carving-width is " + w);
                derivation = new CarvingDerivation(truths, encoding);
                break;
            }
        }
        if (derivation == null) {
            throw new Error("Could not determine carving-width");
        }
        return derivation;
    }

    /**
     * Computes a clique derivation of a graph. This method checks for clique-width starting with 2. If the graph
     * has clique-width 1, this method returns null.
     * @param graph the graph.
     * @return an optimal clique derivation of the graph, or null if such doesn't exis or cw(graph) = 1.
     * @throws TimeoutException if the SAT solver takes too long.
     */
    public static CliqueDerivation clique(Graph graph) throws TimeoutException {
        if (graph.vertices().isEmpty() || graph.edges().isEmpty()) {
            return null;
        }
        CliqueDerivation derivation = null;
        for (int w = 2; w <= graph.vertices().size(); w++) {
            SATEncoding encoding = SATEncodingFactory.forCliqueWidth(graph, w);
            Set<Variable> truths = SATSolver.getSatisfyingAssignment(encoding);
            if (!truths.isEmpty()) {
                Logger.debug("Clique-width is " + w);
                derivation = new CliqueDerivation(truths, encoding);
                break;
            }
        }
        if (derivation == null) {
            throw new Error("Could not determine clique-width");
        }
        return derivation;
    }

}
