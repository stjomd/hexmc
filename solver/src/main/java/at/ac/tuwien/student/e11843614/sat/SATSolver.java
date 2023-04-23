package at.ac.tuwien.student.e11843614.sat;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.formula.Formula;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.util.HashSet;
import java.util.Set;

/**
 * A class that is responsible for communication with the SAT solver.
 */
public abstract class SATSolver {

    /**
     * Runs a SAT solver on a SAT encoding and returns the set of variables assigned to true.
     * @return a set of true variables.
     * @throws TimeoutException if the SAT solver takes too long.
     */
    public static Set<Variable> getSatisfyingAssignment(SATEncoding satEncoding) throws TimeoutException {
        int[] assignments = SATSolver.getModel(satEncoding.formula());
        Set<Variable> truth = new HashSet<>();
        for (int assignment : assignments) {
            if (assignment > 0) {
                truth.add(satEncoding.variableMap().getFromDomain(assignment));
            }
        }
        return truth;
    }

    /**
     * Returns a model of the formula.
     * @param formula a CNF formula.
     * @return an array of integers, representing a model. Positive integers specify variables set to true, negative
     *         integers specify variables set to false. If the formula is unsatisfiable, returns an empty array.
     * @throws TimeoutException if the SAT solver takes too long.
     */
    public static int[] getModel(Formula formula) throws TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        solver.setExpectedNumberOfClauses(formula.clauses().size());
        try {
            for (Clause clause : formula.clauses()) {
                solver.addClause(asVecInt(clause));
            }
            int[] model = solver.findModel();
            if (model == null) {
                Logger.debug("Formula is unsatisfiable");
                return new int[]{};
            }
            Logger.debug("Formula is satisfiable");
            return model;
        } catch (ContradictionException exception) {
            Logger.debug("Formula is unsatisfiable");
            return new int[]{};
        }
    }

    /**
     * Converts this project's Clause object to Sat4j's VecInt.
     * @param clause the clause to be converted to VecInt.
     * @return a VecInt equivalent to clause.
     */
    private static VecInt asVecInt(Clause clause) {
        int[] array = new int[clause.literals().size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = clause.literals().get(i);
        }
        return new VecInt(array);
    }

}
