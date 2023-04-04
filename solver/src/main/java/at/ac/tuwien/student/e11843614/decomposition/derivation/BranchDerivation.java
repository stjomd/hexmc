package at.ac.tuwien.student.e11843614.decomposition.derivation;

import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.struct.Partition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An object that represents a derivation for branch decompositions.
 */
public class BranchDerivation {

    List<Partition<Edge<Integer>>> derivation = new ArrayList<>();

    /**
     * Constructs a derivation from the set and leader variables of the model.
     * @param model a set of variables set to true by the SAT solver.
     */
    public BranchDerivation(Set<Variable> model, SATEncoding sat) {
        construct(model, sat);
    }

    /**
     * Fills the derivation's partitions.
     * @param model the model set.
     * @param sat the SAT encoding.
     */
    private void construct(Set<Variable> model, SATEncoding sat) {
        int levels = 0;
        for (Variable variable : model) {
            if (variable.getType() == Variable.Type.LEADER) {
                levels = Math.max(levels, variable.getArgs().get(1));
            }
        }
        // Create a partition for each level.
        for (int i = 1; i <= levels; i++) {
            derivation.add(new Partition<>());
        }
        // Go through leader and set variables
        for (Variable variable : model) {
            if (variable.getType() == Variable.Type.LEADER) {
                Edge<Integer> edge = sat.getEdgeMap().getFromDomain(variable.getArgs().get(0));
                int level = variable.getArgs().get(1);
                Partition<Edge<Integer>> partition = derivation.get(level - 1);
                partition.add(edge);
            } else if (variable.getType() == Variable.Type.SET) {
                Edge<Integer> edge1 = sat.getEdgeMap().getFromDomain(variable.getArgs().get(0));
                Edge<Integer> edge2 = sat.getEdgeMap().getFromDomain(variable.getArgs().get(1));
                int level = variable.getArgs().get(2);
                Partition<Edge<Integer>> partition = derivation.get(level - 1);
                partition.add(edge1, edge2);
            }
        }
        // TODO: does not meet conditions
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < derivation.size(); i++) {
            Partition<Edge<Integer>> partition = derivation.get(i);
            builder.append(i + 1).append(": ").append(partition).append("\n");
        }
        return builder.toString();
    }

}