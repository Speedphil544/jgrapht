import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.flow.DinicMFImpl;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;
import java.util.zip.Deflater;

public class Testing {


    public static void main(String[] args) {


        Graph<String, DefaultEdge> directedGraph =
                new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        directedGraph.addVertex("a");

        directedGraph.addVertex("b");
        directedGraph.addVertex("c");

        directedGraph.addEdge("a", "b");
        directedGraph.addEdge("b", "c");

        directedGraph.setEdgeWeight("a", "b", 5.0);
        directedGraph.setEdgeWeight("b", "c", 1.0);



        System.out.println(directedGraph);
        GargAndKoenemannMMCFImp alg = new GargAndKoenemannMMCFImp(directedGraph);


        DinicMFImpl dinic = new DinicMFImpl(directedGraph);
        dinic.getMaximumFlow("a","b");
        System.out.println(dinic.getMaximumFlowValue());


        alg.getMaximumFlow("a","c");
       // System.out.println(alg.networkCopy);

        Map flow =alg.composeFlow();
        System.out.println(alg.getMaximumFlowValue());

 //       DijkstraShortestPath dijkstra = new DijkstraShortestPath(directedGraph);
        //GraphPath shortestPath = dijkstra.getPath("a", "c");

       //System.out.println(shortestPath.getWeight());



    }
}

