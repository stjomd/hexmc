package at.ac.tuwien.student.e11843614.decomposition.derivation;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.struct.Partition;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An object that represents a derivation for clique width of a graph.
 */
public class CliqueDerivation {

    List<Template> templates = new ArrayList<>();

    /**
     * Constructs a derivation from the component and group variables of the assignment.
     * @param assignment a set of variables set to true by the SAT solver.
     * @param sat the SAT encoding for the graph.
     */
    public CliqueDerivation(Set<Variable> assignment, SATEncoding sat) {
        construct(assignment, sat);
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
     * @param assignment the satisfying assignment, the set of variables set to true.
     * @param sat the SAT encoding.
     */
    private void construct(Set<Variable> assignment, SATEncoding sat) {
        // Create enough templates. Look at component/group variables and their levels.
        int levels = 0;
        for (Variable variable : assignment) {
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
        for (Variable variable : assignment) {
            if (variable.getType() == Variable.Type.COMPONENT || variable.getType() == Variable.Type.GROUP) {
                int u = sat.vertexMap().getFromDomain(variable.getArgs().get(0));
                int v = sat.vertexMap().getFromDomain(variable.getArgs().get(1));
                int level = variable.getArgs().get(2);
                if (variable.getType() == Variable.Type.COMPONENT) {
                    getComponents(level).add(u, v);
                } else if (variable.getType() == Variable.Type.GROUP) {
                    getGroups(level).add(u, v);
                }
            } else if (variable.getType() == Variable.Type.REPRESENTATIVE) {
                int u = sat.vertexMap().getFromDomain(variable.getArgs().get(0));
                int level = variable.getArgs().get(1);
                getGroups(level).add(u);
                getComponents(level).add(u);
            } else if (variable.getType() == Variable.Type.NUMBER) {
                // TODO: direct encoding, decide if needed
                int v = sat.vertexMap().getFromDomain(variable.getArgs().get(0));
                int level = variable.getArgs().get(2);
                getGroups(level).add(v);
                getComponents(level).add(v);
            }
        }
        Logger.debug("Constructed a derivation for clique-width of length " + (size() - 1));
    }

    /**
     * Checks whether this derivation fulfils the conditions.
     * @param graph the graph associated with this derivation.
     * @return true, if all conditions are satisfied, and false otherwise.
     */
    public boolean fulfilsConditions(Graph<Integer> graph) {
        // D1
        int t = templates.size() - 1;
        if (graph.getVertices().size() != getComponents(0).size()
            || graph.getVertices().size() != getGroups(0).size() || 1 != getComponents(t).size()) {
            return false;
        }
        // D2
        for (int i = 0; i < templates.size(); i++) {
            if (!getGroups(i).isRefinementOf(getComponents(i))) {
                return false;
            }
        }
        // D3
        for (int i = 1; i < templates.size(); i++) {
            if (!getComponents(i - 1).isRefinementOf(getComponents(i))) {
                return false;
            }
        }
        // D4
        for (int i = 1; i < templates.size(); i++) {
            if (!getGroups(i - 1).isRefinementOf(getGroups(i))) {
                return false;
            }
        }
        return true;
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
