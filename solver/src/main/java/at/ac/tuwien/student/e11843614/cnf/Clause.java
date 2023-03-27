package at.ac.tuwien.student.e11843614.cnf;

import java.util.ArrayList;
import java.util.List;
public class Clause {

    private final List<Integer> literals = new ArrayList<>();

    public Clause() {}

    public Clause(List<Integer> literals) {
        this.literals.addAll(literals);
    }

    public void addLiteral(Integer literal) {
        this.literals.add(literal);
    }

    @Override
    public String toString() {
        return this.literals.toString();
    }

}
