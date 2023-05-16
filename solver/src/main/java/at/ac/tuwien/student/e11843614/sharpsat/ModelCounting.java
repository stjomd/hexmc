package at.ac.tuwien.student.e11843614.sharpsat;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.exception.InfiniteModelsException;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.sharpsat.clique.CliqueDynamicModelCounting;
import at.ac.tuwien.student.e11843614.sharpsat.psw.PswDynamicModelCounting;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.graph.GraphFactory;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

public abstract class ModelCounting {

    /**
     * Returns the amount of models of a propositional formula.
     * @param formula the formula to count models of.
     * @return the amount of models.
     * @throws TimeoutException if the SAT solver takes too long.
     * @throws InfiniteModelsException if the formula has an infinite number of models.
     */
    public static long count(Formula formula) throws TimeoutException, InfiniteModelsException {
        if (formula.hasEmptyClauses()) {
            Logger.debug("Formula contains an empty (unsatisfiable) clause");
            return 0;
        } else if (formula.clauses().isEmpty()) {
            Logger.debug("Formula has no clauses");
            throw new InfiniteModelsException();
        }
        return psw(formula);
        // return cw(formula);
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
        TreeNode<Set<Integer>> decomposition = DecompositionFactory.pswBranch(incidenceGraph);
        stopwatch.split();
        Logger.debug("[psw] Time elapsed: " + stopwatch.formatSplitTime());
        // Solve #SAT
        long models = PswDynamicModelCounting.count(formula, decomposition);
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
