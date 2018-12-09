import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Testing {


    public static void main(String[] args) {


        Graph<String, DefaultEdge> directedGraph =
                new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        directedGraph.addVertex("a");

        directedGraph.addVertex("b");
        directedGraph.addVertex("c");

        directedGraph.addEdge("a", "b");
        directedGraph.addEdge("b", "c");

        directedGraph.setEdgeWeight("a", "b", 2.0);
        directedGraph.setEdgeWeight("b", "c", 1.0);



        System.out.println(directedGraph);
        GargAndKoenemannMMCFImp alg = new GargAndKoenemannMMCFImp(directedGraph);

        alg.getMaximumFlow("a","c");
        System.out.println(alg.networkCopy);

        DijkstraShortestPath dijkstra2 = new DijkstraShortestPath(alg.networkCopy);
        GraphPath shortestPath2 = dijkstra2.getPath(alg.vertexExtensionManager.getExtension("a"), alg.vertexExtensionManager.getExtension("c"));
        System.out.println(shortestPath2.getWeight());


        DijkstraShortestPath dijkstra = new DijkstraShortestPath(directedGraph);
        GraphPath shortestPath = dijkstra.getPath("a", "c");

        System.out.println(shortestPath.getWeight());



    }
}

