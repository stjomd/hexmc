package at.ac.tuwien.student.e11843614.decomposition.clique.contents;

/**
 * Contents of a node that represents edge creation.
 */
public class CliqueDecompositionEdgeCreation implements CliqueDecompositionContents {

    private final int from, to;

    public CliqueDecompositionEdgeCreation(int from, int to) {
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
        return String.format("edges(%d -> %d)", from, to);
    }

}
