package at.ac.tuwien.student.e11843614.counting.psw;

import at.ac.tuwien.student.e11843614.Arguments;
import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class PSDynamicModelCounting {

    /**
     * Counts the models of a formula dynamically and returns the computed value.
     * @param formula the formula to count models of.
     * @param decomposition a branch decomposition (psw) of the formula.
     * @return the amount of models of the formula.
     */
    public static long count(Formula formula, TreeNode<Set<Integer>> decomposition) {
        PSSetMap psMap = computePSSets(formula, decomposition);
        PSTableMap tableMap = computeTables(decomposition, psMap);
        return tableMap.get(decomposition).get(Set.of(), Set.of());
    }

    // ----- Computing tables ------------------------------------------------------------------------------------------

    /**
     * Computes the tables for this decomposition.
     * @param decomposition the root node of the decomposition.
     * @param psMap the map of PS sets.
     * @return a map from nodes to PS tables, where the table at the root node, and at index ({},{}), stores the
     *         amount of models.
     */
    private static PSTableMap computeTables(TreeNode<Set<Integer>> decomposition, PSSetMap psMap) {
        StopWatch stopwatch = StopWatch.createStarted();
        PSTableMap tableMap = new PSTableMap();
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            Logger.debug("v = " + node.object() + ": computing the PS table");
            PSTable table = new PSTable();
            if (node.children().isEmpty()) {
                // leaf, base case
                computeTableBaseCase(node, psMap, table);
            } else {
                // internal node, reduction
                Iterator<TreeNode<Set<Integer>>> childIterator = node.children().iterator();
                TreeNode<Set<Integer>> child1 = childIterator.next();
                TreeNode<Set<Integer>> child2 = childIterator.next();
                PSTable child1Table = tableMap.get(child1);
                PSTable child2Table = tableMap.get(child2);
                computeTableReduction(node, psMap, table, child1Table, child2Table);
            }
            tableMap.set(node, table);
        }
        stopwatch.stop();
        Logger.debug("Computed all PS tables in time: " + stopwatch.formatTime());
        return tableMap;
    }

    private static void computeTableBaseCase(TreeNode<Set<Integer>> node, PSSetMap psMap, PSTable table) {
        int vertex = node.object().iterator().next();
        for (Set<Integer> c1 : psMap.getPositive(node)) {
            for (Set<Integer> c2 : psMap.getNegative(node)) {
                if (vertex % 10 == 1) {
                    // variable vertex
                    table.set(c1, c2, 1);
                } else {
                    // clause vertex
                    int clause = vertex / 10;
                    if (c2.contains(clause)) {
                        table.set(c1, c2, 1);
                    } else {
                        table.set(c1, c2, 0);
                    }
                }
            }
        }
    }

    private static void computeTableReduction(TreeNode<Set<Integer>> node, PSSetMap psMap, PSTable table,
                                              PSTable child1Table, PSTable child2Table) {
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
        for (Set<Integer> c1 : psMap.getPositive(child1)) {
            for (Set<Integer> c2 : psMap.getPositive(child2)) {
                for (Set<Integer> cv : psMap.getNegative(node)) {
                    Set<Integer> first = new HashSet<>(c2);
                    first.addAll(cv);
                    first.retainAll(deltaClauses(child1));
                    Set<Integer> second = new HashSet<>(c1);
                    second.addAll(cv);
                    second.retainAll(deltaClauses(child2));
                    Set<Integer> third = new HashSet<>(c1);
                    third.addAll(c2);
                    third.removeAll(deltaClauses(node));
                    long n = Math.addExact(
                        table.get(third, cv),
                        Math.multiplyExact(child1Table.get(c1, first), child2Table.get(c2, second))
                    );
                    table.set(third, cv, n);
                }
            }
        }
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
        Logger.debug("Computing PS sets");
        StopWatch stopwatch = StopWatch.createStarted();
        // First, we compute the sets for the base cases: root node and leaves.
        computePSBaseCases(formula, decomposition, map);
        Logger.debug("Computed PS sets for base cases");
        // Now, we compute the PS sets for F_v for internal nodes.
        computePSPositives(decomposition, map);
        Logger.debug("Computed PS(F_v) sets");
        // Finally, we compute the PS sets for F_-v for internal nodes.
        computePSNegatives(decomposition, map);
        stopwatch.stop();
        Logger.debug("Computed PS(F_-v) sets");
        Logger.debug("Computed all PS sets in time: " + stopwatch.formatTime());
        // Compute ps-width
        if (Arguments.verbose()) {
            int width = 0;
            for (TreeNode<Set<Integer>> node : decomposition) {
                int psValue = Math.max(map.getPositive(node).size(), map.getNegative(node).size());
                width = Math.max(width, psValue);
            }
            Logger.debug("ps-width of the decomposition is " + width);
        }
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
                // leaf node, base case
                List<Formula> formulas = inducedFormulas(formula, node); // 0 => F_v, 1 => F_-v
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
                } else {
                    // clause vertex
                    // F_v  = {{},{},...}
                    // F_-v = {c}
                    // PS(F_v) = {{}}
                    map.addToPositive(node, Set.of());
                    // PS(F-v) = {{}, {c}}
                    int clause = formulas.get(1).clauses().iterator().next().position();
                    map.addToNegative(node, Set.of());
                    map.addToNegative(node, Set.of(clause));
                }
                Logger.debug("v = " + node.object() + ": PS(F_v) = " + map.getPositive(node));
                Logger.debug("v = " + node.object() + ": PS(F_-v) = " + map.getNegative(node));
            } else if (node.parent() == null) {
                // root node, base case
                // at the root node, F_v is an empty formula without any clauses.
                map.addToPositive(node, Set.of());
                // And F_-v is a formula with empty clauses.
                map.addToNegative(node, Set.of());
                Logger.debug("v = " + node.object() + ": PS(F_v) = " + map.getPositive(node));
                Logger.debug("v = " + node.object() + ": PS(F_-v) = " + map.getNegative(node));
            }
        }
    }

    private static void computePSPositives(TreeNode<Set<Integer>> decomposition, PSSetMap map) {
        Iterator<TreeNode<Set<Integer>>> iterator = decomposition.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<Set<Integer>> node = iterator.next();
            if (!node.children().isEmpty() && node.parent() != null) {
                // internal node. for positive: both children
                Iterator<TreeNode<Set<Integer>>> childIterator = node.children().iterator();
                TreeNode<Set<Integer>> c1 = childIterator.next();
                TreeNode<Set<Integer>> c2 = childIterator.next();
                // reduction
                Set<Integer> deltaClauses = deltaClauses(node);
                Set<Set<Integer>> l = new HashSet<>();
                for (Set<Integer> clauses1 : map.getPositive(c1)) {
                    for (Set<Integer> clauses2 : map.getPositive(c2)) {
                        Set<Integer> newClauses = new HashSet<>(clauses1);
                        newClauses.addAll(clauses2);
                        newClauses.removeAll(deltaClauses);
                        l.add(newClauses);
                    }
                }
                map.setPositive(node, l);
                Logger.debug("v = " + node.object() + ": PS(F_v) = " + map.getPositive(node));
            }
        }
    }

    private static void computePSNegatives(TreeNode<Set<Integer>> decomposition, PSSetMap map) {
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
                Set<Integer> deltaClauses = deltaClauses(node);
                Set<Set<Integer>> l = new HashSet<>();
                for (Set<Integer> clauses1 : map.getPositive(s)) {
                    for (Set<Integer> clauses2 : map.getNegative(p)) {
                        Set<Integer> newClauses = new HashSet<>(clauses1);
                        newClauses.addAll(clauses2);
                        newClauses.retainAll(deltaClauses);
                        l.add(newClauses);
                    }
                }
                map.setNegative(node, l);
                Logger.debug("v = " + node.object() + ": PS(F_-v) = " + map.getNegative(node));
            }
        }
    }

    // ----- Helpers ---------------------------------------------------------------------------------------------------

    /**
     * Computes the set of clauses in delta.
     * @param node the node.
     * @return the set cla(delta(node)).
     */
    private static Set<Integer> deltaClauses(TreeNode<Set<Integer>> node) {
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

    /**
     * Outputs memory usage at the point of call to the console.
     * Call this method where necessary and recompile to see output.
     */
    private static void debugMemoryUsage() {
        double memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0;
        double gib = memory / 1000000;
        Logger.debug("[psw] Memory usage: " + gib + " GiB");
    }

}
