package at.ac.tuwien.student.e11843614.counting;

import at.ac.tuwien.student.e11843614.Constants;
import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.counting.clique.CliqueDynamicModelCounting;
import at.ac.tuwien.student.e11843614.counting.psw.PSDynamicModelCounting;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.graph.GraphFactory;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

public abstract class ModelCounting {

    public enum Algorithm {
        psw, cw
    }

    /**
     * Returns the amount of models of a propositional formula.
     * @param formula the formula to count models of.
     * @param algorithm the algorithm to use for the computation.
     * @return the amount of models.
     * @throws TimeoutException if the SAT solver takes too long.
     */
    public static long count(Formula formula, Algorithm algorithm) throws TimeoutException {
        if (formula.hasEmptyClauses()) {
            Logger.debug("Formula contains an empty (unsatisfiable) clause");
            return 0;
        } else if (formula.clauses().isEmpty()) {
            Logger.debug("Formula has no clauses");
            return 0;
        }
        switch (algorithm) {
            case psw:
                return psw(formula);
            case cw:
                return cw(formula);
            default:
                throw new IllegalArgumentException("Dynamic algorithm '" + Constants.algorithm() + "' does not exist");
        }
    }

    /**
     * Counts the amount of models of a propositional formula with a dynamic algorithm utilizing the ps-width of the
     * formula.
     * @param formula the formula.
     * @return the amount of the formula's models.
     * @throws TimeoutException if the SAT solver takes too long while computing a carving derivation.
     */
    private static long psw(Formula formula) throws TimeoutException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Compute a branch decomposition (as defined in the psw paper)
        StopWatch stopwatch = StopWatch.createStarted();
        TreeNode<Set<Integer>> decomposition;
        if (Constants.carving()) {
            decomposition = DecompositionFactory.pswBranchFromCarving(incidenceGraph);
        } else {
            decomposition = DecompositionFactory.pswBranch(incidenceGraph);
        }
        stopwatch.split();
        Logger.debug("[psw] Time elapsed: " + stopwatch.formatSplitTime());
        // Solve #SAT
        long models = PSDynamicModelCounting.count(formula, decomposition);
        stopwatch.stop();
        Logger.debug("[psw] Time elapsed: " + stopwatch.formatTime());
        return models;
    }

    /**
     * Counts the amount of models of a propositional formula with a dynamic algorithm utilizing the clique-width of the
     * formula.
     * @param formula the formula.
     * @return the amount of the formula's models.
     * @throws TimeoutException if the SAT solver takes too long while computing a clique derivation.
     */
    private static long cw(Formula formula) throws TimeoutException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Compute a parse tree for clique width
        StopWatch stopwatch = StopWatch.createStarted();
        TreeNode<CliqueOperation> decomposition = DecompositionFactory.clique(incidenceGraph, true);
        stopwatch.split();
        Logger.debug("[cw] Time elapsed: " + stopwatch.formatSplitTime());
        // Solve #SAT
        int models = CliqueDynamicModelCounting.count(decomposition);
        stopwatch.stop();
        Logger.debug("[cw] Time elapsed: " + stopwatch.formatTime());
        return models;
    }

}
