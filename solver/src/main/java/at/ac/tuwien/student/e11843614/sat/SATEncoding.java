package at.ac.tuwien.student.e11843614.sat;

import at.ac.tuwien.student.e11843614.formula.Clause;
import at.ac.tuwien.student.e11843614.formula.Formula;
import at.ac.tuwien.student.e11843614.graph.Edge;
import at.ac.tuwien.student.e11843614.graph.Graph;

public class SATEncoding {

    private final Formula formula = new Formula();

    private int counter = 1;

    private final Bijection<Integer, Integer>       vertices  = new Bijection<>();
    private final Bijection<Edge<Integer>, Integer> edges     = new Bijection<>();
    private final Bijection<Variable, Integer>      variables = new Bijection<>();

    private SATEncoding() {}

    public static SATEncoding forBranchDecompositionOf(Graph<Integer> graph, int w) {
        SATEncoding encoding = new SATEncoding();
        encoding.encodeGraph(graph);
        encoding.buildClauses(graph, w, w);
        return encoding;
    }

    private int encodeVariable(Variable variable) {
        Integer value = variables.get2(variable);
        if (value != null) {
            return value;
        }
        variables.put(variable, counter);
        return counter++;
    }

    private void encodeGraph(Graph<Integer> graph) {
        int i = 1;
        for (Integer vertex : graph.getVertices()) {
            vertices.put(vertex, i);
            i++;
        }
        i = 1;
        for (Edge<Integer> edge : graph.getEdges()) {
            edges.put(edge, i);
            i++;
        }
    }

    private void buildClauses(Graph<Integer> graph, int w, int d) {
        // 1
        for (Integer e : edges.getDestination()) {
            for (Integer f : edges.getDestination()) {
                if (e < f) {
                    int var1 = encodeVariable(Variable.set(e, f, 0));
                    int var2 = encodeVariable(Variable.set(e, f, d));
                    for (int i = 1; i <= d; i++) {
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
        for (Integer e : edges.getDestination()) {
            for (Integer f : edges.getDestination()) {
                for (Integer g : edges.getDestination()) {
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
        for (Integer e : edges.getDestination()) {
            for (int i = 1; i <= d; i++) {
                // Left side
                Clause clause = new Clause();
                int var1 = encodeVariable(Variable.leader(e, i));
                clause.addLiteral(var1);
                for (Integer f : edges.getDestination()) {
                    if (f < e) {
                        int var2 = encodeVariable(Variable.set(f, e, i));
                        clause.addLiteral(var2);
                    }
                }
                formula.addClause(clause);
                // Right side
                for (Integer f : edges.getDestination()) {
                    if (f < e) {
                        int var2 = encodeVariable(Variable.set(f, e, i));
                        formula.addClause(-var1, -var2);
                    }
                }
            }
        }
        // TODO: add rest
    }

    public Formula getFormula() {
        return formula;
    }

}
