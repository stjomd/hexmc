package at.ac.tuwien.student.e11843614.sharpsat;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.DerivationFactory;
import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.graph.GraphFactory;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

public abstract class ModelCounting {

    // TODO: special cases when derivation does not exist
    // TODO: dynamic algorithm for #SAT

    // utilizing psw
    public static int psw(Formula formula) throws TimeoutException {
        StopWatch stopwatch = StopWatch.create();
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Determine carving-width, build a derivation
        stopwatch.start();
        CarvingDerivation derivation = DerivationFactory.carving(incidenceGraph);
        stopwatch.split();
        Logger.debug("[crw] Elapsed time: " + stopwatch.formatSplitTime());
        // Compute a decomposition from the derivation
        TreeNode<Set<Integer>> decomposition = DecompositionFactory.carving(derivation);
        stopwatch.stop();
        Logger.debug("[crw] Elapsed time: " + stopwatch.formatSplitTime());
        // Solve #SAT
        Logger.warn("#SAT utilizing psw not yet implemented, returning 0");
        return 0;
    }

    // utilizing cw
    public static int cw(Formula formula) throws TimeoutException {
        StopWatch stopwatch = StopWatch.create();
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Determine clique-width, build a derivation
        stopwatch.start();
        CliqueDerivation derivation = DerivationFactory.clique(incidenceGraph);
        stopwatch.split();
        Logger.debug("[cw] Elapsed time: " + stopwatch.formatSplitTime());
        // Compute a decomposition from the derivation
        TreeNode<CliqueOperation> decomposition = DecompositionFactory.clique(derivation, incidenceGraph);
        CliqueDecompositionFactory.makeDisjointColorSets(decomposition);
        stopwatch.stop();
        Logger.debug("[cw] Elapsed time: " + stopwatch.formatTime());
        // Solve #SAT
        Logger.warn("#SAT utilizing cw not yet implemented, returning 0");
        return 0;
    }

}
