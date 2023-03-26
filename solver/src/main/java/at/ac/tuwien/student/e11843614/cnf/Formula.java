package at.ac.tuwien.student.e11843614.cnf;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An object that represents a propositional formula in conjunctive normal form (CNF).
 */
public class Formula {

    private final List<List<Integer>> clauses = new ArrayList<>();

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
     * @param variables an array of variables, represented by integers, representing a clause.
     */
    public void addClause(Integer[] variables) {
        List<Integer> list = Arrays.asList(variables);
        addClause(list);
    }

    /**
     * Adds a clause to the formula.
     * @param variables a list of variables, represented by integers, representing a clause.
     */
    public void addClause(List<Integer> variables) {
        clauses.add(variables);
    }

    /**
     * Retrieves the list of clauses.
     * @return the list of clauses.
     */
    public List<List<Integer>> getClauses() {
        return clauses;
    }

}
