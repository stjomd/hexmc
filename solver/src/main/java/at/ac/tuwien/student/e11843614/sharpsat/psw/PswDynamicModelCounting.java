package at.ac.tuwien.student.e11843614.sharpsat.psw;

import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class PswDynamicModelCounting {

    public static int count(Formula formula, TreeNode<Set<Integer>> decomposition) {
        int width = 0;
        // Compute PS sets
        PSSetMap psMap = computePSSets(formula, decomposition);
        // Compute tables
        Map<TreeNode<Set<Integer>>, PSTable> tableMap = new HashMap<>();
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        System.out.println("PS VALUES");
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            System.out.println("\nvisiting: " + node.object());
            PSTable table = new PSTable();
            if (node.children().isEmpty()) {
                // leaf, base case
                for (Set<Clause> c1 : psMap.getPositive(node)) {
                    for (Set<Clause> c2 : psMap.getNegative(node)) {
                        // TODO: 0,1,2?
                        table.set(c1, c2, 0);
                    }
                }
                List<Formula> indf = inducedFormulas(formula, node);
                System.out.println("F_v:  " + indf.get(0));
                System.out.println("F_-v: " + indf.get(1));
                System.out.println("PS(F_v):");
                for (Set<Clause> psset : psMap.getPositive(node)) {
                    System.out.println("\t" + psset);
                }
                System.out.println("PS(F_-v):");
                for (Set<Clause> psset : psMap.getNegative(node)) {
                    System.out.println("\t" + psset);
                }
            } else {
                List<Formula> indf = inducedFormulas(formula, node);
                System.out.println("F_v:  " + indf.get(0));
                System.out.println("F_-v: " + indf.get(1));
                System.out.println("PS(F_v):");
                for (Set<Clause> psset : psMap.getPositive(node)) {
                    System.out.println("\t" + psset);
                }
                System.out.println("PS(F_-v):");
                for (Set<Clause> psset : psMap.getNegative(node)) {
                    System.out.println("\t" + psset);
                }
                // internal node, reduction
                // initialize to 0
                for (Set<Clause> c1 : psMap.getPositive(node)) {
                    for (Set<Clause> c2 : psMap.getNegative(node)) {
                        table.set(c1, c2, 0);
                    }
                }
                // fill the table
                Iterator<TreeNode<Set<Integer>>> childIterator = node.children().iterator();
                TreeNode<Set<Integer>> child1 = childIterator.next();
                TreeNode<Set<Integer>> child2 = childIterator.next();
                PSTable child1Table = tableMap.get(child1);
                PSTable child2Table = tableMap.get(child2);
                for (Set<Clause> c1 : psMap.getPositive(child1)) {
                    for (Set<Clause> c2 : psMap.getPositive(child2)) {
                        for (Set<Clause> cv : psMap.getNegative(node)) {
                            Set<Clause> first = new HashSet<>(c2);
                            first.addAll(cv);
                            first.retainAll(deltaClauses(child1, formula));
                            Set<Clause> second = new HashSet<>(c1);
                            second.addAll(cv);
                            second.retainAll(deltaClauses(child2, formula));
                            Set<Clause> third = new HashSet<>(c1);
                            third.addAll(c2);
                            third.removeAll(deltaClauses(node, formula));
                            int n = table.get(third, cv) + child1Table.get(c1, first) + child2Table.get(c2, second);
                            table.set(third, cv, n);
                        }
                    }
                }
            }
            tableMap.put(node, table);
        }
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
        computePSBaseCases(formula, decomposition, map);
        // Now, we compute the PS sets for F_v for internal nodes.
        computePSPositives(formula, decomposition, map);
        // Finally, we compute the PS sets for F_-v for internal nodes.
        computePSNegatives(formula, decomposition, map);
        return map;
    }

    private static void computePSBaseCases(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMap map) {
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            int vertex = node.object().iterator().next();
            if (vertex % 10 != 1 && vertex % 10 != 2) {
                throw new IllegalArgumentException("The decomposition is not of an incidence graph");
            }
            if (node.children().isEmpty()) {
                System.out.println("\nvisiting: " + node.object());
                // child node, base case
                List<Formula> formulas = inducedFormulas(formula, node); // 0 => F_v, 1 => F_-v
                System.out.println("F:    " + formula);
                System.out.println("F_v:  " + formulas.get(0));
                System.out.println("F_-v: " + formulas.get(1));
                if (vertex % 10 == 1) {
                    // variable vertex
                    // F_v  = {{x},...,{x,x},...,{-x},...,{-x,-x},...,{x,-x},...}
                    // F_-v = {}
                    Set<Clause> ps1 = new HashSet<>();
                    Set<Clause> ps2 = new HashSet<>();
                    // PS(F_v) contains {{x},...,{x,x},...,{x,-x},...} and {{-x},...,{-x,-x},...,{x,-x},...}
                    for (Clause clause : formulas.get(0).clauses()) {
                        // Check which literals the clause contains
                        boolean hasPositiveLiterals = false, hasNegatedLiterals = false;
                        for (int literal : clause.literals()) {
                            if (literal > 0) {
                                hasPositiveLiterals = true;
                            } else {
                                hasNegatedLiterals = true;
                            }
                        }
                        // both {x,-x} => ps1 & ps2; {x,...} => ps1; {-x,...} => ps2
                        if (hasPositiveLiterals) {
                            ps1.add(clause);
                        }
                        if (hasNegatedLiterals) {
                            ps2.add(clause);
                        }
                    }
                    map.addToPositive(node, ps1);
                    map.addToPositive(node, ps2);
                    // PS(F_-v) = {}
                    map.setNegative(node, Set.of());
                    System.out.println("PS(F_v):  " + map.getPositive(node));
                    System.out.println("PS(F_-v): " + map.getNegative(node));
                } else {
                    // clause vertex
                    // F_v  = {{},{},...}
                    // F_-v = {c}
                    // PS(F_v) = {F_v}
                    Set<Clause> clauses = new HashSet<>(formulas.get(0).clauses());
                    map.addToPositive(node, clauses);
                    // PS(F-v) = {{c}}
                    Clause clause = formulas.get(1).clauses().iterator().next();
                    map.addToNegative(node, Set.of(clause));
                    map.addToNegative(node, Set.of());
                    System.out.println("PS(F_v):  " + map.getPositive(node));
                    System.out.println("PS(F_-v): " + map.getNegative(node));
                }
            } else if (node.parent() == null) {
                // root node, base case
                List<Formula> inducedFormulas = inducedFormulas(formula, node);
                // at the root node, F_v is an empty formula without any clauses, and F_-v is a formula with empty clauses.
                map.addToPositive(node, Set.of());
                map.addToNegative(node, new HashSet<>(inducedFormulas.get(1).clauses()));
            }
        }
    }

    private static void computePSPositives(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMap map) {
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            if (node.parent() != null && !node.children().isEmpty()) {
                System.out.println("\nvisiting: " + node.object());
                // internal node. for positive: both children
                Iterator<TreeNode<Set<Integer>>> childIterator = node.children().iterator();
                TreeNode<Set<Integer>> c1 = childIterator.next();
                TreeNode<Set<Integer>> c2 = childIterator.next();
                System.out.println("c1: " + c1.object());
                System.out.println("c2: " + c2.object());
                // reduction
                Set<Clause> deltaClauses = new HashSet<>();
                for (Integer vertex : node.object()) {
                    if (vertex % 10 == 2) {
                        deltaClauses.add(formula.clauses().get(vertex/10 - 1));
                    }
                }
                System.out.println("PS(F_c1): " + map.getPositive(c1));
                System.out.println("PS(F_c2): " + map.getPositive(c2));
                Set<Set<Clause>> l = new HashSet<>();
                for (Set<Clause> clauses1 : map.getPositive(c1)) {
                    for (Set<Clause> clauses2 : map.getPositive(c2)) {
                        Set<Clause> newClauses = new HashSet<>(clauses1);
                        newClauses.addAll(clauses2);
                        newClauses.removeAll(deltaClauses);
                        l.add(newClauses);
                        System.out.println("C1 = " + clauses1 + ", C2 = " + clauses2);
                        System.out.println("cla delta = " + deltaClauses);
                        System.out.println("new = " + newClauses);
                    }
                }
                System.out.println("l: " + l);
                map.setPositive(node, l);
            }
        }
    }

    private static void computePSNegatives(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMap map) {
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

    // ----- Helpers ---------------------------------------------------------------------------------------------------

    private static Set<Integer> deltaVariables(TreeNode<Set<Integer>> node) {
        Set<Integer> vars = new HashSet<>();
        for (int vertex : node.object()) {
            if (vertex % 10 == 1) {
                vars.add(vertex / 10);
            }
        }
        return vars;
    }

    private static Set<Clause> deltaClauses(TreeNode<Set<Integer>> node, Formula formula) {
        Set<Clause> clauses = new HashSet<>();
        for (int vertex : node.object()) {
            if (vertex % 10 == 2) {
                Clause clause = formula.clauses().get(vertex/10 - 1);
                clauses.add(clause);
            }
        }
        return clauses;
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
