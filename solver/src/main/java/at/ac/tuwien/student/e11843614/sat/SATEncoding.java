package at.ac.tuwien.student.e11843614.sat;

import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.graph.Graph;
import org.sat4j.specs.TimeoutException;

import java.util.HashSet;
import java.util.Set;

/**
 * A class that represents a SAT encoding for graph parameters, i.e. a formula and a mapping of its variables to
 * integers (for DIMACS CNF format).
 */
public class SATEncoding {

    private int variableCounter = 1;

    private final Formula formula = new Formula();
    private final Bijection<Variable, Integer> variableMap = new Bijection<>();

    private final Bijection<Integer, Integer> vertexMap = new Bijection<>();
    private final Bijection<Edge<Integer>, Integer> edgeMap = new Bijection<>();

    private SATEncoding() {}

    /**
     * Returns a SAT Encoding with a formula that is satisfiable iff the graph has branch-width at most w.
     * @param graph the graph to construct a SAT Encoding for.
     * @param w the target branch-width.
     * @return a SAT Encoding with a formula that is satisfiable iff bw(graph) <= w.
     */
    public static SATEncoding forBranchDecompositionOf(Graph<Integer> graph, int w) {
        SATEncoding encoding = new SATEncoding();
        encoding.encodeGraph(graph);
        int d = (int) (Math.floor(encoding.edgeMap.size() / 2.0)
            - Math.ceil(w / 2.0)
            + Math.ceil(Math.log(Math.floor(w / 2.0)) / Math.log(2))
        ); // TODO: weird values?
        encoding.buildFormulaForBranchDecomposition(w, d+1);
        return encoding;
    }

    // ----- Properties ------------------------------------------------------------------------------------------------

    /**
     * Returns the formula in this encoding.
     * @return a Formula.
     */
    public Formula getFormula() {
        return formula;
    }

    /**
     * Returns the bijection between the variables and their integer representations in the formula of this encoding.
     * @return a bijection between variables and integers.
     */
    public Bijection<Variable, Integer> getVariableMap() {
        return variableMap;
    }

    /**
     * Returns the bijection between the vertices and their integer representations.
     * @return a bijection between vertices and integers.
     */
    public Bijection<Integer, Integer> getVertexMap() {
        return vertexMap;
    }

    /**
     * Returns the bijection between the edges and their integer representations.
     * @return a bijection between edges and integers.
     */
    public Bijection<Edge<Integer>, Integer> getEdgeMap() {
        return edgeMap;
    }

    // ----- Solving ---------------------------------------------------------------------------------------------------

    /**
     * Runs a SAT solver on this SAT encoding and returns the set of variables assigned to true.
     * @return a set of true variables.
     * @throws TimeoutException if the SAT solver takes too long.
     */
    public Set<Variable> getModel() throws TimeoutException {
        int[] assignments = SATSolver.getModels(formula);
        Set<Variable> set = new HashSet<>();
        for (int assignment : assignments) {
            if (assignment > 0) {
                set.add(variableMap.getFromDomain(assignment));
            }
        }
        return set;
    }

    // ----- Encoding --------------------------------------------------------------------------------------------------

    /**
     * Maps an integer (>= 1) to a variable. If the variable has been mapped already, returns its existing mapping.
     * @param variable the variable to be encoded.
     * @return the integer (>= 1) this variable is mapped to.
     */
    private int encodeVariable(Variable variable) {
        Integer value = variableMap.getFromDestination(variable);
        if (value != null) {
            return value;
        }
        variableMap.put(variable, variableCounter);
        return variableCounter++;
    }

    /**
     * Maps the graph's vertices and edges to integers, beginning with 1 in each case.
     * @param graph the graph to encode vertices and edges of.
     */
    private void encodeGraph(Graph<Integer> graph) {
        int i = 1;
        for (Integer vertex : graph.getVertices()) {
            vertexMap.put(vertex, i);
            i++;
        }
        i = 1;
        for (Edge<Integer> edge : graph.getEdges()) {
            edgeMap.put(edge, i);
            i++;
        }
    }

    // ----- Formulas --------------------------------------------------------------------------------------------------

