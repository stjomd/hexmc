package at.ac.tuwien.student.e11843614.decomposition.clique.operation;

/**
 * Contents of a node that represents recoloring.
 */
public class CliqueRecoloring implements CliqueOperation {

    private final int from, to;

    public CliqueRecoloring(int from, int to) {
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
        return String.format("recolor(%d -> %d)", from, to);
    }

}
