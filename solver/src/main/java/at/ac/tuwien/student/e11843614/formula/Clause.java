package at.ac.tuwien.student.e11843614.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class representing a clause of a propositional formula.
 */
public class Clause {

    private final List<Integer> literals = new ArrayList<>();
    private int position = 0;

    public Clause() {}

    /**
     * Creates a copy of a clause.
     * @param other the clause to be copied.
     */
    public Clause(Clause other) {
        this.literals.addAll(other.literals);
        this.position = other.position();
    }

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
    public List<Integer> literals() {
        return literals;
    }

    public int position() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "cl" + this.literals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clause clause = (Clause) o;
        return Objects.equals(literals, clause.literals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(literals);
    }

}
