package at.ac.tuwien.student.e11843614.sharpsat.psw;

import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An object that maps a node v to PS(F_v) and PS(F_-v).
 */
public class PSSetMap {

    private final Map<TreeNode<Set<Integer>>, Set<Set<Clause>>> positives = new HashMap<>();
    private final Map<TreeNode<Set<Integer>>, Set<Set<Clause>>> negatives = new HashMap<>();

    /**
     * Retrieves the set PS(F_v) for a node v.
     * @param node the node v.
     * @return PS(F_v), a set of precisely satisfiable clauses of F_v.
     */
    public Set<Set<Clause>> getPositive(TreeNode<Set<Integer>> node) {
        return positives.get(node);
    }

    /**
     * Retrieves the set PS(F_-v) for a node v.
     * @param node the node v.
     * @return PS(F_-v), a set of precisely satisfiable clauses of F_-v.
     */
    public Set<Set<Clause>> getNegative(TreeNode<Set<Integer>> node) {
        return negatives.get(node);
    }

    public void setPositive(TreeNode<Set<Integer>> node, Set<Set<Clause>> set) {
        if (!positives.containsKey(node)) {
            positives.put(node, new HashSet<>());
        }
        positives.put(node, set);
    }

    public void setNegative(TreeNode<Set<Integer>> node, Set<Set<Clause>> set) {
        if (!negatives.containsKey(node)) {
            negatives.put(node, new HashSet<>());
        }
        negatives.put(node, set);
    }

    public void addToPositive(TreeNode<Set<Integer>> node, Set<Clause> set) {
        if (!positives.containsKey(node)) {
            positives.put(node, new HashSet<>());
        }
        positives.get(node).add(set);
    }

    public void addToNegative(TreeNode<Set<Integer>> node, Set<Clause> set) {
        if (!negatives.containsKey(node)) {
            negatives.put(node, new HashSet<>());
        }
        negatives.get(node).add(set);
    }

}
