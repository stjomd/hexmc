package at.ac.tuwien.student.e11843614.sharpsat.psw;

import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class PswDynamicModelCounting {

    public static int count(Formula formula, TreeNode<Set<Integer>> decomposition) {
        int width = 0;
        PSSetMap psMap = computePSSets(formula, decomposition);
        return width;
    }

    // ----- Computing PS Sets -----------------------------------------------------------------------------------------

    /**
     * Computes the precisely satisfiable sets for this decomposition.
     * @param formula the original formula.
     * @param decomposition the root node of the decomposition.
     * @return a map from nodes of the decomposition to PS sets.
     */
    private static PSSetMap computePSSets(Formula formula, TreeNode<Set<Integer>> decomposition) {
        PSSetMap map = new PSSetMap();
        // First, we compute the sets for the base cases: root node and leaves.
        computePSbaseCases(formula, decomposition, map);
        // Now, we compute the PS sets for F_v for internal nodes.
        computePSpositives(formula, decomposition, map);
        // Finally, we compute the PS sets for F_-v for internal nodes.
        computePSnegatives(formula, decomposition, map);
        return map;
    }

    private static void computePSbaseCases(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMap map) {
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            int vertex = node.object().iterator().next();
            if (vertex % 10 != 1 && vertex % 10 != 2) {
                throw new IllegalArgumentException("The decomposition is not of an incidence graph");
            }
            if (node.children().isEmpty()) {
                // child node, base case
                List<Formula> formulas = inducedFormulas(formula, node);
                // F_v is of the form {{x},...,{x},{-x},...{-x}} with two PS sets: {{x},...,{x}} and {{-x},...,{-x}}.
                // It might also contain {x,x} and the like; as well as {x,-x} which are always satisfied.
                Set<Clause> onePs = new HashSet<>();
                Set<Clause> twoPs = new HashSet<>();
                for (Clause clause : formulas.get(0).clauses()) {
                    // TODO: if empty?
                    // If the clause has only x, add it to onePs. If it only has -x, add it to twoPs.
                    // If the clause has both x and -x, add it to both.
                    boolean containsPositive = false, containsNegative = false;
                    for (int literal : clause.literals()) {
                        if (literal > 0) {
                            containsPositive = true;
                        } else if (literal < 0) {
                            containsNegative = true;
                        }
                    }
                    if (containsPositive && containsNegative) {
                        onePs.add(clause);
                        twoPs.add(clause);
                    } else if (containsPositive) {
                        onePs.add(clause);
                    } else if (containsNegative) {
                        twoPs.add(clause);
                    }
                }
                map.addToPositive(node, onePs);
                map.addToPositive(node, twoPs);
                // F_-v is of the form {{x_1,x_2,...,-x_k,...,-x_n}} with one PS set: the clause itself
                Set<Clause> ps = new HashSet<>();
                if (!formulas.get(1).clauses().isEmpty()) {
                    ps.add(formulas.get(1).clauses().get(0));
                } else {
                    ps.add(new Clause());
                }
                map.addToNegative(node, ps);
            } else if (node.parent() == null) {
                // root node, base case
                List<Formula> inducedFormulas = inducedFormulas(formula, node);
                // at the root node, F_v is an empty formula without any clauses, and F_-v is a formula with empty clauses.
                map.addToPositive(node, Set.of());
                map.addToNegative(node, new HashSet<>(inducedFormulas.get(1).clauses()));
            }
        }
    }

    private static void computePSpositives(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMap map) {
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            if (node.parent() != null && !node.children().isEmpty()) {
                // internal node. for positive: both children
                Iterator<TreeNode<Set<Integer>>> childIterator = node.children().iterator();
                TreeNode<Set<Integer>> c1 = childIterator.next();
                TreeNode<Set<Integer>> c2 = childIterator.next();
                // reduction
                Set<Clause> deltaClauses = new HashSet<>();
                for (Integer vertex : node.object()) {
                    if (vertex % 10 == 2) {
                        deltaClauses.add(formula.clauses().get(vertex/10 - 1));
                    }
                }
                Set<Set<Clause>> l = new HashSet<>();
                for (Set<Clause> clauses1 : map.getPositive(c1)) {
                    for (Set<Clause> clauses2 : map.getPositive(c2)) {
                        Set<Clause> newClauses = new HashSet<>(clauses1);
                        newClauses.addAll(clauses2);
                        newClauses.removeAll(deltaClauses);
                        l.add(newClauses);
                    }
                }
                map.setPositive(node, l);
            }
        }
    }

    private static void computePSnegatives(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMap map) {
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.breadthIterator();
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            if (node.parent() != null && !node.children().isEmpty()) {
                // parent and sibling
                TreeNode<Set<Integer>> p = node.parent();
                TreeNode<Set<Integer>> s = null;
                for (TreeNode<Set<Integer>> pChild : p.children()) {
                    if (pChild != node) {
                        s = pChild;
                    }
                }
                // Reduction
                Set<Clause> deltaClauses = new HashSet<>();
                for (Integer vertex : node.object()) {
                    if (vertex % 10 == 2) {
                        deltaClauses.add(formula.clauses().get(vertex/10 - 1));
                    }
                }
                Set<Set<Clause>> l = new HashSet<>();
                for (Set<Clause> clauses1 : map.getPositive(s)) {
                    for (Set<Clause> clauses2 : map.getNegative(p)) {
                        Set<Clause> newClauses = new HashSet<>(clauses1);
                        newClauses.addAll(clauses2);
                        newClauses.removeAll(deltaClauses);
                        l.add(newClauses);
                    }
                }
                map.setNegative(node, l);
            }
        }
    }

    /**
     * Computes the two induced formulas F_v and F_-v.
     * @param formula the original formula.
     * @param node the node v.
     * @return a list of two formulas, F_v and F_-v.
     */
    private static List<Formula> inducedFormulas(Formula formula, TreeNode<Set<Integer>> node) {
        // F_v:  induced by clauses in cla(F) \ delta(node), and variables in delta(node)
        // F_-v: induced by clauses in delta(node), and variables in var(F) \ delta(node)
        Formula positive = new Formula();
        Formula negative = new Formula();
        Set<Integer> delta = node.object();
        for (int i = 1; i <= formula.clauses().size(); i++) {
            int vertex = 10*i + 2;
            Clause clause = formula.clauses().get(i - 1);
            if (delta.contains(vertex)) {
                negative.addClause(new Clause(clause));
            } else {
                positive.addClause(new Clause(clause));
            }
        }
        // In 'positive', we only leave variables that appear in delta.
        for (Clause clause : positive.clauses()) {
            Set<Integer> removing = new HashSet<>();
            for (int literal : clause.literals()) {
                int vertex = 10*Math.abs(literal) + 1;
                if (!delta.contains(vertex)) {
                    removing.add(literal);
                }
            }
            for (int i = clause.literals().size() - 1; i >= 0; i--) {
                int literal = clause.literals().get(i);
                if (removing.contains(literal)) {
                    clause.literals().remove(i);
                }
            }
        }
        // In 'negative', we only leave variables that do not appear in delta.
        for (Clause clause : negative.clauses()) {
            Set<Integer> removing = new HashSet<>();
            for (int literal : clause.literals()) {
                int vertex = 10*Math.abs(literal) + 1;
                if (delta.contains(vertex)) {
                    removing.add(literal);
                }
            }
            for (int i = clause.literals().size() - 1; i >= 0; i--) {
                int literal = clause.literals().get(i);
                if (removing.contains(literal)) {
                    clause.literals().remove(i);
                }
            }
        }
        return List.of(positive, negative);
    }

}
