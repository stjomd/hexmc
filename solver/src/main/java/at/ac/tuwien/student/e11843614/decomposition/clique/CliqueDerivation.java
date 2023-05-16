package at.ac.tuwien.student.e11843614.decomposition.clique;

import at.ac.tuwien.student.e11843614.Logger;
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
     * Constructs a strict derivation from the component and group variables of the assignment.
     * @param assignment a set of variables set to true by the SAT solver.
     * @param sat the SAT encoding for the graph.
     */
    public CliqueDerivation(Set<Variable> assignment, SATEncoding sat) {
        construct(assignment, sat);
        makeStrict();
    }

    /**
     * Returns the template at the specified level.
     * @param level the level.
     * @return a Template.
     */
    public Template template(int level) {
        return templates.get(level);
    }

    /**
     * Returns the components of the template at the specified level.
     * @param level the level.
     * @return the components (a Partition).
     */
    public Partition<Integer> cmp(int level) {
        return template(level).getComponents();
    }

    /**
     * Returns the groups of the template at the specified level.
     * @param level the level.
     * @return the groups (a Partition).
     */
    public Partition<Integer> grp(int level) {
        return template(level).getGroups();
    }

    /**
     * Returns the size of this derivation, i.e. the amount of templates (length plus one).
     * @return the size.
     */
    public int size() {
        return templates.size();
    }

    /**
     * Computes the width of this derivation.
     * @return the width.
     */
    public int getWidth() {
        // width of a component c is number of groups in same template s.t. (g subset of c)
        // width of a template is max width over all its components
        // width of a derivation is max width over all its templates
        int derivationWidth = 0;
        for (Template template : templates) {
            int templateWidth = 0;
            for (Set<Integer> component : template.getComponents()) {
                int componentWidth = 0;
                for (Set<Integer> group : template.getGroups()) {
                    if (component.containsAll(group)) {
                        componentWidth++;
                    }
                }
                templateWidth = Math.max(templateWidth, componentWidth);
            }
            derivationWidth = Math.max(derivationWidth, templateWidth);
        }
        return derivationWidth;
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
            if (variable.type() == Variable.Type.COMPONENT || variable.type() == Variable.Type.GROUP) {
                int level = variable.args().get(2);
                levels = Math.max(levels, level);
            }
        }
        for (int i = 0; i <= levels; i++) {
            Template template = new Template();
            templates.add(template);
        }
        // Look at component and group variables and fill the derivation.
        for (Variable variable : assignment) {
            if (variable.type() == Variable.Type.COMPONENT || variable.type() == Variable.Type.GROUP) {
                int u = sat.vertexMap().getFromDomain(variable.args().get(0));
                int v = sat.vertexMap().getFromDomain(variable.args().get(1));
                int level = variable.args().get(2);
                if (variable.type() == Variable.Type.COMPONENT) {
                    cmp(level).add(u, v);
                } else if (variable.type() == Variable.Type.GROUP) {
                    grp(level).add(u, v);
                }
            } else if (variable.type() == Variable.Type.REPRESENTATIVE) {
                int u = sat.vertexMap().getFromDomain(variable.args().get(0));
                int level = variable.args().get(1);
                grp(level).add(u);
                cmp(level).add(u);
            }
        }
        Logger.debug("Constructed a derivation for clique-width with t = " + (size() - 1));
    }

    /**
     * Makes this derivation strict, i.e. such that |cmp(T_{i-1})| > |cmp(T_i)| for all 1 <= i <= t is fulfilled.
     */
    private void makeStrict() {
        boolean strictifiable = true;
        while (strictifiable) {
            strictifiable = false;
            for (int i = 1; i < templates.size(); i++) {
                // strict if |cmp(T_{i-1})| > |cmp(T_i)| for all 1 <= i <= t.
                if (cmp(i - 1).size() <= cmp(i).size()) {
                    strictifiable = true;
                    templates.remove(i);
                    break;
                }
            }
        }
        Logger.debug("Made derivation strict; t = " + (size() - 1));
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
