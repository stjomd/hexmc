package at.ac.tuwien.student.e11843614.sharpsat;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
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

    public enum CheckResult {
        UNSATISFIABLE, INFINITE, UNCLEAR
    }

    /**
     * Performs special case check on the formula. Returns the amount of models or a special value indicating that
     * the models need to be counted with the other methods in this class.
     * @param formula the formula to perform checks against.
     * @return a CheckResult value: UNSATISFIABLE if the formula is unsatisfiable, INFINITE if the formula is empty and
     *         is satisfied by any assignment, and UNCLEAR if the models have to be counted using other methods in this
     *         class.
     */
    public static CheckResult specialCaseCheck(Formula formula) {
        if (formula.hasEmptyClauses()) {
            Logger.debug("Formula contains an empty (unsatisfiable) clause");
            return CheckResult.UNSATISFIABLE;
        } else if (formula.clauses().isEmpty()) {
            Logger.debug("Formula has no clauses");
            return CheckResult.INFINITE;
        }
        return CheckResult.UNCLEAR;
    }

    /**
     * Counts the amount of models of a propositional formula with a dynamic algorithm utilizing the ps-width of the
     * formula.
     * @param formula the formula.
     * @return the amount of the formula's models.
     * @throws TimeoutException if the SAT solver takes too long while computing a carving derivation.
     */
    public static int psw(Formula formula) throws TimeoutException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Compute a branch decomposition (as defined in the psw paper)
        StopWatch stopwatch = StopWatch.createStarted();
        TreeNode<Set<Integer>> decomposition = DecompositionFactory.pswBranch(incidenceGraph);
        stopwatch.split();
        Logger.debug("[psw] Time elapsed: " + stopwatch.formatSplitTime());
        // Solve #SAT
        int models = PswDynamicModelCounting.count(formula, decomposition);
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
    public static int cw(Formula formula) throws TimeoutException {
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
