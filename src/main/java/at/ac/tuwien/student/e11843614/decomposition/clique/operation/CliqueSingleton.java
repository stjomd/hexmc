package at.ac.tuwien.student.e11843614.decomposition.clique.operation;

/**
 * Contents of a node that represents a singleton.
 */
public class CliqueSingleton implements CliqueOperation {

    private final int vertex;
    private int color;

    public CliqueSingleton(int vertex, int color) {
        this.vertex = vertex;
        this.color = color;
    }

    public int vertex() {
        return vertex;
    }

    public int color() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return String.format("vertex(%d, color=%d)", vertex, color);
    }

}