package at.ac.tuwien.student.e11843614.sharpsat;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.SATSolver;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.sat.factory.SATEncodingFactory;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.graph.GraphFactory;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

public abstract class ModelCounting {

    // utilizing psw
    public static int psw(Formula formula) throws TimeoutException, WidthComputationException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Determine carving width
        // TODO: max carving width for graph with n vertices?
        CarvingDerivation derivation = null;
        for (int w = 1; w <= incidenceGraph.vertices().size(); w++) {
            SATEncoding encoding = SATEncodingFactory.forCarvingWidth(incidenceGraph, w);
            Set<Variable> truths = SATSolver.getSatisfyingAssignment(encoding);
            if (!truths.isEmpty()) {
                Logger.debug("Carving-width is " + w);
                derivation = new CarvingDerivation(truths, encoding);
                break;
            }
        }
        if (derivation == null) {
            throw new WidthComputationException("Could not determine carving-width");
        }
        // TODO: dynamic algorithm for #SAT
        Logger.warn("#SAT utilizing psw not yet implemented, returning 0");
        return 0;
    }

    // utilizing cw
    public static int cw(Formula formula) throws TimeoutException, WidthComputationException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Determine clique-width
        CliqueDerivation derivation = null;
        for (int w = 1; w <= incidenceGraph.vertices().size(); w++) {
            SATEncoding encoding = SATEncodingFactory.forCliqueWidth(incidenceGraph, w);
            Set<Variable> truths = SATSolver.getSatisfyingAssignment(encoding);
            if (!truths.isEmpty()) {
                Logger.debug("Clique-width is " + w);
                derivation = new CliqueDerivation(truths, encoding);
                break;
            }
        }
        if (derivation == null) {
            throw new WidthComputationException("Could not determine clique-width");
        }
        // TODO: dynamic algorithm for #SAT
        Logger.warn("#SAT utilizing cw not yet implemented, returning 0");
        return 0;
    }

}
