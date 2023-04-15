package at.ac.tuwien.student.e11843614.sat;

import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.struct.Bijection;

/**
 * A class that represents a SAT encoding for graph parameters, i.e. a formula and a mapping of its variables to
 * integers (for DIMACS CNF format).
 */
public class SATEncoding {

    private int variableCounter = 1;

    private final Formula formula = new Formula();
    private final Bijection<Variable, Integer> variableMap = new Bijection<>();

    private final Bijection<Integer, Integer> vertexMap = new Bijection<>();
    private final Bijection<Edge, Integer> edgeMap = new Bijection<>();

    public SATEncoding(Graph graph) {
        encodeGraph(graph);
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
    public Bijection<Variable, Integer> variableMap() {
        return variableMap;
    }

    /**
     * Returns the bijection between the vertices and their integer representations.
     * @return a bijection between vertices and integers.
     */
    public Bijection<Integer, Integer> vertexMap() {
        return vertexMap;
    }

    /**
     * Returns the bijection between the edges and their integer representations.
     * @return a bijection between edges and integers.
     */
    public Bijection<Edge, Integer> edgeMap() {
        return edgeMap;
    }

    // ----- Encoding --------------------------------------------------------------------------------------------------

    /**
     * Maps an integer (>= 1) to a variable. If the variable has been mapped already, returns its existing mapping.
     * @param variable the variable to be encoded.
     * @return the integer (>= 1) this variable is mapped to.
     */
    public int encodeVariable(Variable variable) {
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
    private void encodeGraph(Graph graph) {
        int i = 1;
        for (Integer vertex : graph.getVertices()) {
            vertexMap.put(vertex, i);
            i++;
        }
        i = 1;
        for (Edge edge : graph.getEdges()) {
            edgeMap.put(edge, i);
            i++;
        }
    }

}
