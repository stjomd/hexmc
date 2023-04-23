package at.ac.tuwien.student.e11843614.sat.factory;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;

public abstract class SATEncodingFactoryForCliqueWidth {

    /**
     * Constructs a SAT encoding of a graph with a formula that is satisfiable iff clique-width of the graph is <= k.
     * @param graph the graph.
     * @param k the target clique-width.
     * @return a SAT encoding for this graph that contains a formula which is satisfiable if cw(graph) <= k.
     */
    public static SATEncoding of(Graph graph, int k) {
        int t = graph.vertices().size() - k + 1;
        Logger.debug("Constructing a SAT encoding for clique-width, k = " + k + ", t = " + t);
        SATEncoding sat = new SATEncoding(graph);
        clause1(sat, t);
        clause2(sat, t);
        clause3(sat, graph, t);
        clause4(sat, graph, t);
        clause5(sat, graph, t);
        clause6(sat, t);
        clause7(sat, t, k);
        Logger.debug("Constructed a SAT encoding for clique-width; formula has " + sat.variableMap().size()
            + " variables and " + sat.getFormula().getClauses().size() + " clauses");
        return sat;
    }

    private static void clause1(SATEncoding sat, int t) {
        for (Integer u : sat.vertexMap().destinationSet()) {
            for (Integer v : sat.vertexMap().destinationSet()) {
                if (u < v) {
                    for (int i = 0; i <= t; i++) {
                        int[] var = new int[]{
                            sat.encodeVariable(Variable.component(u, v, 0)),
                            sat.encodeVariable(Variable.component(u, v, t)),
                            sat.encodeVariable(Variable.component(u, v, i)),
                            sat.encodeVariable(Variable.group(u, v, i)),
                            sat.encodeVariable(Variable.component(u, v, i - 1)),
                            sat.encodeVariable(Variable.component(u, v, i)),
                            sat.encodeVariable(Variable.group(u, v, i - 1)),
                            sat.encodeVariable(Variable.group(u, v, i))
                        };
                        sat.getFormula().addClause(-var[0]);
                        sat.getFormula().addClause(var[1]);
                        sat.getFormula().addClause(var[2], -var[3]);
                        sat.getFormula().addClause(-var[4], var[5]);
                        sat.getFormula().addClause(-var[6], var[7]);
                    }
                }
            }
        }
    }

    private static void clause2(SATEncoding sat, int t) {
        for (Integer u : sat.vertexMap().destinationSet()) {
            for (Integer v : sat.vertexMap().destinationSet()) {
                for (Integer w : sat.vertexMap().destinationSet()) {
                    if (u < v && v < w) {
                        for (int i = 0; i <= t; i++) {
                            int[] varc = new int[]{
                                sat.encodeVariable(Variable.component(u, v, i)),
                                sat.encodeVariable(Variable.component(v, w, i)),
                                sat.encodeVariable(Variable.component(u, w, i)),
                            };
                            sat.getFormula().addClause(-varc[0], -varc[1], varc[2]);
                            sat.getFormula().addClause(-varc[0], -varc[2], varc[1]);
                            sat.getFormula().addClause(-varc[2], -varc[1], varc[0]);
                            int[] varg = new int[]{
                                sat.encodeVariable(Variable.group(u, v, i)),
                                sat.encodeVariable(Variable.group(v, w, i)),
                                sat.encodeVariable(Variable.group(u, w, i))
                            };
                            sat.getFormula().addClause(-varg[0], -varg[1], varg[2]);
                            sat.getFormula().addClause(-varg[0], -varg[2], varg[1]);
                            sat.getFormula().addClause(-varg[2], -varg[1], varg[0]);
                        }
                    }
                }
            }
        }
    }

    private static void clause3(SATEncoding sat, Graph graph, int t) {
        for (Integer u : sat.vertexMap().destinationSet()) {
            for (Integer v : sat.vertexMap().destinationSet()) {
                if (u < v) {
                    // Unmap u, v to check edges
                    Integer uVertex = sat.vertexMap().getFromDomain(u);
                    Integer vVertex = sat.vertexMap().getFromDomain(v);
                    if (graph.hasEdgeWithEndpoints(uVertex, vVertex)) {
                        for (int i = 1; i <= t; i++) {
                            int[] var = new int[]{
                                sat.encodeVariable(Variable.component(u, v, i - 1)),
                                sat.encodeVariable(Variable.group(u, v, i))
                            };
                            sat.getFormula().addClause(var[0], -var[1]);
                        }
                    }
                }
            }
        }
    }

