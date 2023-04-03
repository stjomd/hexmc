package at.ac.tuwien.student.e11843614.decomposition.derivation;

import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An object that represents a derivation for clique width of a graph.
 */
public class CliqueDerivation {

    List<Template> derivation = new ArrayList<>();

    /**
     * Constructs a derivation from the component and group variables of the model.
     * @param model a set of variables set to true by the SAT solver.
     * @param sat the SAT encoding for the graph.
     */
    public CliqueDerivation(Set<Variable> model, SATEncoding sat) {
        construct(model, sat);
    }

    /**
     * Fills the derivation's templates.
     * @param model the model set.
     * @param sat the SAT encoding.
     */
    private void construct(Set<Variable> model, SATEncoding sat) {
        // Create enough templates. Look at component/group variables and their levels.
        int levels = 0;
        for (Variable variable : model) {
            if (variable.getType() == Variable.Type.COMPONENT || variable.getType() == Variable.Type.GROUP) {
                int level = variable.getArgs().get(2);
                levels = Math.max(levels, level);
            }
        }
        for (int i = 0; i <= levels; i++) {
            Template template = new Template();
            derivation.add(template);
        }
        // Look at component and group variables and fill the derivation.
        for (Variable variable : model) {
            if (variable.getType() == Variable.Type.COMPONENT || variable.getType() == Variable.Type.GROUP) {
                int u = sat.getVertexMap().getFromDomain(variable.getArgs().get(0));
                int v = sat.getVertexMap().getFromDomain(variable.getArgs().get(1));
                int level = variable.getArgs().get(2);
                Template template = derivation.get(level);
                if (variable.getType() == Variable.Type.COMPONENT) {
                    template.getComponents().add(u, v);
                } else if (variable.getType() == Variable.Type.GROUP) {
                    template.getGroups().add(u, v);
                }
            } else if (variable.getType() == Variable.Type.REPRESENTATIVE) {
                // TODO: not sure if this has to be done: Look at representative variables, add the groups
                int u = sat.getVertexMap().getFromDomain(variable.getArgs().get(0));
                int level = variable.getArgs().get(1);
                Template template = derivation.get(level);
                template.getGroups().add(u);
            }
        }
        // TODO: does not meet the conditions
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < derivation.size(); i++) {
            Template template = derivation.get(i);
            builder.append(i).append(": cmp=").append(template.getComponents())
                .append(", grp=").append(template.getGroups()).append("\n");
        }
        return builder.toString();
    }

}
