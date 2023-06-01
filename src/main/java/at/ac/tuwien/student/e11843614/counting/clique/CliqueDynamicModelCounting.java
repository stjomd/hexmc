package at.ac.tuwien.student.e11843614.counting.clique;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueEdgeCreation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueRecoloring;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueSingleton;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueUnion;
import at.ac.tuwien.student.e11843614.struct.SubsetIterator;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CliqueDynamicModelCounting {

    // TODO: requires a signed parse tree on input

    /**
     * Runs a dynamic model counting algorithm for a propositional formula.
     * @param decomposition the clique decomposition of the incidence graph of a formula.
     * @return the amount of the formula's models.
     */
    public static int count(TreeNode<CliqueOperation> decomposition) {
        // Determine the clique-width (amount of colors in the decomposition)
        int k = 1;
        for (TreeNode<CliqueOperation> node : decomposition) {
            if (node.object() instanceof CliqueRecoloring) {
                CliqueRecoloring recoloring = (CliqueRecoloring) node.object();
                k = Math.max(k, Math.max(recoloring.from(), recoloring.to()));
            } else if (node.object() instanceof CliqueSingleton) {
                CliqueSingleton singleton = (CliqueSingleton) node.object();
                k = Math.max(k, singleton.color());
            }
        }
        // Map each vertex of the decomposition to a table
        Map<TreeNode<CliqueOperation>, CliqueTable> tableMap = new HashMap<>();
        // Go through all nodes in post-order fashion
        StopWatch stopwatch = StopWatch.createStarted();
        Iterator<TreeNode<CliqueOperation>> iterator = decomposition.depthIterator();
        while (iterator.hasNext()) {
            TreeNode<CliqueOperation> node = iterator.next();
            Logger.debug("v = " + node.object() + ": computing the clique table");
            CliqueTable table = null;
            if (node.object() instanceof CliqueSingleton) {
                CliqueSingleton singleton = (CliqueSingleton) node.object();
                table = baseCase(singleton, k);
            } else if (node.object() instanceof CliqueUnion) {
                Iterator<TreeNode<CliqueOperation>> iter = node.children().iterator();
                CliqueTable tableU = tableMap.get(iter.next());
                CliqueTable tableW = tableMap.get(iter.next());
                table = unionReduction(tableU, tableW, k);
            } else if (node.object() instanceof CliqueRecoloring) {
                CliqueRecoloring recoloring = (CliqueRecoloring) node.object();
                CliqueTable tableU = tableMap.get(node.children().iterator().next());
                table = recoloringReduction(recoloring, tableU, k);
            } else if (node.object() instanceof CliqueEdgeCreation) {
                // TODO: need to support positive and negative edge creation
                CliqueEdgeCreation edgeCreation = (CliqueEdgeCreation) node.object();
                CliqueTable tableU = tableMap.get(node.children().iterator().next());
                table = edgeCreationReduction(edgeCreation, tableU, k);
            }
            tableMap.put(node, table);
        }
        stopwatch.stop();
        Logger.debug("Computed all clique tables in time: " + stopwatch.formatTime());
        // At the end, the number of models is at the root node
        return tableMap.get(decomposition).get(Set.of(), Set.of(), Set.of());
    }

    /**
     * Computes a table for a singleton node.
     * @param singleton the singleton.
     * @param k the width of the decomposition.
     * @return the table.
     */
    private static CliqueTable baseCase(CliqueSingleton singleton, int k) {
        // Base case. (A,B,C) in the table mean the following. From the corresponding formula we remove clauses
        // colored with a in A. For every b of B, we add a disjunction to the formula, which contains variables
        // colored with b. For every c of C, we add a disjunction of the negations of variables colored with c.
        CliqueTable table = new CliqueTable();
        // Since we're dealing with singletons and incidence graphs, the corresponding formula is either a
        // single variable, or an empty clause. A single variable has one model, an empty clause zero.
        // The singleton is assigned a color, say r. This is how (A,B,C) will affect the corresponding formula.
        if (singleton.vertex() % 10 == 1) {
            // The singleton is a variable of the formula. A, B don't affect the amount of models. If C contains r, we
            // get an unsatisfiable formula with 0 models, otherwise it's unchanged.
            forEachSubset(k, (a, b, c) -> {
                if (c.contains(singleton.color())) {
                    // F = {{x},{-x}} => 0 models
                    table.set(a, b, c, 0);
                } else {
                    // F = {{x}} => 1 model
                    table.set(a, b, c, 1);
                }
            });
        } else if (singleton.vertex() % 10 == 2) {
            // The singleton is a clause. If A contains r, the clause is removed, and we get an empty
            // formula with inf models, otherwise it's unchanged. B, C do not affect the formula.
            forEachSubset(k, (a, b, c) -> {
                if (a.contains(singleton.color())) {
                    // F = {} => inf models
                    // TODO: unclear base case (infinite amount of models)
                    table.set(a, b, c, 1);
                } else {
                    // F = {{}} => 0 models
                    // TODO: if set to 0, the answer in the end is always 0
                    table.set(a, b, c, 0);
                }
            });
        } else {
            throw new IllegalArgumentException("The decomposition is not of an incidence graph");
        }
        return table;
    }

    /**
     * Computes a table for a union node.
     * @param tableU the table of one child node.
     * @param tableW the table of the other child node.
     * @param k the width of the decomposition.
     * @return the table.
     */
    private static CliqueTable unionReduction(CliqueTable tableU, CliqueTable tableW, int k) {
        CliqueTable table = new CliqueTable();
        forEachSubset(k, (a, b, c) -> {
            int x = tableU.get(a, b, c) * tableW.get(a, b, c);
            table.set(a, b, c, x);
        });
        return table;
    }

    /**
     * Computes a table for a recoloring node.
     * @param tableU the table of the child node.
     * @param k the width of the decomposition.
     * @return the table.
     */
    private static CliqueTable recoloringReduction(CliqueRecoloring recoloring, CliqueTable tableU, int k) {
        int i = recoloring.from(), j = recoloring.to();
        CliqueTable table = new CliqueTable();
        forEachSubset(k, (a, b, c) -> {
            if (b.contains(i) || c.contains(i)) {
                table.set(a, b, c, 0);
                return;
            }
            Set<Integer> aPrime = new HashSet<>(a);
            if (a.contains(j)) {
                aPrime.add(i);
            } else {
                aPrime.remove(i);
            }
            Set<Integer> b1 = new HashSet<>(b), b2 = b, b3 = new HashSet<>(b);
            b1.add(i); b1.remove(j); b3.add(i);
            Set<Integer> c1 = new HashSet<>(c), c2 = c, c3 = new HashSet<>(c);
            c1.add(i); c1.remove(j); c3.add(i);
            int x = 0;
            if (!b.contains(j) && !c.contains(j)) {
                x = tableU.get(aPrime, b, c);
            } else if (b.contains(j) && !c.contains(j)) {
                x = tableU.get(aPrime, b1, c) + tableU.get(aPrime, b2, c) - tableU.get(aPrime, b3, c);
            } else if (!b.contains(j) && c.contains(j)) {
                x = tableU.get(aPrime, b, c1) + tableU.get(aPrime, b, c2) - tableU.get(aPrime, b, c3);
            } else {
                x = tableU.get(aPrime, b1, c1) + tableU.get(aPrime, b1, c2) + tableU.get(aPrime, b2, c1)
                    + tableU.get(aPrime, b2, c2) - tableU.get(aPrime, b3, c1) - tableU.get(aPrime, b3, c2)
                    - tableU.get(aPrime, b1, c3) - tableU.get(aPrime, b2, c3) + tableU.get(aPrime, b3, c3);
            }
            table.set(a, b, c, x);
        });
        return table;
    }

    /**
     * Computes a table for an edge creation node.
     * @param tableU the table of the child node.
     * @param k the width of the decomposition.
     * @return the table.
     */
    private static CliqueTable edgeCreationReduction(CliqueEdgeCreation edgeCreation, CliqueTable tableU, int k) {
        // TODO: need to support positive and negative edge creation
        int i = edgeCreation.from(), j = edgeCreation.to();
        CliqueTable table = new CliqueTable();
        forEachSubset(k, (a, b, c) -> {
            int x = 0;
            if (a.contains(i)) {
                x = tableU.get(a, b, c);
            } else if (b.contains(j)) {
                Set<Integer> a1 = new HashSet<>(a);
                a1.add(i);
                x = tableU.get(a1, b, c);
            } else {
                Set<Integer> a1 = new HashSet<>(a), b1 = new HashSet<>(b);
                a1.add(i); b1.add(j);
                x = tableU.get(a, b, c) + tableU.get(a1, b1, c) - tableU.get(a, b1, c);
            }
            table.set(a, b, c, x);
        });
        return table;
    }

    /**
     * Iterates over each subset of (2^[1, ..., k])^3, and calls the specified lambda method.
     * @param k an integer.
     * @param operation a lambda method that accepts (a, b, c) in (2^[1, ..., k])^3 and returns void.
     */
    private static void forEachSubset(int k, TableLambda operation) {
        List<Integer> set = new ArrayList<>();
        for (int i = 1; i <= k; i++) {
            set.add(i);
        }
        Iterator<List<Integer>> aIterator = new SubsetIterator<>(set);
        while (aIterator.hasNext()) {
            Set<Integer> a = new HashSet<>(aIterator.next());
            Iterator<List<Integer>> bIterator = new SubsetIterator<>(set);
            while (bIterator.hasNext()) {
                Set<Integer> b = new HashSet<>(bIterator.next());
                Iterator<List<Integer>> cIterator = new SubsetIterator<>(set);
                while (cIterator.hasNext()) {
                    Set<Integer> c = new HashSet<>(cIterator.next());
                    operation.fill(a, b, c);
                }
            }
        }
    }

    /**
     * A functional interface for lambda methods that accept (a, b, c) in (2^[1, ..., k])^3, fill out a table, and return
     * void.
     */
    private interface TableLambda {
        void fill(Set<Integer> a, Set<Integer> b, Set<Integer> c);
    }

}