    private static void clause4(SATEncoding sat, Graph graph, int t) {
        for (Integer u : sat.vertexMap().destinationSet()) {
            for (Integer v : sat.vertexMap().destinationSet()) {
                for (Integer w : sat.vertexMap().destinationSet()) {
                    // Unmap u, v, w to check edges
                    Integer uVertex = sat.vertexMap().getFromDomain(u);
                    Integer vVertex = sat.vertexMap().getFromDomain(v);
                    Integer wVertex = sat.vertexMap().getFromDomain(w);
                    if (graph.hasEdgeWithEndpoints(uVertex, vVertex) && !graph.hasEdgeWithEndpoints(uVertex, wVertex)) {
                        for (int i = 1; i <= t; i++) {
                            int[] var = new int[]{
                                sat.encodeVariable(Variable.component(Math.min(u, v), Math.max(u, v), i - 1)),
                                sat.encodeVariable(Variable.group(Math.min(v, w), Math.max(v, w), i))
                            };
                            sat.getFormula().addClause(var[0], -var[1]);
                        }
                    }
                }
            }
        }
    }

    private static void clause5(SATEncoding sat, Graph graph, int t) {
        for (Integer u : sat.vertexMap().destinationSet()) {
            for (Integer v : sat.vertexMap().destinationSet()) {
                if (u < v) {
                    for (Integer w : sat.vertexMap().destinationSet()) {
                        for (Integer x : sat.vertexMap().destinationSet()) {
                            // Unmap u, v, w to check edges
                            Integer uVertex = sat.vertexMap().getFromDomain(u);
                            Integer vVertex = sat.vertexMap().getFromDomain(v);
                            Integer wVertex = sat.vertexMap().getFromDomain(w);
                            Integer xVertex = sat.vertexMap().getFromDomain(x);
                            if (graph.hasEdgeWithEndpoints(uVertex, vVertex) && graph.hasEdgeWithEndpoints(uVertex, wVertex)
                                && graph.hasEdgeWithEndpoints(vVertex, xVertex) && !graph.hasEdgeWithEndpoints(wVertex, xVertex)) {
                                for (int i = 1; i <= t; i++) {
                                    int[] var = new int[]{
                                        sat.encodeVariable(Variable.component(u, v, i - 1)),
                                        sat.encodeVariable(Variable.group(Math.min(u, x), Math.max(u, x), i)),
                                        sat.encodeVariable(Variable.group(Math.min(v, w), Math.max(v, w), i))
                                    };
                                    sat.getFormula().addClause(var[0], -var[1], -var[2]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void clause6(SATEncoding sat, int t) {
        for (Integer v : sat.vertexMap().destinationSet()) {
            for (int i = 0; i <= t; i++) {
                int var1 = sat.encodeVariable(Variable.representative(v, i));
                // Left part
                Clause clause = new Clause();
                clause.addLiteral(var1);
                for (Integer u : sat.vertexMap().destinationSet()) {
                    if (u < v) {
                        int var2 = sat.encodeVariable(Variable.group(u, v, i));
                        clause.addLiteral(var2);
                    }
                }
                sat.getFormula().addClause(clause);
                // Right part
                for (Integer u : sat.vertexMap().destinationSet()) {
                    if (u < v) {
                        int var2 = sat.encodeVariable(Variable.group(u, v, i));
                        sat.getFormula().addClause(-var1, -var2);
                    }
                }
            }
        }
    }

    private static void clause7(SATEncoding sat, int t, int k) {
        for (Integer u : sat.vertexMap().destinationSet()) {
            for (Integer v : sat.vertexMap().destinationSet()) {
                if (u < v) {
                    for (int i = 0; i <= t; i++) {
                        int[] var = new int[]{
                            sat.encodeVariable(Variable.component(u, v, i)),
                            sat.encodeVariable(Variable.representative(u, i)),
                            sat.encodeVariable(Variable.representative(v, i)),
                            sat.encodeVariable(Variable.order(u, k - 1, i)),
                            sat.encodeVariable(Variable.order(v, 1, i))
                        };
                        sat.getFormula().addClause(-var[0], -var[1], -var[2], -var[3]);
                        sat.getFormula().addClause(-var[0], -var[1], -var[2], var[4]);
                        for (int a = 1; a < k - 1; a++) {
                            int o1 = sat.encodeVariable(Variable.order(u, a, i));
                            int o2 = sat.encodeVariable(Variable.order(v, a + 1, i));
                            sat.getFormula().addClause(-var[0], -var[1], -var[2], -o1, o2);
                        }
                    }
                }
            }
        }
    }

}