    /**
     * Constructs the formula that is satisfiable iff the graph has a branch decomposition of width at most w, and a
     * derivation of length at most d.
     * @param w the target branch-width.
     * @param d the target derivation length.
     */
    private void buildFormulaForBranchDecomposition(int w, int d) {
        // 1
        for (Integer e : edgeMap.getDestination()) {
            for (Integer f : edgeMap.getDestination()) {
                if (e < f) {
                    for (int i = 1; i <= d; i++) {
                        int var1 = encodeVariable(Variable.set(e, f, 0));
                        int var2 = encodeVariable(Variable.set(e, f, d));
                        int var3 = encodeVariable(Variable.set(e, f, i));
                        int var4 = encodeVariable(Variable.set(e, f, i + 1));
                        formula.addClause(-var1);
                        formula.addClause(var2);
                        formula.addClause(-var3, var4);
                    }
                }
            }
        }
        // 2
        for (Integer e : edgeMap.getDestination()) {
            for (Integer f : edgeMap.getDestination()) {
                for (Integer g : edgeMap.getDestination()) {
                    if (e < f && f < g) {
                        for (int i = 1; i <= d; i++) {
                            int var1 = encodeVariable(Variable.set(e, f, i));
                            int var2 = encodeVariable(Variable.set(e, g, i));
                            int var3 = encodeVariable(Variable.set(f, g, i));
                            formula.addClause(-var1, -var2, var3);
                            formula.addClause(-var1, -var3, var2);
                            formula.addClause(-var2, -var3, var1);
                        }
                    }
                }
            }
        }
        // 3
        for (Integer e : edgeMap.getDestination()) {
            for (int i = 1; i <= d; i++) {
                // Left side
                Clause clause = new Clause();
                int var1 = encodeVariable(Variable.leader(e, i));
                clause.addLiteral(var1);
                for (Integer f : edgeMap.getDestination()) {
                    if (f < e) {
                        int var2 = encodeVariable(Variable.set(f, e, i));
                        clause.addLiteral(var2);
                    }
                }
                formula.addClause(clause);
                // Right side
                for (Integer f : edgeMap.getDestination()) {
                    if (f < e) {
                        int var2 = encodeVariable(Variable.set(f, e, i));
                        formula.addClause(-var1, -var2);
                    }
                }
            }
        }
        // 4
        for (Integer e : edgeMap.getDestination()) {
            for (Integer f : edgeMap.getDestination()) {
                if (e < f) {
                    for (int i = 1; i < d - 1; i++) {
                        int var1 = encodeVariable(Variable.leader(e, i));
                        int var2 = encodeVariable(Variable.leader(f, i));
                        int var3 = encodeVariable(Variable.set(e, f, i + 1));
                        int var4 = encodeVariable(Variable.leader(e, i + 1));
                        int var5 = encodeVariable(Variable.leader(f, i + 1));
                        formula.addClause(-var1, -var2, -var3, var4, var5);
                    }
                }
            }
        }
        // 5
        for (Integer e : edgeMap.getDestination()) {
            for (Integer f : edgeMap.getDestination()) {
                for (Integer g : edgeMap.getDestination()) {
                    if (e < f && f < g) {
                        int var1 = encodeVariable(Variable.leader(e, d - 1));
                        int var2 = encodeVariable(Variable.leader(f, d - 1));
                        int var3 = encodeVariable(Variable.leader(g, d - 1));
                        int var4 = encodeVariable(Variable.set(e, f, d));
                        int var5 = encodeVariable(Variable.set(e, g, d));
                        int var6 = encodeVariable(Variable.leader(e, d));
                        int var7 = encodeVariable(Variable.leader(f, d));
                        int var8 = encodeVariable(Variable.leader(g, d));
                        formula.addClause(-var1, -var2, -var3, -var4, -var5, var6, var7, var8);
                    }
                }
            }
        }
        // 6
        for (Integer e : edgeMap.getDestination()) {
            for (int i = 1; i < d; i++) {
                int var1 = encodeVariable(Variable.leader(e, i));
                int var2 = encodeVariable(Variable.leader(e, i + 1));
                formula.addClause(var1, -var2);
            }
        }
        // 7
        for (Integer e : edgeMap.getDestination()) {
            for (Integer f : edgeMap.getDestination()) {
                for (Integer g : edgeMap.getDestination()) {
                    if (!e.equals(f) && !e.equals(g)) {
                        Edge<Integer> fEdge = edgeMap.getFromDomain(f);
                        Edge<Integer> gEdge = edgeMap.getFromDomain(g);
                        for (Integer u : vertexMap.getDestination()) {
                            // Unmap u to check edge endpoints
                            Integer ud = vertexMap.getFromDomain(u);
                            if (fEdge.getEndpoints().contains(ud) && gEdge.getEndpoints().contains(ud)) {
                                for (int i = 1; i <= d; i++) {
                                    int var1 = encodeVariable(Variable.leader(e, i));
                                    int var2 = encodeVariable(Variable.load(e, u, i));
                                    int var3 = encodeVariable(Variable.set(Math.min(e, f), Math.max(e, f), i));
                                    int var4 = encodeVariable(Variable.set(Math.min(e, g), Math.max(e, g), i));
                                    formula.addClause(-var1, var2, var3, -var4);
                                }
                            }
                        }
                    }
                }
            }
        }
        // 8
        for (Integer e : edgeMap.getDestination()) {
            for (Integer f : edgeMap.getDestination()) {
                Edge<Integer> eEdge = edgeMap.getFromDomain(e);
                Edge<Integer> fEdge = edgeMap.getFromDomain(f);
                if (!e.equals(f)) {
                    for (Integer u : vertexMap.getDestination()) {
                        // Unmap u to check edge endpoints
                        Integer ud = vertexMap.getFromDomain(u);
                        if (eEdge.getEndpoints().contains(ud) && fEdge.getEndpoints().contains(ud)) {
                            for (int i = 1; i <= d; i++) {
                                int var1 = encodeVariable(Variable.leader(e, i));
                                int var2 = encodeVariable(Variable.set(Math.min(e, f), Math.max(e, f), i));
                                int var3 = encodeVariable(Variable.load(e, u, i));
                                formula.addClause(-var1, var2, var3);
                            }
                        }
                    }
                }
            }
        }
        // 9
        for (Integer e : edgeMap.getDestination()) {
            for (Integer u : vertexMap.getDestination()) {
                for (int i = 1; i <= d - 2; i++) {
                    int var1 = encodeVariable(Variable.leader(e, i));
                    int var2 = encodeVariable(Variable.leader(e, i + 1));
                    int var3 = encodeVariable(Variable.leader(e, i + 2));
                    int var4 = encodeVariable(Variable.load(e, u, i));
                    int var5 = encodeVariable(Variable.load(e, u, i + 2));
                    int var6 = encodeVariable(Variable.load(e, u, i + 1));
                    formula.addClause(-var1, -var2, -var3, -var4, -var5, var6);
                }
            }
        }
        // 10
        for (Integer e : edgeMap.getDestination()) {
            for (int u = 2; u <= vertexMap.size(); u++) {
                for (int i = 1; i <= d; i++) {
                    for (int j = 1; j <= w; j++) {
                        int var1 = encodeVariable(Variable.counter(e, u - 1, i, j));
                        int var2 = encodeVariable(Variable.counter(e, u, i, j));
                        int var3 = encodeVariable(Variable.load(e, u, i));
                        int var4 = encodeVariable(Variable.counter(e, u - 1, i, j - 1));
                        int var5 = var2;
                        int var6 = var3;
                        int var7 = encodeVariable(Variable.counter(e, u - 1, i, w));
                        formula.addClause(-var1, var2);
                        formula.addClause(-var3, var4, var5);
                        formula.addClause(-var6, -var7);
                    }
                }
            }
        }
        // 11
        for (Integer e : edgeMap.getDestination()) {
            for (int u = 1; u <= vertexMap.size(); u++) {
                for (int i = 1; i <= d; i++) {
                    int var1 = encodeVariable(Variable.load(e, u, i));
                    int var2 = encodeVariable(Variable.counter(e, u, i, 1));
                    formula.addClause(-var1, var2);
                }
            }
        }
    }

}
