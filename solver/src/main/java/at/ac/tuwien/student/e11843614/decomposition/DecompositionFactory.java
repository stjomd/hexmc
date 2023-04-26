package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;

import java.util.Set;

public abstract class DecompositionFactory {

    /**
     * Constructs a carving decomposition from a derivation.
     * @param derivation the derivation.
     * @return a carving decomposition.
     */
    public static TreeNode<Set<Integer>> carving(CarvingDerivation derivation) {
        return CarvingDecompositionFactory.from(derivation);
    }

    /**
     * Construct a clique decomposition (parse tree for clique-width) from a derivation.
     * @param derivation the derivation.
     * @return a clique decomposition.
     */
    public static TreeNode<CliqueOperation> clique(CliqueDerivation derivation, Graph graph) {
        return CliqueDecompositionFactory.from(derivation, graph);
    }

}
