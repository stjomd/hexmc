package at.ac.tuwien.student.e11843614.struct.graph;

import at.ac.tuwien.student.e11843614.formula.Formula;

public abstract class GraphFactory {

    /**
     * Constructs the incidence graph of the formula.
     * @param formula the formula to construct the incidence graph for.
     * @return an incidence graph, where vertices representing variables of the formula end in 1, and vertices
     *         representing clauses end in 2.
     */
    public static Graph incidenceGraph(Formula formula) {
        // Encode as follows: variable vertices end with 1, clause vertices end with 2
        Graph graph = new Graph();
        for (int i = 1; i <= formula.clauses().size(); i++) {
            for (Integer literal : formula.clauses().get(i - 1).literals()) {
                int variableVertex = 10*Math.abs(literal) + 1;
                int clauseVertex = 10*i + 2;
                graph.addEdge(variableVertex, clauseVertex);
            }
        }
        return graph;
    }

}
