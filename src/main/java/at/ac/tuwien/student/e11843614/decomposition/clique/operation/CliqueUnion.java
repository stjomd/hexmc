package at.ac.tuwien.student.e11843614.decomposition.clique.operation;

import java.util.Set;

/**
 * Contents of a node that represents a disjoint union.
 */
public class CliqueUnion implements CliqueOperation {

    private final Set<Integer> component;
    private final int level;

    public CliqueUnion(Set<Integer> component, int level) {
        this.component = component;
        this.level = level;
    }

    public Set<Integer> component() {
        return component;
    }

    public int level() {
        return level;
    }

    @Override
    public String toString() {
        return "union(" + component + ", level=" + level + ")";
    }

}