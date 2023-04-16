package at.ac.tuwien.student.e11843614.decomposition.clique.contents;

import java.util.Set;

/**
 * Contents of a node that represents a disjoint union.
 */
class CliqueDecompositionUnion implements CliqueDecompositionContents {

    private final Set<Integer> component;
    private final int level;

    public CliqueDecompositionUnion(Set<Integer> component, int level) {
        this.component = component;
        this.level = level;
    }

    public Set<Integer> getComponent() {
        return component;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "union";
    }

}