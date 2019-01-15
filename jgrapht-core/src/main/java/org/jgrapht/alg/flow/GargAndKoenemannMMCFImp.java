package org.jgrapht.alg.flow;


import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.extension.ExtensionFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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


    private double approximationRate = 0.0;


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
        //maxFlow = composeFlow();
        // for (Pair<VertexExtensionBase, VertexExtensionBase> demand : demands) {
        //     composeFlow(demand.getFirst().prototype, demand.getSecond().prototype);
        // }
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
        this.demandSize = sinks.size();
        for (int i = 0; i < demandSize; i++) {
            if (sinks.get(i).equals(sources.get(i))) {
                throw new IllegalArgumentException("A source is equal to its sink!");
            }
        }
        this.approximationRate = approximationRate;
        this.accuracy = 1 - Math.pow(1 + approximationRate, -0.5);
        this.delta = (1 + accuracy) * Math.pow(lengthOfLongestPath * (1 + accuracy), -1 / accuracy);
        this.delta = 1e-8;
        assert (comparator.compare(delta, 0.0) != 0) : "Delta too small: " + delta;
        super.init(accuracy, sources, sinks, vertexExtensionsFactory, edgeExtensionsFactory);
        for (int i = 0; i < demandSize; i++) {
            currentDemands.add(new Pair(getVertexExtension(sources.get(i)), getVertexExtension(sinks.get(i))));
        }
        gargAndKoenemann();
        return maxFlowValue;
    }


    public void gargAndKoenemann() {

        int counter = 1;
        BreakingCriterionsAndEdgeScalingObject breakingCriterionsAndEdgeScalingObject = new BreakingCriterionsAndEdgeScalingObject();
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
            if (breakingCriterionsAndEdgeScalingObject.actualizeStats(pathsExist, shortestPath)) {
                break;
            }
            //get smallest capacity
            Double smallestCapacity = Double.POSITIVE_INFINITY;
            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {
                double newCapacity = e.capacity;
                if (comparator.compare(newCapacity, smallestCapacity) < 0) {
                    smallestCapacity = e.capacity;
                }
            }
            //update length and flow(value) (also for each demand)
            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {
                networkCopy.setEdgeWeight(e, networkCopy.getEdgeWeight(e) + networkCopy.getEdgeWeight(e) * accuracy * (smallestCapacity / e.capacity));
                e.flow = e.flow + smallestCapacity;
                //demandFlowMap
                e.demandFlows.put(new Pair<VertexExtensionBase, VertexExtensionBase>(currentDemandFlowIsPushedAlong.getFirst(), currentDemandFlowIsPushedAlong.getSecond()),
                        e.demandFlows.get(currentDemandFlowIsPushedAlong) + smallestCapacity);

            }
            maxFlowValue += smallestCapacity;
            Pair<V, V> pair = new Pair(currentDemandFlowIsPushedAlong.getFirst().prototype, currentDemandFlowIsPushedAlong.getSecond().prototype);
            maxFlowValueForEachDemand.put(pair, maxFlowValueForEachDemand.get(pair) + smallestCapacity);
            counter++;
            // System.out.println(counter);
        }
        //scale the flow to make it feasible
        // maxFlowValue = breakingCriterions.bestMaxFlow;
        maxFlowValue = breakingCriterionsAndEdgeScalingObject.bestMaxFlowValue;
        maxFlow = breakingCriterionsAndEdgeScalingObject.bestmaxFlow;
        mapOfFlowsForEachDemand = breakingCriterionsAndEdgeScalingObject.bestmapOfFlowsForEachDemand;
        maxFlowValueForEachDemand = breakingCriterionsAndEdgeScalingObject.bestMaxFlowValueForEachDemand;
        scaleFlow();

    }

    private void scaleFlow() {
        Double mostViolatedEdgeViolation = 0.0;
        for (E e : maxFlow.keySet()) {
            if (comparator.compare(maxFlow.get(e) / network.getEdgeWeight(e), mostViolatedEdgeViolation) > 0) {
                mostViolatedEdgeViolation = maxFlow.get(e) / network.getEdgeWeight(e);
            }
        }
        for (E e : network.edgeSet()) {
            maxFlow.put(e, maxFlow.get(e) / mostViolatedEdgeViolation);
            for (Pair<VertexExtension, VertexExtension> demand : currentDemands) {
                mapOfFlowsForEachDemand.get(new Pair(demand.getFirst().prototype, demand.getSecond().prototype)).put(e, mapOfFlowsForEachDemand.get(new Pair(demand.getFirst().prototype, demand.getSecond().prototype)).get(e) / mostViolatedEdgeViolation);
            }
            maxFlowValue /= mostViolatedEdgeViolation;

            for (Pair<VertexExtensionBase, VertexExtensionBase> demand : demands) {
                maxFlowValueForEachDemand.put(castPrototypePair(demand), maxFlowValueForEachDemand.get(castPrototypePair(demand)) / mostViolatedEdgeViolation);
            }
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

    // cast an extension pair to a V pair
    private Pair<V, V> castPrototypePair(Pair<VertexExtensionBase, VertexExtensionBase> pair) {
        return new Pair(pair.getFirst().prototype, pair.getSecond().prototype);
    }

    // handle the breaking conditions, the best primal/dual solutions and the scaling
    private class BreakingCriterionsAndEdgeScalingObject {
        double maxPrimalObjectiveFunction = 0.0;
        double minDualObjectiveFunction = Double.POSITIVE_INFINITY;
        double bestMaxFlowValue = 0.0;
        protected Map<Pair<V, V>, Double> bestMaxFlowValueForEachDemand = new HashMap<>();
        int divisionCounter = 0;
        Map<E, Double> bestmaxFlow = null;
        public Map<Pair<V, V>, Map<E, Double>> bestmapOfFlowsForEachDemand = new HashMap<>();

        public boolean actualizeStats(boolean pathsExist, GraphPath shortestPath) {
            double shortestPathWeight = shortestPath.getWeight();
            int shortestPathLength = shortestPath.getLength();
            // if there are no valid paths, break and set flow = zeroMapping
            if (!pathsExist) {
                System.out.println("There are no valid paths from a source to its sink");
                return true;
            }
            // breaking condition, we stop when shortest path hast length bigger or equal to 1
            double b = Math.pow(lengthOfLongestPath * (1 + accuracy), 1 / accuracy - divisionCounter) / (1 + accuracy);
            if (comparator.compare(shortestPathWeight, delta * b) >= 0) {
                return true;
            }
            // check if we need to update the length of the edges
            boolean scaleLengthOfAllEdges = true;
            for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {
                if (comparator.compare(networkCopy.getEdgeWeight(e), delta * lengthOfLongestPath) * (1 + accuracy) <= 0) {
                    scaleLengthOfAllEdges = false;
                }
            }
            if (scaleLengthOfAllEdges) {
                for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {
                    networkCopy.setEdgeWeight(e, networkCopy.getEdgeWeight(e) / (lengthOfLongestPath * (1 + accuracy)));
                }
            }
            // finish earlier with acceptable result
            // first we find out which edge is most violated, in order to get a feasible solution
            Double mostViolatedEdgeViolation = 1.0;
            for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {
                if (comparator.compare(e.flow / e.capacity, mostViolatedEdgeViolation) > 0) {
                    mostViolatedEdgeViolation = e.flow / e.capacity;
                }
            }
            // we update the value of the best flow, if the the value of the current feasible flow is better
            double intermediatePrimalObjectiveFunction = maxFlowValue / mostViolatedEdgeViolation;
            if (comparator.compare(intermediatePrimalObjectiveFunction, maxPrimalObjectiveFunction) >= 0) {
                maxPrimalObjectiveFunction = intermediatePrimalObjectiveFunction;
                bestMaxFlowValue = maxFlowValue;
                bestmaxFlow = composeFlow();
                for (Pair<VertexExtensionBase, VertexExtensionBase> demand : demands) {
                    composeFlow(demand.getFirst().prototype, demand.getSecond().prototype);

                    bestMaxFlowValueForEachDemand.put(new Pair(demand.getFirst().prototype, demand.getSecond().prototype), maxFlowValueForEachDemand.get(new Pair(demand.getFirst().prototype, demand.getSecond().prototype)));
                    bestmapOfFlowsForEachDemand.put(new Pair(demand.getFirst().prototype, demand.getSecond().prototype), composeFlow(demand.getFirst().prototype, demand.getSecond().prototype));

                }
            }
            // we update the value of the length function if the value of the current length function is better
            double intermediateDualObjectiveFunction = Math.pow(shortestPathWeight, -1) * networkCopy.edgeSet().stream().mapToDouble(e -> networkCopy.getEdgeWeight(e) * e.capacity).sum();
            if (comparator.compare(intermediateDualObjectiveFunction, minDualObjectiveFunction) <= 0) {
                minDualObjectiveFunction = intermediateDualObjectiveFunction;
            }
            // if the ratio between the best flow and the best length function is small enough, we end the algorithm
            if (comparator.compare(minDualObjectiveFunction / maxPrimalObjectiveFunction, 1 + approximationRate) <= 0) {
                System.out.println(1 / shortestPathWeight);
                return true;
            }
            MaximumMultiCommodityFlowAlgorithmBase.Demand dm = new MaximumMultiCommodityFlowAlgorithmBase.Demand(currentDemandFlowIsPushedAlong.getFirst(), currentDemandFlowIsPushedAlong.getFirst());
            return false;
        }
    }


    public class Demand extends MaximumMultiCommodityFlowAlgorithmBase.Demand {

        Demand(VertexExtension source, VertexExtension sink) {
            super(source, sink);
        }

    }


}