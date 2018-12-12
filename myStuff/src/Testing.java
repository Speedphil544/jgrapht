import org.jgrapht.Graph;
import org.jgrapht.alg.flow.DinicMFImpl;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Testing {


    public static void main(String[] args) {


        Graph<String, DefaultEdge> directedGraph =
                new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        directedGraph.addVertex("a");
        directedGraph.addVertex("b1");
        directedGraph.addVertex("b");
        directedGraph.addVertex("c");

        directedGraph.addEdge("a", "b");
        directedGraph.addEdge("b", "c");
        directedGraph.addEdge("a", "b1");
        directedGraph.addEdge("b1", "c");

        directedGraph.setEdgeWeight("a", "b", 20.0);
        directedGraph.setEdgeWeight("b", "c", 100.0);
        directedGraph.setEdgeWeight("a", "b1", 500.0);
        directedGraph.setEdgeWeight("b1", "c", 100.0);


        GargAndKoenemannMMCFImp alg = new GargAndKoenemannMMCFImp(directedGraph,Math.pow(10,-40));


        List<String> sources = new LinkedList<String>();
        sources.add("a");
       // sources.add("a");
        List<String> sinks = new LinkedList<String>();
        sinks.add("c");
       //sinks.add("b");
        //System.out.println(sources);
        alg.getMaximumFlow(sources, sinks,0.01);
        // System.out.println(alg.networkCopy);

        Map flow = alg.composeFlow();
        System.out.println(flow.get(directedGraph.getEdge("a","b1")));
        System.out.println(alg.getMaximumFlowValue());

        //       DijkstraShortestPath dijkstra = new DijkstraShortestPath(directedGraph);
        //GraphPath shortestPath = dijkstra.getPath("a", "c");

        //System.out.println(shortestPath.getWeight());


    }
}

