package at.ac.tuwien.student.e11843614.decomposition.clique.operation;

/**
 * Contents of a node that represents edge creation.
 */
public class CliqueEdgeCreation implements CliqueOperation {

    private final int from, to;

    public CliqueEdgeCreation(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    @Override
    public String toString() {
        return String.format("edges(%d -> %d)", from, to);
    }

}
