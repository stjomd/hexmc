package at.ac.tuwien.student.e11843614.decomposition.carving;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.struct.Partition;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An object that represents a derivation for carving decompositions.
 */
public class CarvingDerivation {

    List<Partition<Integer>> derivation = new ArrayList<>();

    /**
     * Constructs a derivation from the set and leader variables of the assignment.
     * @param assignment a set of variables set to true by the SAT solver.
     */
    public CarvingDerivation(Set<Variable> assignment, SATEncoding sat) {
        construct(assignment, sat);
    }

    /**
     * Returns the partition at the specified level of this derivation.
     * @param level the level, a number between one and the size.
     * @return the partition at the specified level.
     */
    public Partition<Integer> getLevel(int level) {
        return derivation.get(level - 1);
    }

    /**
     * Returns the size of this derivation (amount of partitions).
     * @return the size.
     */
    public int size() {
        return derivation.size();
    }

    /**
     * Computes the width of this derivation.
     * @param graph the corresponding graph.
     * @return the width.
     */
    public int getWidth(Graph graph) {
        // Let V be an equivalence class from any level of the derivation. Let delta(V) denote the set of edges in
        // the corresponding graph that have one endpoint in V and one endpoint outside V. Then the width of this
        // derivation is the maximum |delta(V)| over all equivalence classes V.
        int width = 0;
        for (Partition<Integer> partition : derivation) {
            for (Set<Integer> ec : partition.equivalenceClasses()) {
                Set<Edge> edges = new HashSet<>();
                for (Edge edge : graph.edges()) {
                    int u = edge.endpoints().get(0);
                    int v = edge.endpoints().get(1);
                    if (ec.contains(u) && !ec.contains(v)) {
                        edges.add(edge);
                    } else if (ec.contains(v) && !ec.contains(u)) {
                        edges.add(edge);
                    }
                }
                width = Math.max(width, edges.size());
            }
        }
        return width;
    }

    /**
     * Fills the derivation's partitions.
     * @param assignment the satisfying assignment, the set of variables set to true.
     * @param sat the SAT encoding.
     */
    private void construct(Set<Variable> assignment, SATEncoding sat) {
        int levels = 0;
        for (Variable variable : assignment) {
            if (variable.type() == Variable.Type.LEADER) {
                levels = Math.max(levels, variable.args().get(1));
            }
        }
        // Create a partition for each level.
        for (int i = 0; i < levels; i++) {
            derivation.add(new Partition<>());
        }
        // Go through leader and set variables
        for (Variable variable : assignment) {
            if (variable.type() == Variable.Type.LEADER) {
                Integer vertex = sat.vertexMap().getFromDomain(variable.args().get(0));
                int level = variable.args().get(1);
                Partition<Integer> partition = getLevel(level);
                partition.add(vertex);
            } else if (variable.type() == Variable.Type.SET) {
                Integer vertex1 = sat.vertexMap().getFromDomain(variable.args().get(0));
                Integer vertex2 = sat.vertexMap().getFromDomain(variable.args().get(1));
                int level = variable.args().get(2);
                Partition<Integer> partition = getLevel(level);
                partition.add(vertex1, vertex2);
            }
        }
        Logger.debug("Constructed a derivation for carving-width with l = " + size());
    }

    /**
     * Checks whether this derivation fulfils the conditions.
     * @param graph the graph associated with this derivation.
     * @return true, if all conditions are satisfied, and false otherwise.
     */
    public boolean fulfilsConditions(Graph graph) {
        int l = derivation.size();
        // D1
        // P_1 has |V(G)| equivalence classes, each consisting of one element
        if (getLevel(1).size() != graph.vertices().size()) {
            return false;
        }
        for (Set<Integer> ec : getLevel(1).equivalenceClasses()) {
            if (ec.size() != 1) {
                return false;
            }
        }
        // P_l has 1 equivalence class which contains all vertices
        if (getLevel(l).size() != 1) {
            return false;
        }
        if (!getLevel(l).equivalenceClasses().iterator().next().containsAll(graph.vertices())) {
            return false;
        }
        // D2
        for (int i = 1; i < l - 2; i++) {
            if (!getLevel(i).isBinaryRefinementOf(getLevel(i + 1))) {
                return false;
            }
        }
        // D3
        if (!getLevel(l - 1).isTernaryRefinementOf(getLevel(l))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < derivation.size(); i++) {
            Partition<Integer> partition = derivation.get(i);
            builder.append(i + 1).append(": ").append(partition).append("\n");
        }
        return builder.toString();
    }

}