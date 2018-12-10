


import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.extension.ExtensionFactory;

import java.util.List;

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
    private List<VertexExtension> currentSources;

    /**
     * Current sink vertex.
     */
    private List<VertexExtension> currentSinks;

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
    public MaximumFlow<E> getMaximumFlow(List<V> sources, List<V> sinks) {
        this.calculateMaxFlow(sources, sinks);
        maxFlow = composeFlow();
        return new MaximumMultiCommodityFlowImpl<>(maxFlowValue, maxFlow);
    }

    /**
     * Assigns source to currentSource and sink to currentSink. Afterwards invokes dinic() method to
     * calculate the maximum flow in the network using Dinic algorithm with scaling.
     *
     * @param sources source vertex.
     * @param sinks   sink vertex.
     * @return the value of the maximum flow in the network.
     */
    private double calculateMaxFlow(List<V> sources, List<V> sinks) {

        super.init(sources, sinks, vertexExtensionsFactory, edgeExtensionsFactory);

        if (sources == (null)) {
            throw new IllegalArgumentException("Network does not contain sources!");
        }


        if (sinks == (null)) {
            throw new IllegalArgumentException("Network does not contain sinks!");
        }
        if (sinks.size() != sources.size()) {
            throw new IllegalArgumentException("Network does not have the same number of sources and sinks!");
        }


        for (V source : sources) {
            if (!network.containsVertex(source)) {
                throw new IllegalArgumentException("Network does not contain valid source!");
            }
        }
        for (V sink : sinks) {
            if (!network.containsVertex(sink)) {
                throw new IllegalArgumentException("Network does not contain valid sink!");
            }
        }

        for (int i = 0; i < sinks.size(); i++) {
            if (sinks.get(i).equals(sources.get(i))) {
                throw new IllegalArgumentException("A source is equal to its sink!");
            }
        }

        /*
        for (int i = 0; i < sinks.size(); i++) {
            currentSources.add(getVertexExtension(sources.get(i)));
            currentSinks.add(getVertexExtension(sinks.get(i)));

        }
*/
        gargAndKoenemann();

        return maxFlowValue;
    }

    /**
     * /**
     */
    public void gargAndKoenemann() {


        while (true) {
            //choose shortest path, its value
            DijkstraShortestPath dijkstra = new DijkstraShortestPath(networkCopy);

            double shortestPathLength = 100000;

            GraphPath<VertexExtensionBase, AnnotatedFlowEdge> shortestPath = null;

            for (int i = 0; i < this.sinks.size(); i++) {

                GraphPath<VertexExtensionBase, AnnotatedFlowEdge> newPath = dijkstra.getPath(getVertexExtension(this.sources.get(i)), getVertexExtension(this.sinks.get(i)));
                if (newPath.getLength() < shortestPathLength) {
                    shortestPath = newPath;
                    shortestPathLength = shortestPath.getWeight();
                }

            }


            // breaking condition, we stop when shortest path hast length bigger or equal to 1`
            if (shortestPath.getWeight() >= 1) {
                break;
            }


            //get smallest capacity


            Double smallestCapacity = 10000.0;

            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {

                double newCapacity = networkCopy.getEdgeWeight(e);
                if (newCapacity < smallestCapacity) {
                    smallestCapacity = e.capacity;

                }
            }


            //update length and flow(value)

            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {

                //System.out.println(e.getSource().toString() + networkCopy.getEdgeWeight(e));
                networkCopy.setEdgeWeight(e, networkCopy.getEdgeWeight(e) + networkCopy.getEdgeWeight(e) * accuracy * (smallestCapacity / e.capacity));
                e.flow = e.flow + smallestCapacity;

            }

            maxFlowValue += smallestCapacity;


        }


        //scale the flow
        for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {

            e.flow *= Math.log(1 + accuracy);
            e.flow /= (Math.log((1 + accuracy) / delta));

        }
        maxFlowValue *= Math.log(1 + accuracy);
        maxFlowValue /= (Math.log((1 + accuracy) / delta));
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



