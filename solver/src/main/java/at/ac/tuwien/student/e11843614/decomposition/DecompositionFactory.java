package at.ac.tuwien.student.e11843614.decomposition;

import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

public abstract class DecompositionFactory {

    /**
     * Constructs a carving decomposition of a graph.
     * @param graph the graph.
     * @return a carving decomposition, or null if it does not exist.
     */
    public static TreeNode<Set<Integer>> carving(Graph graph) throws TimeoutException {
        CarvingDerivation derivation = DerivationFactory.carving(graph);
        if (derivation != null) {
            return CarvingDecompositionFactory.from(derivation);
        } else {
            return null;
        }
    }

    /**
     * Construct a clique decomposition (parse tree for clique-width) of a graph.
     * @param graph the graph.
     * @return a clique decomposition, or null if it does not exist.
     */
    public static TreeNode<CliqueOperation> clique(Graph graph) throws TimeoutException {
        // There is no decomposition of the graph if there are no vertices
        if (graph.vertices().isEmpty()) {
            return null;
        }
        // The derivation factory will check starting with cw = 2. Check for cw = 1 here.
        // Clique-width is 1 if every component of the graph is a singleton.
        // TODO: ^^^
        CliqueDerivation derivation = DerivationFactory.clique(graph);
        return CliqueDecompositionFactory.from(derivation, graph);
    }

}
