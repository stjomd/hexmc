package at.ac.tuwien.student.e11843614.decomposition.derivation;

import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.struct.Partition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An object that represents a derivation for clique width of a graph.
 */
public class CliqueDerivation {

    List<Template> templates = new ArrayList<>();

    /**
     * Constructs a derivation from the component and group variables of the model.
     * @param model a set of variables set to true by the SAT solver.
     * @param sat the SAT encoding for the graph.
     */
    public CliqueDerivation(Set<Variable> model, SATEncoding sat) {
        construct(model, sat);
    }

    // TODO: for tests, TBR
    public CliqueDerivation(int t) {
        for (int i = 0; i <= t; i++)
            templates.add(new Template());
    }

    /**
     * Returns the template at the specified level.
     * @param level the level.
     * @return a Template.
     */
    public Template getTemplate(int level) {
        return templates.get(level);
    }

    /**
     * Returns the components of the template at the specified level.
     * @param level the level.
     * @return the components (a Partition).
     */
    public Partition<Integer> getComponents(int level) {
        return getTemplate(level).getComponents();
    }

    /**
     * Returns the groups of the template at the specified level.
     * @param level the level.
     * @return the groups (a Partition).
     */
    public Partition<Integer> getGroups(int level) {
        return getTemplate(level).getGroups();
    }

    /**
     * Returns the size of this derivation, i.e. the amount of templates (length plus one).
     * @return the size.
     */
    public int size() {
        return templates.size();
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
            templates.add(template);
        }
        // Look at component and group variables and fill the derivation.
        for (Variable variable : model) {
            if (variable.getType() == Variable.Type.COMPONENT || variable.getType() == Variable.Type.GROUP) {
                int u = sat.getVertexMap().getFromDomain(variable.getArgs().get(0));
                int v = sat.getVertexMap().getFromDomain(variable.getArgs().get(1));
                int level = variable.getArgs().get(2);
                //Template template = templates.get(level);
                if (variable.getType() == Variable.Type.COMPONENT) {
                    getComponents(level).add(u, v);
                    //template.getComponents().add(u, v);
                } else if (variable.getType() == Variable.Type.GROUP) {
                    getGroups(level).add(u, v);
                    //template.getGroups().add(u, v);
                }
            } else if (variable.getType() == Variable.Type.REPRESENTATIVE) {
                int u = sat.getVertexMap().getFromDomain(variable.getArgs().get(0));
                int level = variable.getArgs().get(1);
                //Template template = templates.get(level);
                getGroups(level).add(u);
            }
        }
        // TODO: does not meet the conditions
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < templates.size(); i++) {
            Template template = templates.get(i);
            builder.append(i).append(": cmp=").append(template.getComponents())
                .append(", grp=").append(template.getGroups()).append("\n");
        }
        return builder.toString();
    }

}
