package at.ac.tuwien.student.e11843614.sharpsat.psw;

import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An object that maps a node to a PS table.
 */
public class PSTableMap {

    private final Map<TreeNode<Set<Integer>>, PSTable> map = new HashMap<>();

    /**
     * Returns the PS table mapped to a node.
     * @param node the node.
     * @return the PS table mapped to the node.
     */
    public PSTable get(TreeNode<Set<Integer>> node) {
        return map.get(node);
    }

    /**
     * Maps a node to a PS table.
     * @param node a node.
     * @param table the respective PS table.
     */
    public void set(TreeNode<Set<Integer>> node, PSTable table) {
        map.put(node, table);
    }

}
