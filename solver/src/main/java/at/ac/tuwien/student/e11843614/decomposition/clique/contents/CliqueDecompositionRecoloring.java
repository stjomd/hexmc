package at.ac.tuwien.student.e11843614.decomposition.clique.contents;

/**
 * Contents of a node that represents recoloring.
 */
public class CliqueDecompositionRecoloring implements CliqueDecompositionContents {

    private final int from, to;

    public CliqueDecompositionRecoloring(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    @Override
    public String toString() {
        return String.format("recolor(%d, %d)", from, to);
    }
}
