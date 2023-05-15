package at.ac.tuwien.student.e11843614.sharpsat.psw;

import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An object that maps a node v to precisely satisfiable families PS(F_v) and PS(F_-v).
 */
public class PSSetMap {

    private final Map<TreeNode<Set<Integer>>, Set<Set<Integer>>> positives = new HashMap<>();
    private final Map<TreeNode<Set<Integer>>, Set<Set<Integer>>> negatives = new HashMap<>();

    /**
     * Retrieves the set PS(F_v) for a node v.
     * @param node the node v.
     * @return PS(F_v), a set of precisely satisfiable clauses of F_v.
     */
    public Set<Set<Integer>> getPositive(TreeNode<Set<Integer>> node) {
        return positives.get(node);
    }

    /**
     * Retrieves the set PS(F_-v) for a node v.
     * @param node the node v.
     * @return PS(F_-v), a set of precisely satisfiable clauses of F_-v.
     */
    public Set<Set<Integer>> getNegative(TreeNode<Set<Integer>> node) {
        return negatives.get(node);
    }

    /**
     * Sets PS(F_v) to a specified subset of clauses.
     * @param node the node v.
     * @param set the set PS(F_v).
     */
    public void setPositive(TreeNode<Set<Integer>> node, Set<Set<Integer>> set) {
        if (!positives.containsKey(node)) {
            positives.put(node, new HashSet<>());
        }
        positives.put(node, set);
    }

    /**
     * Sets PS(F_-v) to a specified subset of clauses.
     * @param node the node v.
     * @param set the set PS(F_-v).
     */
    public void setNegative(TreeNode<Set<Integer>> node, Set<Set<Integer>> set) {
        if (!negatives.containsKey(node)) {
            negatives.put(node, new HashSet<>());
        }
        negatives.put(node, set);
    }

    /**
     * Adds a subset of clauses to PS(F_v).
     * @param node the node v.
     * @param set the subset of clauses to be added to PS(F_v).
     */
    public void addToPositive(TreeNode<Set<Integer>> node, Set<Integer> set) {
        if (!positives.containsKey(node)) {
            positives.put(node, new HashSet<>());
        }
        positives.get(node).add(set);
    }

    /**
     * Adds a subset of clauses to PS(F_-v).
     * @param node the node v.
     * @param set the subset of clauses to be added to PS(F_-v).
     */
    public void addToNegative(TreeNode<Set<Integer>> node, Set<Integer> set) {
        if (!negatives.containsKey(node)) {
            negatives.put(node, new HashSet<>());
        }
        negatives.get(node).add(set);
    }

}
