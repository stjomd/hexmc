package at.ac.tuwien.student.e11843614.sat;

import java.util.List;
import java.util.Objects;

/**
 * A class that represents a propositional variable.
 */
public class Variable {

    /**
     * An enum that represents the type of the variable.
     */
    public enum Type {
        SET, LEADER, LOAD, COUNTER
    }

    private Type type;
    private List<Integer> args;

    private Variable() {}

    /**
     * A set variable. Should be true if edges e, f are in the same set at level i.
     * @param e an edge.
     * @param f an edge.
     * @param i a level of the derivation.
     * @return the set variable.
     */
    public static Variable set(int e, int f, int i) {
        Variable var = new Variable();
        var.type = Type.SET;
        var.args = List.of(e, f, i);
        return var;
    }

    /**
     * A leader variable. Should be true if e is the smallest edge in some set at level i.
     * @param e an edge.
     * @param i a level of the derivation.
     * @return the leader variable.
     */
    public static Variable leader(int e, int i) {
        Variable var = new Variable();
        var.type = Type.LEADER;
        var.args = List.of(e, i);
        return var;
    }

    /**
     * A load variable. Should be true if u is a load vertex of the edge e at level i.
     * @param e an edge.
     * @param u a vertex.
     * @param i a level of the derivation.
     * @return the load variable.
     */
    public static Variable load(int e, int u, int i) {
        Variable var = new Variable();
        var.type = Type.LOAD;
        var.args = List.of(e, u, i);
        return var;
    }

    /**
     * A counter variable. Should be true if u is the j-th load vertex of the edge e at level i.
     * @param e an edge.
     * @param u a vertex.
     * @param i a level of the derivation.
     * @param j the order.
     * @return the counter variable.
     */
    public static Variable counter(int e, int u, int i, int j) {
        Variable var = new Variable();
        var.type = Type.COUNTER;
        var.args = List.of(e, u, i, j);
        return var;
    }

    /**
     * Returns the type of this variable.
     * @return a value of Type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the arguments of this variable. The contents depend on the type of this variable.
     * @return a list of arguments (integers) of this variable.
     */
    public List<Integer> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return type.toString() + "(" + args + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Variable))
            return false;
        Variable other = (Variable) obj;
        return this.type == other.type && this.args.equals(other.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, args);
    }

}