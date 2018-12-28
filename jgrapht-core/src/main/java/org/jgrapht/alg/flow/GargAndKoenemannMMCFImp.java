package org.jgrapht.alg.flow;


import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.extension.ExtensionFactory;

import java.util.LinkedList;
import java.util.List;

/*
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 *
 * @author
 */

public class GargAndKoenemannMMCFImp<V, E>
        extends
        MaximumMultiCommodityFlowAlgorithmBase<V, E> {

    /**
     * Current sources vertexList.
     */
    //private List<VertexExtension> currentSources;
    /**
     * Current sinks vertexList.
     */
    // private List<VertexExtension> currentSinks;


    private final ExtensionFactory<VertexExtension> vertexExtensionsFactory;
    private final ExtensionFactory<AnnotatedFlowEdge> edgeExtensionsFactory;


    // try another data structure

    private List<Pair<VertexExtension, VertexExtension>> currentDemands;
    private Pair<VertexExtension, VertexExtension> currentDemandFlowIsPushedAlong;


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
        currentDemands = new LinkedList<>();

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
    public MaximumFlow<E> getMaximumFlow(List<V> sources, List<V> sinks, double approximationRate) {
        this.calculateMaxFlow(sources, sinks, approximationRate);
        maxFlow = composeFlow();
        for (Pair<VertexExtensionBase, VertexExtensionBase> demand : demands) {
            composeFlow(demand.getFirst().prototype, demand.getSecond().prototype);
        }
        return new MaximumMultiCommodityFlowImpl<>(maxFlowValue, maxFlow, maxFlowValueForEachDemand, mapOfFlowsForEachDemand);
    }


    /**
     * Assigns source to currentSource and sink to currentSink. Afterwards invokes dinic() method to
     * calculate the maximum flow in the network using Dinic algorithm with scaling.
     *
     * @param sources source vertex.
     * @param sinks   sink vertex.
     * @return the value of the maximum flow in the network.
     */
    private double calculateMaxFlow(List<V> sources, List<V> sinks, double approximationRate) {

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
                throw new IllegalArgumentException("Network contains a non  valid source!");
            }
        }
        for (V sink : sinks) {
            if (!network.containsVertex(sink)) {
                throw new IllegalArgumentException("Network contains a non valid sink!");
            }
        }
        for (int i = 0; i < demandSize; i++) {
            if (sinks.get(i).equals(sources.get(i))) {
                throw new IllegalArgumentException("A source is equal to its sink!");
            }
        }

        this.accuracy = 1 - Math.pow(1 + approximationRate, -0.5);
        this.delta = (1 + accuracy) * Math.pow(lengthOfLongestPath * (1 + accuracy), -1 / accuracy);
        assert (comparator.compare(delta, 0.0) != 0) : "Delta too small: " + delta;


        super.init(accuracy, sources, sinks, vertexExtensionsFactory, edgeExtensionsFactory);

        for (int i = 0; i < demandSize; i++) {
            currentDemands.add(new Pair(getVertexExtension(sources.get(i)), getVertexExtension(sinks.get(i))));
        }


        gargAndKoenemann();

        return maxFlowValue;
    }

    /**
     * /**
     */
    public void gargAndKoenemann() {
        int counter = 0;


        while (true) {

            //choose shortest path, its value, its demand
            boolean pathsExist = false;
            double shortestPathWeight = Double.POSITIVE_INFINITY;
            GraphPath<VertexExtensionBase, AnnotatedFlowEdge> shortestPath = null;
            for (Pair demand : currentDemands) {
                DijkstraShortestPath dijkstra = new DijkstraShortestPath(networkCopy);
                GraphPath newPath = dijkstra.getPath(demand.getFirst(), demand.getSecond());
                if (newPath != null) {
                    pathsExist = true;
                    double newPathWeight = newPath.getWeight();
                    if (comparator.compare(newPathWeight, shortestPathWeight) < 0) {
                        shortestPathWeight = newPathWeight;
                        shortestPath = newPath;
                        currentDemandFlowIsPushedAlong = demand;
                    }
                }
            }
            // if there are no valid paths, break and set flow = zeroMapping
            if (!pathsExist) {
                System.out.println("no paths");
                break;
            }
            // breaking condition, we stop when shortest path hast length bigger or equal to 1
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
            //update length and flow(value) (also for each demand)
            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {
                networkCopy.setEdgeWeight(e, networkCopy.getEdgeWeight(e) + networkCopy.getEdgeWeight(e) * accuracy * (smallestCapacity / e.capacity));
                e.flow = e.flow + smallestCapacity;
                //demandFLowMap
                e.demandFlows.put(new Pair<VertexExtensionBase, VertexExtensionBase>(currentDemandFlowIsPushedAlong.getFirst(), currentDemandFlowIsPushedAlong.getSecond()),
                        e.demandFlows.get(currentDemandFlowIsPushedAlong) + smallestCapacity);

            }
            maxFlowValue += smallestCapacity;
            Pair<V, V> pair = new Pair(currentDemandFlowIsPushedAlong.getFirst().prototype, currentDemandFlowIsPushedAlong.getSecond().prototype);
            maxFlowValueForEachDemand.put(pair, maxFlowValueForEachDemand.get(pair) + smallestCapacity);


            counter++;
            System.out.println("");


        }


        //scale the flow
        scaleFlow();


    }


    // method to scale the flow
    private void scaleFlow() {


        maxFlowValue /= Math.log((1 + accuracy) / delta) / Math.log(1 + accuracy);

        for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {
            e.flow /= Math.log((1 + accuracy) / delta) / Math.log(1 + accuracy);
            for (Pair demand : currentDemands) {
                e.demandFlows.put(demand, e.demandFlows.get(demand) / (Math.log((1 + accuracy) / delta) / Math.log(1 + accuracy)));
            }
        }
        for (Pair<VertexExtensionBase, VertexExtensionBase> demand : demands) {
            maxFlowValueForEachDemand.put(castPrototypePair(demand), maxFlowValueForEachDemand.get(castPrototypePair(demand)) / (Math.log((1 + accuracy) / delta) / Math.log(1 + accuracy)));
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

    }

    // cast a extension pair to a V pair
    private Pair<V, V> castPrototypePair(Pair<VertexExtensionBase, VertexExtensionBase> pair) {
        return new Pair(pair.getFirst().prototype, pair.getSecond().prototype);


    }


}