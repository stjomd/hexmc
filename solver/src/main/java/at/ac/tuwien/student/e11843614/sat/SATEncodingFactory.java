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
                    for (int i = 1; i <= d; i++) {
                        int var1 = sat.encodeVariable(Variable.set(e, f, 0));
                        int var2 = sat.encodeVariable(Variable.set(e, f, d));
                        int var3 = sat.encodeVariable(Variable.set(e, f, i));
                        int var4 = sat.encodeVariable(Variable.set(e, f, i + 1));
                        sat.getFormula().addClause(-var1);
                        sat.getFormula().addClause(var2);
                        sat.getFormula().addClause(-var3, var4);
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
                            int var1 = sat.encodeVariable(Variable.set(e, f, i));
                            int var2 = sat.encodeVariable(Variable.set(e, g, i));
                            int var3 = sat.encodeVariable(Variable.set(f, g, i));
                            sat.getFormula().addClause(-var1, -var2, var3);
                            sat.getFormula().addClause(-var1, -var3, var2);
                            sat.getFormula().addClause(-var2, -var3, var1);
                        }
                    }
                }
            }
        }
        // 3
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int i = 1; i <= d; i++) {
                // Left side
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
                // Right side
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
                        int var1 = sat.encodeVariable(Variable.leader(e, i));
                        int var2 = sat.encodeVariable(Variable.leader(f, i));
                        int var3 = sat.encodeVariable(Variable.set(e, f, i + 1));
                        int var4 = sat.encodeVariable(Variable.leader(e, i + 1));
                        int var5 = sat.encodeVariable(Variable.leader(f, i + 1));
                        sat.getFormula().addClause(-var1, -var2, -var3, var4, var5);
                    }
                }
            }
        }
        // 5
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (Integer f : sat.getEdgeMap().getDestination()) {
                for (Integer g : sat.getEdgeMap().getDestination()) {
                    if (e < f && f < g) {
                        int var1 = sat.encodeVariable(Variable.leader(e, d - 1));
                        int var2 = sat.encodeVariable(Variable.leader(f, d - 1));
                        int var3 = sat.encodeVariable(Variable.leader(g, d - 1));
                        int var4 = sat.encodeVariable(Variable.set(e, f, d));
                        int var5 = sat.encodeVariable(Variable.set(e, g, d));
                        int var6 = sat.encodeVariable(Variable.leader(e, d));
                        int var7 = sat.encodeVariable(Variable.leader(f, d));
                        int var8 = sat.encodeVariable(Variable.leader(g, d));
                        sat.getFormula().addClause(-var1, -var2, -var3, -var4, -var5, var6, var7, var8);
                    }
                }
            }
        }
        // 6
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int i = 1; i < d; i++) {
                int var1 = sat.encodeVariable(Variable.leader(e, i));
                int var2 = sat.encodeVariable(Variable.leader(e, i + 1));
                sat.getFormula().addClause(var1, -var2);
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
                                    int var1 = sat.encodeVariable(Variable.leader(e, i));
                                    int var2 = sat.encodeVariable(Variable.load(e, u, i));
                                    int var3 = sat.encodeVariable(Variable.set(Math.min(e, f), Math.max(e, f), i));
                                    int var4 = sat.encodeVariable(Variable.set(Math.min(e, g), Math.max(e, g), i));
                                    sat.getFormula().addClause(-var1, var2, var3, -var4);
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
                Edge<Integer> eEdge = sat.getEdgeMap().getFromDomain(e);
                Edge<Integer> fEdge = sat.getEdgeMap().getFromDomain(f);
                if (!e.equals(f)) {
                    for (Integer u : sat.getVertexMap().getDestination()) {
                        // Unmap u to check edge endpoints
                        Integer ud = sat.getVertexMap().getFromDomain(u);
                        if (eEdge.getEndpoints().contains(ud) && fEdge.getEndpoints().contains(ud)) {
                            for (int i = 1; i <= d; i++) {
                                int var1 = sat.encodeVariable(Variable.leader(e, i));
                                int var2 = sat.encodeVariable(Variable.set(Math.min(e, f), Math.max(e, f), i));
                                int var3 = sat.encodeVariable(Variable.load(e, u, i));
                                sat.getFormula().addClause(-var1, var2, var3);
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
                    int var1 = sat.encodeVariable(Variable.leader(e, i));
                    int var2 = sat.encodeVariable(Variable.leader(e, i + 1));
                    int var3 = sat.encodeVariable(Variable.leader(e, i + 2));
                    int var4 = sat.encodeVariable(Variable.load(e, u, i));
                    int var5 = sat.encodeVariable(Variable.load(e, u, i + 2));
                    int var6 = sat.encodeVariable(Variable.load(e, u, i + 1));
                    sat.getFormula().addClause(-var1, -var2, -var3, -var4, -var5, var6);
                }
            }
        }
        // 10
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int u = 2; u <= sat.getVertexMap().size(); u++) {
                for (int i = 1; i <= d; i++) {
                    for (int j = 1; j <= w; j++) {
                        int var1 = sat.encodeVariable(Variable.counter(e, u - 1, i, j));
                        int var2 = sat.encodeVariable(Variable.counter(e, u, i, j));
                        int var3 = sat.encodeVariable(Variable.load(e, u, i));
                        int var4 = sat.encodeVariable(Variable.counter(e, u - 1, i, j - 1));
                        int var5 = var2;
                        int var6 = var3;
                        int var7 = sat.encodeVariable(Variable.counter(e, u - 1, i, w));
                        sat.getFormula().addClause(-var1, var2);
                        sat.getFormula().addClause(-var3, var4, var5);
                        sat.getFormula().addClause(-var6, -var7);
                    }
                }
            }
        }
        // 11
        for (Integer e : sat.getEdgeMap().getDestination()) {
            for (int u = 1; u <= sat.getVertexMap().size(); u++) {
                for (int i = 1; i <= d; i++) {
                    int var1 = sat.encodeVariable(Variable.load(e, u, i));
                    int var2 = sat.encodeVariable(Variable.counter(e, u, i, 1));
                    sat.getFormula().addClause(-var1, var2);
                }
            }
        }
        return sat;
    }

}
