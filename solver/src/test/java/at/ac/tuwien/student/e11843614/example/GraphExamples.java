package at.ac.tuwien.student.e11843614.example;

import at.ac.tuwien.student.e11843614.struct.graph.Graph;

public abstract class GraphExamples {

    public static Graph<Integer> example() {
        // a1, b2, c3, d4, e5, f6, g7, h8, i9, j10, k11
        Graph<Integer> graph = new Graph<>();
        graph.addEdge(1, 4); // ad
        graph.addEdge(1, 8); // ah
        graph.addEdge(1, 2); // ab
        graph.addEdge(1, 3); // ac
        graph.addEdge(4, 5); // de
        graph.addEdge(2, 5); // be
        graph.addEdge(2, 6); // bf
        graph.addEdge(3, 6); // cf
        graph.addEdge(3, 7); // cg
        graph.addEdge(7, 8); // gh
        graph.addEdge(5, 9); // ei
        graph.addEdge(6, 9); // fi
        graph.addEdge(6, 10); // fj
        graph.addEdge(7, 10); // gj
        graph.addEdge(4, 11); // dk
        graph.addEdge(8, 11); // hk
        graph.addEdge(9, 11); // ik
        graph.addEdge(10, 11); // jk
        return graph;
    }

}
