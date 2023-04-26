package at.ac.tuwien.student.e11843614.sharpsat;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.graph.GraphFactory;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

public abstract class ModelCounting {

    // TODO: dynamic algorithm for #SAT

    // utilizing psw
    public static int psw(Formula formula) throws TimeoutException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Compute a carving decomposition
        TreeNode<Set<Integer>> decomposition = DecompositionFactory.carving(incidenceGraph);
        // Transform it into a branch decomposition as defined in the psw paper (binary tree)
        DecompositionFactory.transformCarvingIntoBranch(decomposition);
        // Solve #SAT
        Logger.warn("#SAT utilizing psw not yet implemented, returning 0");
        return 0;
    }

    // utilizing cw
    public static int cw(Formula formula) throws TimeoutException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        // Compute a parse tree for clique width
        TreeNode<CliqueOperation> decomposition = DecompositionFactory.clique(incidenceGraph);
        CliqueDecompositionFactory.makeDisjointColorSets(decomposition);
        // Solve #SAT
        Logger.warn("#SAT utilizing cw not yet implemented, returning 0");
        return 0;
    }

}
