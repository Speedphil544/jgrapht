


import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.extension.ExtensionFactory;


/*
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author
 */

public class GargAndKoenemannMMCFImp<V, E>
        extends
        MaximumMultiCommodityFLowAlgorithmBase<V, E> {

    /**
     * Current source vertex.
     */
    private VertexExtension currentSource;

    /**
     * Current sink vertex.
     */
    private VertexExtension currentSink;

    private final ExtensionFactory<VertexExtension> vertexExtensionsFactory;
    private final ExtensionFactory<AnnotatedFlowEdge> edgeExtensionsFactory;

    /**
     * Constructor. Constructs a new network on which we will calculate the maximum flow, using
     * Dinic algorithm.
     *
     * @param network the network on which we calculate the maximum flow.
     * @param epsilon the tolerance for the comparison of floating point values.
     */
    public GargAndKoenemannMMCFImp(Graph<V, E> network, double epsilon) {
        super(network, epsilon);
        this.vertexExtensionsFactory = VertexExtension::new;

        this.edgeExtensionsFactory = AnnotatedFlowEdge::new;

        if (epsilon <= 0) {
            throw new IllegalArgumentException("Epsilon must be positive!");
        }

        for (E e : network.edgeSet()) {
            if (network.getEdgeWeight(e) < -epsilon) {
                throw new IllegalArgumentException("Capacity must be non-negative!");
            }
        }
    }

    /**
     * Constructor. Constructs a new network on which we will calculate the maximum flow.
     *
     * @param network the network on which we calculate the maximum flow.
     */
    public GargAndKoenemannMMCFImp(Graph<V, E> network) {
        this(network, DEFAULT_EPSILON);
    }

    @Override
    public MaximumFlow<E> getMaximumFlow(V source, V sink) {
        this.calculateMaxFlow(source, sink);
        maxFlow = composeFlow();
        return new MaximumFlowImpl<>(maxFlowValue, maxFlow);
    }

    /**
     * Assigns source to currentSource and sink to currentSink. Afterwards invokes dinic() method to
     * calculate the maximum flow in the network using Dinic algorithm with scaling.
     *
     * @param source source vertex.
     * @param sink   sink vertex.
     * @return the value of the maximum flow in the network.
     */
    private double calculateMaxFlow(V source, V sink) {
        super.init(source, sink, vertexExtensionsFactory, edgeExtensionsFactory);

        if (!network.containsVertex(source)) {
            throw new IllegalArgumentException("Network does not contain source!");
        }

        if (!network.containsVertex(sink)) {
            throw new IllegalArgumentException("Network does not contain sink!");
        }

        if (source.equals(sink)) {
            throw new IllegalArgumentException("Source is equal to sink!");
        }

        currentSource = getVertexExtension(source);
        currentSink = getVertexExtension(sink);

        gargAndKoenemann();

        return maxFlowValue;
    }

    /**
     * /**
     */
    public void gargAndKoenemann() {


        while (true) {

            DijkstraShortestPath dijkstra = new DijkstraShortestPath(networkCopy);
            GraphPath<VertexExtensionBase, AnnotatedFlowEdge> shortestPath = dijkstra.getPath(getVertexExtension(this.source), getVertexExtension(this.sink));


            //choose shortest path, its value


            // breaking condition, we stop when shortest path hast length bigger or equal to 1`
            if (shortestPath.getWeight() >= 1) {
                break;
            }


            //get smallest capacity

            Double smallestWeight = 10000.0;
            Double smallestCapacity = 10000.0;

            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {

                if (networkCopy.getEdgeWeight(e) < smallestWeight){
                    smallestCapacity = e.capacity;
                }


            }


            //update length and flow(value)

            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {
                System.out.println(e.getSource().toString()+networkCopy.getEdgeWeight(e));
                networkCopy.setEdgeWeight(e, networkCopy.getEdgeWeight(e) + accuracy * (smallestCapacity / e.capacity));
                e.flow = e.flow + smallestCapacity;

            }
            System.out.println(this.networkCopy);


        }


        //scale the flow
       for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {

           e.flow = (e.flow* Math.log(1 + accuracy)) / (Math.log((1 + accuracy) / delta));
       }
    }


    private VertexExtension getVertexExtension(V v) {
        return (VertexExtension) vertexExtensionManager.getExtension(v);
    }

    /**
     * Extension for vertex class.
     */
    class VertexExtension
            extends
            VertexExtensionBase {

        /**
         * Stores index of the first unexplored edge from current vertex.
         */
        int index;

        /**
         * Level of vertex in the level graph.
         */
        int level;
    }
}



