package at.ac.tuwien.student.e11843614.sat.factory;

import at.ac.tuwien.student.e11843614.Logger;
import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.struct.graph.Edge;
import at.ac.tuwien.student.e11843614.struct.graph.Graph;
import at.ac.tuwien.student.e11843614.sat.SATEncoding;
import at.ac.tuwien.student.e11843614.sat.Variable;

public abstract class SATEncodingFactoryForBranchWidth {

    /**
     * Constructs a SAT encoding of a graph with a formula that is satisfiable iff branch-width of the graph is <= w.
     * @param graph the graph.
     * @param w the target branch-width.
     * @return a SAT encoding for this graph that contains a formula which is satisfiable if bw(graph) <= w.
     */
    public static SATEncoding of(Graph<Integer> graph, int w) {
        int d = (int) Math.floor(graph.getEdges().size() / 2.0)
            - (int) Math.ceil(w / 2.0)
            + (int) Math.max(0, Math.ceil(Math.log(Math.floor(w / 2.0)) / Math.log(2))); // prevent overflow when w=1
        Logger.debug("Constructing a SAT encoding for branch-width, d = " + d + ", w = " + w);
        SATEncoding sat = new SATEncoding(graph);
        clause1(sat, d);
        clause2(sat, d);
        clause3(sat, d);
        clause4(sat, d);
        clause5(sat, d);
        clause6(sat, d);
        clause7(sat, d);
        clause8(sat, d);
        clause9(sat, d);
        clause10(sat, d, w);
        clause11(sat, d);
        Logger.debug("Constructed a SAT encoding for branch-width; formula has " + sat.getVariableMap().size()
            + " variables and " + sat.getFormula().getClauses().size() + " clauses");
        return sat;
    }

