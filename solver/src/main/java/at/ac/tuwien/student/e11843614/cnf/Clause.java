package at.ac.tuwien.student.e11843614.cnf;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a clause of a propositional formula.
 */
public class Clause {

    private final List<Integer> literals = new ArrayList<>();

    public Clause() {}

    /**
     * Adds a literal to the clause.
     * @param literal a literal represented by an integer (by a negative one if the literal is negated).
     */
    public void addLiteral(Integer literal) {
        this.literals.add(literal);
    }

    /**
     * Retrieves the list of literals in the clause.
     * @return the list of literals.
     */
    public List<Integer> getLiterals() {
        return literals;
    }

    @Override
    public String toString() {
        return this.literals.toString();
    }

}
