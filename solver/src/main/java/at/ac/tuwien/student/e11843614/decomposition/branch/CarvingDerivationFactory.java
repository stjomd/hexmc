package at.ac.tuwien.student.e11843614.decomposition.branch;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.SATSolver;
import at.ac.tuwien.student.e11843614.sat.Variable;
import at.ac.tuwien.student.e11843614.sat.factory.SATEncodingFactory;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import org.sat4j.specs.TimeoutException;

import java.util.Set;

/**
 * A class that tries to construct a carving derivation until it satisfies all conditions.
 */
public abstract class CarvingDerivationFactory {

    // TODO: This class should not be necessary, but I could not figure out the issue.

    private static final int ATTEMPTS = 10;

    /**
     * A functional interface for lambda expressions initializing and returning a Graph.
     */
    public interface GraphConstructor {
        Graph construct();
    }

    /**
     * Repeatedly attempts to construct a carving derivation of a graph until it satisfies all conditions.
     * @param w the target carving-width
     * @param constructor a lambda expression that initializes and returns a graph.
     * @return a carving derivation of width <= w, or null if a) such doesn't exist, or b) took too many attempts.
     * @throws TimeoutException if the SAT solver takes too long.
     */
    public static CarvingDerivation carving(int w, GraphConstructor constructor) throws TimeoutException {
        Logger.debug("Attempting to construct a valid carving derivation");
        int tries = 1;
        while (tries <= ATTEMPTS) {
            Graph graph = constructor.construct();
            SATEncoding encoding = SATEncodingFactory.forCarvingWidth(graph, w);
            Set<Variable> truth = SATSolver.getSatisfyingAssignment(encoding);
            if (truth.isEmpty()) {
                return null;
            }
            // Derivation does not fulfil D3 sometimes.
            CarvingDerivation derivation = new CarvingDerivation(truth, encoding);
            if (derivation.fulfilsConditions(graph)) {
                Logger.debug("Constructed a valid carving derivation on try " + tries);
                return derivation;
            } else {
                Logger.warn("Obtained an invalid carving derivation. Retrying...");
            }
            tries++;
        }
        Logger.warn("Exceeded attempts limit");
        return null;
    }

}
