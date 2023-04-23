package at.ac.tuwien.student.e11843614.decomposition.branch;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.struct.Partition;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;

import java.util.ArrayList;
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
     * Fills the derivation's partitions.
     * @param assignment the satisfying assignment, the set of variables set to true.
     * @param sat the SAT encoding.
     */
    private void construct(Set<Variable> assignment, SATEncoding sat) {
        int levels = 0;
        for (Variable variable : assignment) {
            if (variable.getType() == Variable.Type.LEADER) {
                levels = Math.max(levels, variable.getArgs().get(1));
            }
        }
        // Create a partition for each level.
        for (int i = 0; i < levels; i++) {
            derivation.add(new Partition<>());
        }
        // Go through leader and set variables
        for (Variable variable : assignment) {
            if (variable.getType() == Variable.Type.LEADER) {
                Integer vertex = sat.vertexMap().getFromDomain(variable.getArgs().get(0));
                int level = variable.getArgs().get(1);
                Partition<Integer> partition = getLevel(level);
                partition.add(vertex);
            } else if (variable.getType() == Variable.Type.SET) {
                Integer vertex1 = sat.vertexMap().getFromDomain(variable.getArgs().get(0));
                Integer vertex2 = sat.vertexMap().getFromDomain(variable.getArgs().get(1));
                int level = variable.getArgs().get(2);
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
        if (getLevel(1).size() != graph.getVertices().size()) {
            return false;
        }
        for (Set<Integer> ec : getLevel(1).getEquivalenceClasses()) {
            if (ec.size() != 1) {
                return false;
            }
        }
        // P_l has 1 equivalence class which contains all vertices
        if (getLevel(l).size() != 1) {
            return false;
        }
        if (!getLevel(l).getEquivalenceClasses().iterator().next().containsAll(graph.getVertices())) {
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
