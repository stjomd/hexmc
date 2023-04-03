package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An object that represents a derivation for branch decompositions.
 */
public class BranchDerivation {

    // A derivation is a sequence of Set<Set<Edge>>
    //   - each Set<Set<Edge>> is a partition at some level
    //   - each Set<Edge> is an equivalence class
    List<Set<Set<Edge<Integer>>>> derivation = new ArrayList<>();

    /**
     * Constructs a derivation from the set and leader variables of the model.
     * @param model a set of variables set to true by the SAT solver.
     */
    public BranchDerivation(Set<Variable> model, SATEncoding sat) {
        construct(model, sat);
    }

    private void construct(Set<Variable> model, SATEncoding sat) {
        int levels = 0;
        Set<Variable> leaders = new HashSet<>();
        for (Variable variable : model) {
            if (variable.getType() == Variable.Type.LEADER) {
                levels = Math.max(levels, variable.getArgs().get(1));
                leaders.add(variable);
            }
        }
        // Create a partition for each level.
        for (int i = 1; i <= levels; i++) {
            derivation.add(new HashSet<>());
        }
        // For each leader variable, create an equivalence class
        for (Variable leader : leaders) {
            Edge<Integer> edge = sat.getEdgeMap().getFromDomain(leader.getArgs().get(0));
            int level = leader.getArgs().get(1);
            Set<Set<Edge<Integer>>> partition = derivation.get(level - 1);
            Set<Edge<Integer>> equivalenceClass = new HashSet<>();
            equivalenceClass.add(edge);
            partition.add(equivalenceClass);
        }
        // For each set variable, fill the equivalence classes
        for (Variable variable : model) {
            if (variable.getType() == Variable.Type.SET) {
                Edge<Integer> edge1 = sat.getEdgeMap().getFromDomain(variable.getArgs().get(0));
                Edge<Integer> edge2 = sat.getEdgeMap().getFromDomain(variable.getArgs().get(1));
                int level = variable.getArgs().get(2);
                Set<Set<Edge<Integer>>> partition = derivation.get(level - 1);
                for (Set<Edge<Integer>> equivalenceClass : partition) {
                    if (equivalenceClass.contains(edge1)) {
                        equivalenceClass.add(edge2);
                    } else if (equivalenceClass.contains(edge2)) {
                        equivalenceClass.add(edge1);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < derivation.size(); i++) {
            Set<Set<Edge<Integer>>> partition = derivation.get(i);
            builder.append(i + 1).append(": ").append(partition).append("\n");
        }
        return builder.toString();
    }

}