    private static void clause1(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (Integer f : sat.getEdgeMap().getDestination()) {
                if (e < f) {
                    for (int i = 1; i < d; i++) {
                        int[] var = new int[]{
                            sat.encodeVariable(Variable.set(e, f, 0)),
                            sat.encodeVariable(Variable.set(e, f, d)),
                            sat.encodeVariable(Variable.set(e, f, i)),
                            sat.encodeVariable(Variable.set(e, f, i + 1))
                        };
                        sat.getFormula().addClause(-var[0]);
                        sat.getFormula().addClause(var[1]);
                        sat.getFormula().addClause(-var[2], var[3]);
                    }
                }
            }
        }
    }

    private static void clause2(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (Integer f : sat.getEdgeMap().getDestination()) {
                for (Integer g : sat.getEdgeMap().getDestination()) {
                    if (e < f && f < g) {
                        for (int i = 1; i <= d; i++) {
                            int[] var = new int[]{
                                sat.encodeVariable(Variable.set(e, f, i)),
                                sat.encodeVariable(Variable.set(e, g, i)),
                                sat.encodeVariable(Variable.set(f, g, i))
                            };
                            sat.getFormula().addClause(-var[0], -var[1], var[2]);
                            sat.getFormula().addClause(-var[0], -var[2], var[1]);
                            sat.getFormula().addClause(-var[1], -var[2], var[0]);
                        }
                    }
                }
            }
        }
    }

    private static void clause3(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int i = 1; i <= d; i++) {
                // Part A
                Clause clause = new Clause();
                int var1 = sat.encodeVariable(Variable.leader(e, i));
                clause.addLiteral(var1);
                for (Integer f : sat.getEdgeMap().getDestination()) {
                    if (f < e) {
                        int var2 = sat.encodeVariable(Variable.set(f, e, i));
                        clause.addLiteral(var2);
                    }
                }
                sat.getFormula().addClause(clause);
                // Part B
                for (Integer f : sat.getEdgeMap().getDestination()) {
                    if (f < e) {
                        int var2 = sat.encodeVariable(Variable.set(f, e, i));
                        sat.getFormula().addClause(-var1, -var2);
                    }
                }
            }
        }
    }

    private static void clause4(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (Integer f : sat.getEdgeMap().getDestination()) {
                if (e < f) {
                    for (int i = 1; i < d - 1; i++) {
                        int[] var = new int[]{
                            sat.encodeVariable(Variable.leader(e, i)),
                            sat.encodeVariable(Variable.leader(f, i)),
                            sat.encodeVariable(Variable.set(e, f, i + 1)),
                            sat.encodeVariable(Variable.leader(e, i + 1)),
                            sat.encodeVariable(Variable.leader(f, i + 1))
                        };
                        sat.getFormula().addClause(-var[0], -var[1], -var[2], var[3], var[4]);
                    }
                }
            }
        }
    }

    private static void clause5(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (Integer f : sat.getEdgeMap().getDestination()) {
                for (Integer g : sat.getEdgeMap().getDestination()) {
                    if (e < f && f < g) {
                        int[] var = new int[]{
                            sat.encodeVariable(Variable.leader(e, d - 1)),
                            sat.encodeVariable(Variable.leader(f, d - 1)),
                            sat.encodeVariable(Variable.leader(g, d - 1)),
                            sat.encodeVariable(Variable.set(e, f, d)),
                            sat.encodeVariable(Variable.set(e, g, d)),
                            sat.encodeVariable(Variable.leader(e, d)),
                            sat.encodeVariable(Variable.leader(f, d)),
                            sat.encodeVariable(Variable.leader(g, d))
                        };
                        sat.getFormula().addClause(-var[0], -var[1], -var[2], -var[3], -var[4], var[5], var[6], var[7]);
                    }
                }
            }
        }
    }

    private static void clause6(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int i = 1; i < d; i++) {
                int[] var = new int[]{
                    sat.encodeVariable(Variable.leader(e, i)),
                    sat.encodeVariable(Variable.leader(e, i + 1))
                };
                sat.getFormula().addClause(var[0], -var[1]);
            }
        }
    }

    private static void clause7(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (Integer f : sat.getEdgeMap().getDestination()) {
                for (Integer g : sat.getEdgeMap().getDestination()) {
                    if (!e.equals(f) && !e.equals(g)) {
                        Edge<Integer> fEdge = sat.getEdgeMap().getFromDomain(f);
                        Edge<Integer> gEdge = sat.getEdgeMap().getFromDomain(g);
                        for (Integer u : sat.getVertexMap().getDestination()) {
                            // Unmap u to check edge endpoints
                            Integer ud = sat.getVertexMap().getFromDomain(u);
                            if (fEdge.getEndpoints().contains(ud) && gEdge.getEndpoints().contains(ud)) {
                                for (int i = 1; i <= d; i++) {
                                    int[] var = new int[]{
                                        sat.encodeVariable(Variable.leader(e, i)),
                                        sat.encodeVariable(Variable.load(e, u, i)),
                                        sat.encodeVariable(Variable.set(Math.min(e, f), Math.max(e, f), i)),
                                        sat.encodeVariable(Variable.set(Math.min(e, g), Math.max(e, g), i))
                                    };
                                    sat.getFormula().addClause(-var[0], var[1], var[2], -var[3]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void clause8(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (Integer f : sat.getEdgeMap().getDestination()) {
                if (!e.equals(f)) {
                    Edge<Integer> eEdge = sat.getEdgeMap().getFromDomain(e);
                    Edge<Integer> fEdge = sat.getEdgeMap().getFromDomain(f);
                    for (Integer u : sat.getVertexMap().getDestination()) {
                        // Unmap u to check edge endpoints
                        Integer ud = sat.getVertexMap().getFromDomain(u);
                        if (eEdge.getEndpoints().contains(ud) && fEdge.getEndpoints().contains(ud)) {
                            for (int i = 1; i <= d; i++) {
                                int[] var = new int[]{
                                    sat.encodeVariable(Variable.leader(e, i)),
                                    sat.encodeVariable(Variable.set(Math.min(e, f), Math.max(e, f), i)),
                                    sat.encodeVariable(Variable.load(e, u, i))
                                };
                                sat.getFormula().addClause(-var[0], var[1], var[2]);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void clause9(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (Integer u : sat.getVertexMap().getDestination()) {
                for (int i = 1; i <= d - 2; i++) {
                    int[] var = new int[]{
                        sat.encodeVariable(Variable.leader(e, i)),
                        sat.encodeVariable(Variable.leader(e, i + 1)),
                        sat.encodeVariable(Variable.leader(e, i + 2)),
                        sat.encodeVariable(Variable.load(e, u, i)),
                        sat.encodeVariable(Variable.load(e, u, i + 2)),
                        sat.encodeVariable(Variable.load(e, u, i + 1))
                    };
                    sat.getFormula().addClause(-var[0], -var[1], -var[2], -var[3], -var[4], var[5]);
                }
            }
        }
    }

    private static void clause10(SATEncoding sat, int d, int w) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int u = 2; u <= sat.getVertexMap().size(); u++) {
                for (int i = 1; i <= d; i++) {
                    for (int j = 1; j <= w; j++) {
                        int[] var = new int[]{
                            sat.encodeVariable(Variable.counter(e, u - 1, i, j)),
                            sat.encodeVariable(Variable.counter(e, u, i, j)),
                            sat.encodeVariable(Variable.load(e, u, i)),
                            sat.encodeVariable(Variable.counter(e, u - 1, i, j - 1)),
                            sat.encodeVariable(Variable.counter(e, u, i, j)),
                            sat.encodeVariable(Variable.load(e, u, i)),
                            sat.encodeVariable(Variable.counter(e, u - 1, i, w))
                        };
                        sat.getFormula().addClause(-var[0], var[1]);
                        sat.getFormula().addClause(-var[2], var[3], var[4]);
                        sat.getFormula().addClause(-var[5], -var[6]);
                    }
                }
            }
        }
    }

    private static void clause11(SATEncoding sat, int d) {
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int u = 1; u <= sat.getVertexMap().size(); u++) {
                for (int i = 1; i <= d; i++) {
                    int[] var = new int[]{
                        sat.encodeVariable(Variable.load(e, u, i)),
                        sat.encodeVariable(Variable.counter(e, u, i, 1))
                    };
                    sat.getFormula().addClause(-var[0], var[1]);
                }
            }
        }
    }

}
