package at.ac.tuwien.student.e11843614.sharpsat;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.decomposition.DecompositionFactory;
import at.ac.tuwien.student.e11843614.decomposition.DerivationFactory;
import at.ac.tuwien.student.e11843614.decomposition.carving.CarvingDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.CliqueDerivation;
import at.ac.tuwien.student.e11843614.decomposition.clique.operation.CliqueOperation;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.graph.GraphFactory;
import at.ac.tuwien.student.e11843614.struct.tree.TreeNode;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

public abstract class ModelCounting {

    // TODO: special cases when derivation does not exist
    // TODO: dynamic algorithm for #SAT

    // utilizing psw
    public static int psw(Formula formula) throws TimeoutException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        CarvingDerivation derivation = DerivationFactory.carving(incidenceGraph);
        TreeNode<Set<Integer>> decomposition = DecompositionFactory.carving(derivation);
        Logger.warn("#SAT utilizing psw not yet implemented, returning 0");
        return 0;
    }

    // utilizing cw
    public static int cw(Formula formula) throws TimeoutException {
        Graph incidenceGraph = GraphFactory.incidenceGraph(formula);
        CliqueDerivation derivation = DerivationFactory.clique(incidenceGraph);
        TreeNode<CliqueOperation> decomposition = DecompositionFactory.clique(derivation, incidenceGraph);
        Logger.warn("#SAT utilizing cw not yet implemented, returning 0");
        return 0;
    }

}
