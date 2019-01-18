package org.jgrapht.alg.flow;


import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
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

    private List<Demand> currentDemands;
    private Demand currentDemandFlowIsPushedAlong;


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
        currentDemands = new LinkedList();

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
    public MaximumFlow<E> getMaximumFlow(List<V> sources, List<V> sinks, double approximationRate, List<Map<E,Double>> allClosedEdgesForADemand) {
        this.calculateMaxFlow(sources, sinks, approximationRate, allClosedEdgesForADemand);
        //maxFlow = composeFlow();
        // for (Pair<VertexExtensionBase, VertexExtensionBase> demand : demands) {
        //     composeFlow(demand.getFirst().prototype, demand.getSecond().prototype);
        // }
        return new MaximumMultiCommodityFlowImpl(maxFlowValue, maxFlow, maxFlowValueForEachDemand, mapOfFlowsForEachDemand);
    }


    /**
     * Assigns source to currentSource and sink to currentSink. Afterwards invokes dinic() method to
     * calculate the maximum flow in the network using Dinic algorithm with scaling.
     *
     * @param sources source vertex.
     * @param sinks   sink vertex.
     * @return the value of the maximum flow in the network.
     */
    private double calculateMaxFlow(List<V> sources, List<V> sinks, double approximationRate, List<Map<E,Double>> allClosedEdgesForADemand) {
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
        super.init(accuracy, sources, sinks, vertexExtensionsFactory, edgeExtensionsFactory, allClosedEdgesForADemand);
        for (int i = 0; i < demandSize; i++) {
            currentDemands.add(new Demand(vertexExtensionManager.getExtension(sources.get(i)), vertexExtensionManager.getExtension(sinks.get(i))));
            //System.out.println((new Demand(vertexExtensionManager.getExtension((sources.get(i))), vertexExtensionManager.getExtension(sinks.get(i)))).equals(demands.get(i))+"lala");
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
            for (Demand demand : demands) {

                // "remove" closed edges
                Map<AnnotatedFlowEdge, Double> saveTheWeightsOfTheEdges = new HashMap<>();
                for (E e : allClosedEdgesForADemand.get(demand).keySet()) {
                    AnnotatedFlowEdge annotatedFlowEdge = edgeExtensionManager.getExtension(e);
                    saveTheWeightsOfTheEdges.put(annotatedFlowEdge, networkCopy.getEdgeWeight(annotatedFlowEdge));
                    networkCopy.setEdgeWeight(annotatedFlowEdge, Double.POSITIVE_INFINITY);
                }
                DijkstraShortestPath dijkstra = new DijkstraShortestPath(networkCopy);
                GraphPath newPath = dijkstra.getPath(demand.source, demand.sink);
                if (newPath != null) {
                    pathsExist = true;
                    double newPathWeight = newPath.getWeight();
                    if (comparator.compare(newPathWeight, shortestPathWeight) < 0) {
                        shortestPathWeight = newPathWeight;
                        shortestPath = newPath;
                        currentDemandFlowIsPushedAlong = demand;
                    }
                }

                // add edges that were removed
                for (AnnotatedFlowEdge annotatedFlowEdge : saveTheWeightsOfTheEdges.keySet()) {
                    networkCopy.setEdgeWeight(annotatedFlowEdge, saveTheWeightsOfTheEdges.get(annotatedFlowEdge));
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
                e.demandFlows.put(currentDemandFlowIsPushedAlong,
                        e.demandFlows.get(currentDemandFlowIsPushedAlong) + smallestCapacity);

            }
            maxFlowValue += smallestCapacity;
            maxFlowValueForEachDemand.put(currentDemandFlowIsPushedAlong, maxFlowValueForEachDemand.get(currentDemandFlowIsPushedAlong) + smallestCapacity);
            counter++;
            // System.out.println(counter);
        }
        //scale the flow to make it feasible
        breakingCriterionsAndEdgeScalingObject.finish();

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

        public VertexExtension() {

            this.isAHelperVertex = false;
        }

        VertexExtensionBase helperVertex;

    }

    // handle the breaking conditions, the best primal/dual solutions and the scaling
    private class BreakingCriterionsAndEdgeScalingObject {
        double maxPrimalObjectiveFunction = 0.0;
        double minDualObjectiveFunction = Double.POSITIVE_INFINITY;
        double bestMaxFlowValue = 0.0;
        int divisionCounter = 0;
        protected Map<Demand, Double> bestMaxFlowValueForEachDemand = new HashMap<>();
        Map<E, Double> bestmaxFlow = null;
        public Map<Demand, Map<E, Double>> bestmapOfFlowsForEachDemand = new HashMap<>();

        public boolean actualizeStats(boolean pathsExist, GraphPath shortestPath) {

            // if there are no valid paths, break and set flow = zeroMapping
            if (!pathsExist) {
                System.out.println("There are no valid paths from a source to its sink");
                // we have to compose the flow...
                bestMaxFlowValue = 0;
                bestmaxFlow = composeFlow();
                for (Demand demand : demands) {
                    bestmapOfFlowsForEachDemand.put(demand, composeFlow(demand));
                    bestMaxFlowValueForEachDemand.put(demand, 0.0);
                }
                return true;
            }
            double shortestPathWeight = shortestPath.getWeight();
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
                for (Demand demand : demands) {
                    bestMaxFlowValueForEachDemand.put(demand, maxFlowValueForEachDemand.get(demand));
                    bestmapOfFlowsForEachDemand.put(demand, composeFlow(demand));

                }
            }
            // we update the value of the length function if the value of the current length function is better
            double intermediateDualObjectiveFunction = Math.pow(shortestPathWeight, -1) * networkCopy.edgeSet().stream().mapToDouble(e -> networkCopy.getEdgeWeight(e) * e.capacity).sum();
            if (comparator.compare(intermediateDualObjectiveFunction, minDualObjectiveFunction) <= 0) {
                minDualObjectiveFunction = intermediateDualObjectiveFunction;
            }
            // if the ratio between the best flow and the best length function is small enough, we end the algorithm
            if (comparator.compare(minDualObjectiveFunction / maxPrimalObjectiveFunction, 1 + approximationRate) <= 0) {
                // System.out.println(1 / shortestPathWeight);
                return true;
            }
            return false;
        }

        public void finish() {

            maxFlowValue = this.bestMaxFlowValue;
            maxFlow = this.bestmaxFlow;
            mapOfFlowsForEachDemand = this.bestmapOfFlowsForEachDemand;
            maxFlowValueForEachDemand = this.bestMaxFlowValueForEachDemand;
            Double mostViolatedEdgeViolation = 0.0;
            for (E e : maxFlow.keySet()) {
                if (comparator.compare(maxFlow.get(e) / network.getEdgeWeight(e), mostViolatedEdgeViolation) > 0) {
                    mostViolatedEdgeViolation = maxFlow.get(e) / network.getEdgeWeight(e);
                }
            }
            if (comparator.compare(mostViolatedEdgeViolation, 0.0) == 0) {
                return;
            }
            maxFlowValue /= mostViolatedEdgeViolation;
            for (E e : network.edgeSet()) {
                maxFlow.put(e, maxFlow.get(e) / mostViolatedEdgeViolation);
                for (Demand demand : demands) {
                    mapOfFlowsForEachDemand.get(demand).put(e, mapOfFlowsForEachDemand.get(demand).get(e) / mostViolatedEdgeViolation);
                }
            }
            for (Demand demand : demands) {
                maxFlowValueForEachDemand.put(demand, maxFlowValueForEachDemand.get(demand) / mostViolatedEdgeViolation);
            }

        }
    }

/*


    public class Demand extends MaximumMultiCommodityFlowAlgorithmBase.Demand {

        Demand(VertexExtension source, VertexExtension sink) {
            super(source, sink);
        }

    }
*/

}