


import static org.junit.Assert.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.extension.ExtensionFactory;
import org.junit.Assert;
import java.util.ArrayList;
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
     * GargAndKoenemann algorithm.
     *
     * @param network the network on which we calculate the maximum flow.
     * @param epsilon the tolerance for the comparison of floating point values.
     */
    public GargAndKoenemannMMCFImp(Graph<V, E> network, double epsilon) {

        super(network, epsilon);
        this.vertexExtensionsFactory = VertexExtension::new;
        this.edgeExtensionsFactory = AnnotatedFlowEdge::new;
        currentSources = new ArrayList<>();
        currentSinks = new ArrayList<>();

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
    public MaximumFlow<E> getMaximumFlow(List<V> sources, List<V> sinks, double accuracy) {
        this.calculateMaxFlow(sources, sinks, accuracy);
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
    private double calculateMaxFlow(List<V> sources, List<V> sinks, double accuracy) {

        super.init(accuracy, sources, sinks, vertexExtensionsFactory, edgeExtensionsFactory);

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
                throw new IllegalArgumentException("Network does not contain any valid source!");
            }
        }
        for (V sink : sinks) {
            if (!network.containsVertex(sink)) {
                throw new IllegalArgumentException("Network does not contain any valid sink!");
            }
        }
        for (int i = 0; i < demandSize; i++) {
            if (sinks.get(i).equals(sources.get(i))) {
                throw new IllegalArgumentException("A source is equal to its sink!");
            }
        }
        for (int i = 0; i < demandSize; i++) {
            currentSources.add(getVertexExtension(sources.get(i)));
            currentSinks.add(getVertexExtension(sinks.get(i)));

        }

        gargAndKoenemann();

        return maxFlowValue;
    }

    /**
     * /**
     */
    public void gargAndKoenemann() {

        // add a dummy to make dijkstra computation shorter
        networkCopy.addVertex(vertexExtensionManager.getExtension(null));
        for (VertexExtension source : currentSources) {
            AnnotatedFlowEdge annotatedFlowEdge = createEdge(vertexExtensionManager.getExtension(null), source, null, Double.POSITIVE_INFINITY);
            annotatedFlowEdge.capacity = Double.POSITIVE_INFINITY;
            networkCopy.addEdge(vertexExtensionManager.getExtension(null), source, annotatedFlowEdge);
            networkCopy.setEdgeWeight(annotatedFlowEdge, 0.0);

        }
        while (true) {

            //choose shortest path, its value
            DijkstraShortestPath dijkstra = new DijkstraShortestPath(networkCopy);
            double shortestPathWeight = Double.POSITIVE_INFINITY;
            GraphPath<VertexExtensionBase, AnnotatedFlowEdge> shortestPath = null;
            SingleSourcePaths allPaths = dijkstra.getPaths(vertexExtensionManager.getExtension(null));

            // check if there are no paths
            boolean pathsExist = false;

            for (VertexExtension sink : currentSinks) {
                GraphPath newPath = allPaths.getPath(sink);
                if (newPath != null) {
                    pathsExist=true;
                    double newPathWeight = newPath.getWeight();
                    if (comparator.compare(newPathWeight, shortestPathWeight) < 0) {
                        shortestPathWeight = newPathWeight;
                        shortestPath = newPath;
                    }
                }
            }
            Assert.assertTrue("no valid paths exist",pathsExist);
            // breaking condition, we stop when shortest path hast length bigger or equal to 1`
            if (comparator.compare(shortestPath.getWeight(), 1.0) >= 0) {
                break;
            }
            //get smallest capacity
            Double smallestCapacity = Double.POSITIVE_INFINITY;
            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {
                double newCapacity = networkCopy.getEdgeWeight(e);
                if (comparator.compare(newCapacity, smallestCapacity) < 0) {
                    smallestCapacity = e.capacity;
                }
            }
            //update length and flow(value)
            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {
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

    }
}



