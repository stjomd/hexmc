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
import java.util.stream.Collectors;

public abstract class PswDynamicModelCounting {

    public static int count(Formula formula, TreeNode<Set<Integer>> decomposition) {
        int width = 0;
        for (Clause clause : formula.clauses()) {
            System.out.println(clause + " at pos " + clause.position());
        }
        // Compute PS sets
        PSSetMapRefs psMap = computePSSets(formula, decomposition);
        // Compute tables
        Map<TreeNode<Set<Integer>>, PSTableRefs> tableMap = new HashMap<>();
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        System.out.println("PS VALUES");
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            System.out.println("\nvisiting: " + node.object());
            PSTableRefs table = new PSTableRefs();
            if (node.children().isEmpty()) {
                // leaf, base case
                for (Set<Integer> c1 : psMap.getPositive(node)) {
                    for (Set<Integer> c2 : psMap.getNegative(node)) {
                        // TODO: 0,1,2?
                        table.set(c1, c2, 0);
                    }
                }
                List<Formula> indf = inducedFormulas(formula, node);
                System.out.println("F_v:  " + indf.get(0));
                System.out.println("F_-v: " + indf.get(1));
                System.out.println("PS(F_v):");
                for (Set<Integer> psset : psMap.getPositive(node)) {
                    System.out.println("\t" + psset);
                }
                System.out.println("PS(F_-v):");
                for (Set<Integer> psset : psMap.getNegative(node)) {
                    System.out.println("\t" + psset);
                }
            } else {
                List<Formula> indf = inducedFormulas(formula, node);
                System.out.println("F_v:  " + indf.get(0));
                System.out.println("F_-v: " + indf.get(1));
                System.out.println("PS(F_v):");
                for (Set<Integer> psset : psMap.getPositive(node)) {
                    System.out.println("\t" + psset);
                }
                System.out.println("PS(F_-v):");
                for (Set<Integer> psset : psMap.getNegative(node)) {
                    System.out.println("\t" + psset);
                }
                // internal node, reduction
                // initialize to 0
                for (Set<Integer> c1 : psMap.getPositive(node)) {
                    for (Set<Integer> c2 : psMap.getNegative(node)) {
                        table.set(c1, c2, 0);
                    }
                }
                // fill the table
                Iterator<TreeNode<Set<Integer>>> childIterator = node.children().iterator();
                TreeNode<Set<Integer>> child1 = childIterator.next();
                TreeNode<Set<Integer>> child2 = childIterator.next();
                PSTableRefs child1Table = tableMap.get(child1);
                PSTableRefs child2Table = tableMap.get(child2);
                for (Set<Integer> c1 : psMap.getPositive(child1)) {
                    for (Set<Integer> c2 : psMap.getPositive(child2)) {
                        for (Set<Integer> cv : psMap.getNegative(node)) {
                            Set<Integer> first = new HashSet<>(c2);
                            first.addAll(cv);
                            first.retainAll(deltaClauses(child1, formula));
                            Set<Integer> second = new HashSet<>(c1);
                            second.addAll(cv);
                            second.retainAll(deltaClauses(child2, formula));
                            Set<Integer> third = new HashSet<>(c1);
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
    private static PSSetMapRefs computePSSets(Formula formula, TreeNode<Set<Integer>> decomposition) {
        PSSetMapRefs map = new PSSetMapRefs();
        // First, we compute the sets for the base cases: root node and leaves.
        computePSBaseCases(formula, decomposition, map);
        // Now, we compute the PS sets for F_v for internal nodes.
        computePSPositives(formula, decomposition, map);
        // Finally, we compute the PS sets for F_-v for internal nodes.
        computePSNegatives(formula, decomposition, map);
        return map;
    }

    private static void computePSBaseCases(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMapRefs map) {
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
                for (Clause clause : formulas.get(0).clauses()) {
                    System.out.println("\t" + clause + " at pos " + clause.position());
                }
                System.out.println("F_-v: " + formulas.get(1));
                for (Clause clause : formulas.get(1).clauses()) {
                    System.out.println("\t" + clause + " at pos " + clause.position());
                }
                if (vertex % 10 == 1) {
                    // variable vertex
                    // F_v  = {{x},...,{x,x},...,{-x},...,{-x,-x},...,{x,-x},...}
                    // F_-v = {}
                    Set<Integer> ps1 = new HashSet<>();
                    Set<Integer> ps2 = new HashSet<>();
                    // PS(F_v) contains {{x},...,{x,x},...,{x,-x},...} and {{-x},...,{-x,-x},...,{x,-x},...}
                    for (int i = 0; i < formulas.get(0).clauses().size(); i++) {
                        Clause clause = formulas.get(0).clauses().get(i);
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
                            ps1.add(clause.position());
                        }
                        if (hasNegatedLiterals) {
                            ps2.add(clause.position());
                        }
                    }
                    map.addToPositive(node, ps1);
                    map.addToPositive(node, ps2);
                    // PS(F_-v) = {{}}
                    map.addToNegative(node, Set.of());
                    System.out.println("PS(F_v):  " + map.getPositive(node));
                    System.out.println("PS(F_-v): " + map.getNegative(node));
                } else {
                    // clause vertex
                    // F_v  = {{},{},...}
                    // F_-v = {c}
                    // PS(F_v) = {F_v}
                    Set<Integer> clauses = formulas.get(0).clauses().stream()
                        .map(Clause::position)
                        .collect(Collectors.toSet());
                    map.addToPositive(node, Set.of());
                    // PS(F-v) = {{c}}
                    int clause = formulas.get(1).clauses().iterator().next().position();
                    map.addToNegative(node, Set.of());
                    map.addToNegative(node, Set.of(clause));
                    System.out.println("PS(F_v):  " + map.getPositive(node));
                    System.out.println("PS(F_-v): " + map.getNegative(node));
                }
            } else if (node.parent() == null) {
                // root node, base case
                List<Formula> inducedFormulas = inducedFormulas(formula, node);
                // at the root node, F_v is an empty formula without any clauses.
                map.addToPositive(node, Set.of());
                // And F_-v is a formula with empty clauses.
                Set<Integer> clauses = inducedFormulas.get(1).clauses().stream()
                    .map(Clause::position)
                    .collect(Collectors.toSet());
                map.addToNegative(node, clauses);
            }
        }
    }

    private static void computePSPositives(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMapRefs map) {
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            if (!node.children().isEmpty()) {
                System.out.println("\nvisiting: " + node.object());
                // internal node. for positive: both children
                Iterator<TreeNode<Set<Integer>>> childIterator = node.children().iterator();
                TreeNode<Set<Integer>> c1 = childIterator.next();
                TreeNode<Set<Integer>> c2 = childIterator.next();
                System.out.println("c1: " + c1.object());
                System.out.println("c2: " + c2.object());
                // reduction
                Set<Integer> deltaClauses = new HashSet<>();
                for (Integer vertex : node.object()) {
                    if (vertex % 10 == 2) {
                        deltaClauses.add(vertex / 10);
                    }
                }
                System.out.println("PS(F_c1): " + map.getPositive(c1));
                System.out.println("PS(F_c2): " + map.getPositive(c2));
                Set<Set<Integer>> l = new HashSet<>();
                for (Set<Integer> clauses1 : map.getPositive(c1)) {
                    for (Set<Integer> clauses2 : map.getPositive(c2)) {
                        Set<Integer> newClauses = new HashSet<>(clauses1);
                        newClauses.addAll(clauses2);
                        newClauses.removeAll(deltaClauses);
//                        if (!newClauses.isEmpty()) {
                            l.add(newClauses);
//                        }
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

    private static void computePSNegatives(Formula formula, TreeNode<Set<Integer>> decomposition, PSSetMapRefs map) {
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
                Set<Integer> deltaClauses = new HashSet<>();
                for (Integer vertex : node.object()) {
                    if (vertex % 10 == 2) {
                        deltaClauses.add(vertex / 10);
                    }
                }
                Set<Set<Integer>> l = new HashSet<>();
                for (Set<Integer> clauses1 : map.getPositive(s)) {
                    for (Set<Integer> clauses2 : map.getNegative(p)) {
                        Set<Integer> newClauses = new HashSet<>(clauses1);
                        newClauses.addAll(clauses2);
                        newClauses.removeAll(deltaClauses);
                        l.add(newClauses);
                    }
                }
                map.setNegative(node, l);
            }
        }
        System.out.println("computed ps negatives");
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

    private static Set<Integer> deltaClauses(TreeNode<Set<Integer>> node, Formula formula) {
        Set<Integer> clauses = new HashSet<>();
        for (int vertex : node.object()) {
            if (vertex % 10 == 2) {
                clauses.add(vertex / 10);
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
            Clause newClause = new Clause(clause);
            if (delta.contains(vertex)) {
                negative.addClause(newClause);
            } else {
                positive.addClause(newClause);
            }
            newClause.setPosition(clause.position());
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
