package org.jgrapht.alg.flow;


import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.extension.ExtensionFactory;
import org.jgrapht.graph.AbstractBaseGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 * @author Philipp
 */

public class GargAndKoenemannMMCFImp<V, E>
        extends
        MaximumMultiCommodityFlowAlgorithmBase<V, E> {

    private final ExtensionFactory<VertexExtension> vertexExtensionsFactory;
    private final ExtensionFactory<AnnotatedFlowEdge> edgeExtensionsFactory;
    private Demand currentDemandFlowIsPushedAlong;
    private double approximationRate = 0.0;

    /**
     * Constructor. Constructs a new network on which we will calculate the maximum flow, using GargAndKoenemann
     * algorithm.
     *
     * @param network the network on which we calculate the maximum flow.
     * @param epsilon the tolerance for the comparison of floating point values.
     */
    public GargAndKoenemannMMCFImp(AbstractBaseGraph<V, E> network, double epsilon) {
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
    public GargAndKoenemannMMCFImp(AbstractBaseGraph<V, E> network) {
        this(network, DEFAULT_EPSILON);
    }

    @Override
    public MaximumFlow<E> getMaximumFlow(List<V> sources, List<V> sinks, double approximationRate,
                                         List<List<E>> allClosedEdgesForADemand) {
        calculateMaxFlow(sources, sinks, approximationRate, allClosedEdgesForADemand);
        return new MaximumMultiCommodityFlowImpl(maxFlowValue, maxFlow, maxFlowValueForEachDemand,
                mapOfFlowsForEachDemand);
    }


    /**
     * calculate the maximum flow in the network using GargAndKoenemann algorithm with scaling.
     *
     * @param sources                  source vertices.
     * @param sinks                    sink vertices.
     * @param approximationRate        approximation rate.
     * @param allClosedEdgesForADemand List that contains Lists of edges which are closed for the corresponding demand.
     * @return the value of the maximum flow in the network.
     */
    private double calculateMaxFlow(List<V> sources, List<V> sinks, double approximationRate,
                                    List<List<E>> allClosedEdgesForADemand) {
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
        gargAndKoenemann();
        return maxFlowValue;
    }

    /**
     * the computation of the MMCF
     */
    private void gargAndKoenemann() {

        BreakingCriterionsAndEdgeScalingObject breakingCriterionsAndEdgeScalingObject =
                new BreakingCriterionsAndEdgeScalingObject();
        while (true) {
            /*choose shortest path, its value, its demand*/
            boolean pathsExist = false;
            double shortestPathWeight = Double.POSITIVE_INFINITY;
            GraphPath<VertexExtensionBase, AnnotatedFlowEdge> shortestPath = null;
            for (Demand demand : networkCopyForEachDemand.keySet()) {
                DijkstraShortestPath dijkstra = new DijkstraShortestPath(networkCopyForEachDemand.get(demand));
                GraphPath newPath = dijkstra.getPath(demand.source, demand.sink);
                if (newPath != null) {
                    double newPathWeight = newPath.getWeight();
                    if (comparator.compare(newPathWeight, shortestPathWeight) < 0) {
                        pathsExist = true;
                        shortestPathWeight = newPathWeight;
                        shortestPath = newPath;
                        currentDemandFlowIsPushedAlong = demand;
                    }
                }
            }
            if (breakingCriterionsAndEdgeScalingObject.actualizeStats(pathsExist, shortestPath)) {
                break;
            }
            /*get smallest capacity*/
            Double smallestCapacity = Double.POSITIVE_INFINITY;
            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {
                double newCapacity = e.capacity;
                if (comparator.compare(newCapacity, smallestCapacity) < 0) {
                    smallestCapacity = e.capacity;
                }
            }
            /*update length and flow(value) (also for each demand)*/
            for (AnnotatedFlowEdge e : shortestPath.getEdgeList()) {
                for (Graph<VertexExtensionBase, AnnotatedFlowEdge> currentGraph : networkCopyForEachDemand.values()) {
                    if (currentGraph.containsEdge(e)) {
                        currentGraph.setEdgeWeight(e,
                                currentGraph.getEdgeWeight(e) + currentGraph.getEdgeWeight(e) * accuracy * (smallestCapacity / e.capacity));
                    }
                }
                networkCopy.setEdgeWeight(e,
                        networkCopy.getEdgeWeight(e) + networkCopy.getEdgeWeight(e) * accuracy * (smallestCapacity / e.capacity));
                e.flow = e.flow + smallestCapacity;
                /*demandFlowMap*/
                e.demandFlows.put(currentDemandFlowIsPushedAlong,
                        e.demandFlows.get(currentDemandFlowIsPushedAlong) + smallestCapacity);
            }
            maxFlowValue += smallestCapacity;
            maxFlowValueForEachDemand.put(currentDemandFlowIsPushedAlong,
                    maxFlowValueForEachDemand.get(currentDemandFlowIsPushedAlong) + smallestCapacity);
        }
        /*scale the flow to make it feasible*/
        breakingCriterionsAndEdgeScalingObject.finish();
    }


    /**
     * not needed yet
     */
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

    /**
     * In this object wer temporary save the best primal/dual values we have obtained in any iteration so far. If we get
     * better ones in the current iteration, they are updated. Furthermore we check if any stopping criterion is
     * fulfilled. Moreover we save the primal value for each demand, and the complete MMCF and the MF for each demand.
     */
    private class BreakingCriterionsAndEdgeScalingObject {
        double maxPrimalObjectiveFunction = 0.0;
        double minDualObjectiveFunction = Double.POSITIVE_INFINITY;
        double bestMaxFlowValue = 0.0;
        int divisionCounter = 0;
        Map<Demand, Double> bestMaxFlowValueForEachDemand = new HashMap<>();
        Map<E, Double> bestMaxFlow = null;
        Map<Demand, Map<E, Double>> bestmapOfFlowsForEachDemand = new HashMap<>();

        private boolean actualizeStats(boolean pathsExist, GraphPath shortestPath) {

            /* if there are no valid paths, break and set flow to a zeroMapping*/
            if (!pathsExist) {
                System.out.println("There are no valid paths from a source to its sink");
                // we have to compose the flow...
                bestMaxFlowValue = 0;
                bestMaxFlow = composeFlow();
                for (Demand demand : demands) {
                    bestmapOfFlowsForEachDemand.put(demand, composeFlow(demand));
                    bestMaxFlowValueForEachDemand.put(demand, 0.0);
                }
                return true;
            }
            double shortestPathWeight = shortestPath.getWeight();
            /* breaking condition, we stop when shortest path hast length bigger or equal to 1 */
            if (comparator.compare(shortestPathWeight, delta * Math.pow(lengthOfLongestPath * (1 + accuracy),
                    1 / accuracy - divisionCounter) / (1 + accuracy)) >= 0) {
                return true;
            }

            /* check if we need to update the length of the edges */
            boolean scaleLengthOfAllEdges = true;
            for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {
                if (comparator.compare(networkCopy.getEdgeWeight(e), delta * lengthOfLongestPath) * (1 + accuracy) <= 0) {
                    scaleLengthOfAllEdges = false;
                }
            }
            /* here we do the scaling of the edges */
            if (scaleLengthOfAllEdges) {
                for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {
                    networkCopy.setEdgeWeight(e, networkCopy.getEdgeWeight(e) / (lengthOfLongestPath * (1 + accuracy)));
                }
            }
            /* finish earlier with acceptable result.
             first we find out which edge is most violated, in order to get a feasible solution */
            double mostViolatedEdgeViolation = 1.0;
            for (AnnotatedFlowEdge e : networkCopy.edgeSet()) {
                if (comparator.compare(e.flow / e.capacity, mostViolatedEdgeViolation) > 0) {
                    mostViolatedEdgeViolation = e.flow / e.capacity;
                }
            }
            /* we update the value of the best flow, if the the value of the current feasible flow is better */
            double intermediatePrimalObjectiveFunction = maxFlowValue / mostViolatedEdgeViolation;
            if (comparator.compare(intermediatePrimalObjectiveFunction, maxPrimalObjectiveFunction) >= 0) {
                maxPrimalObjectiveFunction = intermediatePrimalObjectiveFunction;
                bestMaxFlowValue = maxFlowValue;
                bestMaxFlow = composeFlow();
                for (Demand demand : demands) {
                    bestMaxFlowValueForEachDemand.put(demand, maxFlowValueForEachDemand.get(demand));
                    bestmapOfFlowsForEachDemand.put(demand, composeFlow(demand));

                }
            }
            /* we update the value of the length function if the value of the current length function is better*/
            double intermediateDualObjectiveFunction =
                    Math.pow(shortestPathWeight, -1) * networkCopy.edgeSet().stream().mapToDouble(e -> networkCopy.getEdgeWeight(e) * e.capacity).sum();
            if (comparator.compare(intermediateDualObjectiveFunction, minDualObjectiveFunction) <= 0) {
                minDualObjectiveFunction = intermediateDualObjectiveFunction;
            }
            /* if the ratio between the best flow and the best length function is small enough, we end the algorithm*/
            if (comparator.compare(minDualObjectiveFunction / maxPrimalObjectiveFunction, 1 + approximationRate) <= 0) {
                System.out.println("test" + shortestPath);
                return true;
            }
            return false;
        }

        /* with this method we set the final flow to be the best flow so far obtained and scale it */
        public void finish() {
            maxFlowValue = this.bestMaxFlowValue;
            maxFlow = this.bestMaxFlow;
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
                    mapOfFlowsForEachDemand.get(demand).put(e,
                            mapOfFlowsForEachDemand.get(demand).get(e) / mostViolatedEdgeViolation);
                }
            }
            for (Demand demand : demands) {
                maxFlowValueForEachDemand.put(demand,
                        maxFlowValueForEachDemand.get(demand) / mostViolatedEdgeViolation);
            }
        }
    }

    /* not needed yet
     public class Demand extends MaximumMultiCommodityFlowAlgorithmBase.Demand {
     Demand(VertexExtension source, VertexExtension sink) {
     super(source, sink);
     }
     }
     */

}