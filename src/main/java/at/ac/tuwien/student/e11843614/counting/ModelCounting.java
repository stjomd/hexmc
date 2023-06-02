package at.ac.tuwien.student.e11843614.counting;

import at.ac.tuwien.student.e11843614.Arguments;
import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.exception.MemoryError;
import at.ac.tuwien.student.e11843614.exception.OverflowException;
import at.ac.tuwien.student.e11843614.exception.TimeoutException;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.counting.clique.CliqueDynamicModelCounting;
import at.ac.tuwien.student.e11843614.counting.psw.PSDynamicModelCounting;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.graph.GraphFactory;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class ModelCounting {

    public enum Algorithm {
        psw, cw
    }

    /**
     * A Callable that performs all necessary actions and returns the amount of models.
     */
    private static class CountCallable implements Callable<Long> {

        private final Formula formula;
        private final Algorithm algorithm;

        public CountCallable(Formula formula, Algorithm algorithm) {
            this.formula = formula;
            this.algorithm = algorithm;
        }

        @Override
        public Long call() throws Exception {
            if (formula.hasEmptyClauses()) {
                Logger.debug("Formula contains an empty (unsatisfiable) clause");
                return 0L;
            } else if (formula.clauses().isEmpty()) {
                Logger.debug("Formula has no clauses");
                return 0L;
            }
            switch (algorithm) {
                case psw:
                    return psw(formula);
                case cw:
                    return cw(formula);
                default:
                    throw new IllegalArgumentException("Dynamic algorithm '" + algorithm + "' does not exist");
            }
        }

    }

    /**
     * Returns the amount of models of a propositional formula.
     * @param formula the formula to count models of.
     * @param algorithm the algorithm to use for the computation.
     * @param timeout the timeout in seconds. If zero or negative, timeout is infinite.
     * @return the amount of models.
     * @throws TimeoutException if the timeout was exceeded.
     */
    public static long count(Formula formula, Algorithm algorithm, long timeout) throws TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> models = executor.submit(new CountCallable(formula, algorithm));
        try {
            if (timeout <= 0) {
                return models.get();
            } else {
                long result = models.get(timeout, TimeUnit.SECONDS);
                executor.shutdownNow();
                return result;
            }
        } catch (java.util.concurrent.TimeoutException exception) {
            executor.shutdownNow();
            throw new TimeoutException("Timeout (" + timeout + " s) exceeded", exception);
        } catch (ExecutionException exception) {
            executor.shutdownNow();
            if (exception.getCause() instanceof ArithmeticException) {
                throw new OverflowException("Long overflow", exception);
            } else if (exception.getCause() instanceof OutOfMemoryError) {
                throw new MemoryError("Ran out of memory", exception);
            } else {
                throw new RuntimeException(exception);
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            throw new RuntimeException(exception);
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
        if (Arguments.carving()) {
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
        if (decomposition == null) {
            // Only the case if the incidence graph has no vertices. However, this case (empty formula) is caught
            // in pre-checks, therefore decomposition is guaranteed to be not null.
            throw new IllegalStateException("Decomposition is null");
            // Throw exception to avoid warnings.
        }
        int models = CliqueDynamicModelCounting.count(decomposition);
        stopwatch.stop();
        Logger.debug("[cw] Time elapsed: " + stopwatch.formatTime());
        return models;
    }

}
