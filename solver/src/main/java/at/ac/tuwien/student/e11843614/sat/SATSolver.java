package at.ac.tuwien.student.e11843614.sat;

import at.ac.tuwien.student.e11843614.formula.Clause;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * A class that is responsible for communication with the SAT solver.
 */
public abstract class SATSolver {

    /**
     * Returns a model of the formula in the specified SAT encoding.
     * @param encoding a SAT encoding.
     * @return an array of integers, representing a model. Positive integers specify variables set to true, negative
     *         integers specify variables set to false.
     * @throws TimeoutException if the SAT solver takes too long.
     */
    public static int[] getModels(SATEncoding encoding) throws TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        solver.setExpectedNumberOfClauses(encoding.getFormula().getClauses().size());
        try {
            for (Clause clause : encoding.getFormula().getClauses()) {
                solver.addClause(asVecInt(clause));
            }
            return solver.findModel();
        } catch (ContradictionException exception) {
            return new int[]{};
        }
    }

    /**
     * Converts this project's Clause object to Sat4j's VecInt.
     * @param clause the clause to be converted to VecInt.
     * @return a VecInt equivalent to clause.
     */
    private static VecInt asVecInt(Clause clause) {
        int[] array = new int[clause.getLiterals().size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = clause.getLiterals().get(i);
        }
        return new VecInt(array);
    }

}
