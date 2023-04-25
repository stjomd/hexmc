package at.ac.tuwien.student.e11843614;

import at.ac.tuwien.student.e11843614.struct.graph.Graph;

// TODO: temporary
public abstract class Graphs {

    public static Graph smallFromCliquePaper() {
        Graph cwgraph = new Graph();
        cwgraph.addEdge(1, 2);
        cwgraph.addEdge(1, 4);
        cwgraph.addEdge(2, 3);
        cwgraph.addEdge(2, 4);
        return cwgraph;
    }

    public static Graph petersen() {
        Graph petersen = new Graph();
        petersen.addEdge(0, 1);
        petersen.addEdge(0, 4);
        petersen.addEdge(0, 5);
        petersen.addEdge(1, 2);
        petersen.addEdge(1, 6);
        petersen.addEdge(2, 3);
        petersen.addEdge(2, 7);
        petersen.addEdge(3, 4);
        petersen.addEdge(3, 8);
        petersen.addEdge(4, 9);
        // inner star
        petersen.addEdge(7, 5);
        petersen.addEdge(5, 8);
        petersen.addEdge(8, 6);
        petersen.addEdge(6, 9);
        petersen.addEdge(9, 7);
        return petersen;
    }

    public static Graph fromHeuristicPaper() {
        Graph graph = new Graph();
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

    public static Graph grid4x4() {
        Graph grid = new Graph();
        grid.addEdge(1, 2);
        grid.addEdge(1, 5);
        grid.addEdge(2, 3);
        grid.addEdge(2, 6);
        grid.addEdge(3, 4);
        grid.addEdge(3, 7);
        grid.addEdge(4, 8);
        grid.addEdge(5, 6);
        grid.addEdge(5, 9);
        grid.addEdge(6, 7);
        grid.addEdge(6, 10);
        grid.addEdge(7, 8);
        grid.addEdge(7, 11);
        grid.addEdge(8, 12);
        grid.addEdge(9, 10);
        grid.addEdge(9, 13);
        grid.addEdge(10, 11);
        grid.addEdge(10, 14);
        grid.addEdge(11, 12);
        grid.addEdge(11, 15);
        grid.addEdge(12, 16);
        grid.addEdge(13, 14);
        grid.addEdge(14, 15);
        grid.addEdge(15, 16);
        return grid;
    }

    public static Graph triangularPrism() {
        Graph prism = new Graph();
        prism.addEdge(1, 2);
        prism.addEdge(1, 3);
        prism.addEdge(1, 4);
        prism.addEdge(2, 3);
        prism.addEdge(2, 5);
        prism.addEdge(3, 6);
        prism.addEdge(4, 5);
        prism.addEdge(5, 6);
        prism.addEdge(6, 4);
        return prism;
    }

    public static Graph clique5() {
        Graph clique = new Graph();
        clique.addEdge(1, 2);
        clique.addEdge(1, 3);
        clique.addEdge(1, 4);
        clique.addEdge(1, 5);
        clique.addEdge(2, 3);
        clique.addEdge(2, 4);
        clique.addEdge(2, 5);
        clique.addEdge(3, 4);
        clique.addEdge(3, 5);
        clique.addEdge(4, 5);
        return clique;
    }

}
