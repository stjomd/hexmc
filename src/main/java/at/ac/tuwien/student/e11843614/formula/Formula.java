package at.ac.tuwien.student.e11843614.formula;

import at.ac.tuwien.student.e11843614.exception.FormulaParseException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An object that represents a propositional formula in conjunctive normal form (CNF).
 */
public class Formula {

    private final List<Clause> clauses = new ArrayList<>();

    /**
     * Constructs a formula from a DIMACS CNF file at the given path.
     * @param path the path to the DIMACS CNF file.
     * @return a Formula object that represents the formula in the file.
     * @throws FileNotFoundException if the file at the specified path cannot be found.
     * @throws FormulaParseException if an error occurs during parsing the file.
     */
    public static Formula fromPath(String path) throws FileNotFoundException, FormulaParseException {
        FormulaReader reader = new FormulaReader(path);
        return reader.parseFormula();
    }

    /**
     * Adds a clause to the formula.
     * @param literals literals, represented by integers, representing a clause.
     */
    public void addClause(Integer... literals) {
        Clause clause = new Clause();
        for (Integer literal : literals) {
            clause.addLiteral(literal);
        }
        addClause(clause);
    }

    /**
     * Adds a clause to the formula.
     * @param clause a clause to be added.
     */
    public void addClause(Clause clause) {
        clause.setPosition(clauses.size() + 1);
        clauses.add(clause);
    }

    /**
     * Retrieves the list of clauses.
     * @return the list of clauses.
     */
    public List<Clause> clauses() {
        return clauses;
    }

    /**
     * Computes the set of variables in this formula.
     * @return the set of variables.
     */
    public Set<Integer> getVariables() {
        Set<Integer> variables = new HashSet<>();
        for (Clause clause : clauses) {
            for (Integer literal : clause.literals()) {
                variables.add(Math.abs(literal));
            }
        }
        return variables;
    }

    /**
     * Returns a boolean value indicating whether this formula contains an empty clause.
     * @return true if there is an empty clause in the formula, and false otherwise.
     */
    public boolean hasEmptyClauses() {
        for (Clause clause : clauses) {
            if (clause.literals().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return clauses.toString();
    }

}
