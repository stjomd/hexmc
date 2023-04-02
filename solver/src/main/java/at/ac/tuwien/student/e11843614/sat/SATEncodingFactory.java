package at.ac.tuwien.student.e11843614.sat;

import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.graph.Graph;

/**
 * A class that is responsible for constructing a SAT encoding of a graph.
 */
public abstract class SATEncodingFactory {

    /**
     * Construct a SAT encoding of a graph with a formula that is satisfiable iff branch-width of the graph is <= w.
     * @param graph the graph.
     * @param w the target branch-width.
     * @return a SAT encoding for this graph that contains a formula which is satisfiable if bw(graph) <= w.
     */
    public static SATEncoding forBranchDecompositionOf(Graph<Integer> graph, int w) {
        int d = (int) (Math.floor(graph.getEdges().size() / 2.0)
            - Math.ceil(w / 2.0)
            + Math.ceil(Math.log(Math.floor(w / 2.0)) / Math.log(2))
        ) + 1; // TODO: weird values of d
        SATEncoding sat = new SATEncoding(graph);
        // 1
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
        // 2
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
        // 3
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
        // 4
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
        // 5
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
        // 6
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int i = 1; i < d; i++) {
                int[] var = new int[]{
                    sat.encodeVariable(Variable.leader(e, i)),
                    sat.encodeVariable(Variable.leader(e, i + 1))
                };
                sat.getFormula().addClause(var[0], -var[1]);
            }
        }
        // 7
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
        // 8
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
        // 9
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
        // 10
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
        // 11
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
        return sat;
    }

}