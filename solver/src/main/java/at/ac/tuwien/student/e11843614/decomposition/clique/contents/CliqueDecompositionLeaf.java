package at.ac.tuwien.student.e11843614.decomposition.clique.contents;

import java.util.Set;

/**
 * Contents of a node that represents a singleton.
 */
class CliqueDecompositionLeaf implements CliqueDecompositionContents {

    private final Set<Integer> component;
    private final int level;
    private final int vertex;
    private final int color;

    public CliqueDecompositionLeaf(Set<Integer> component, int level, int vertex, int color) {
        this.component = component;
        this.level = level;
        this.vertex = vertex;
        this.color = color;
    }

    public Set<Integer> getComponent() {
        return component;
    }

    public int getLevel() {
        return level;
    }

    public int getVertex() {
        return vertex;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.format("vertex(%d, color=%d)", vertex, color);
    }

}