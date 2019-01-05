package org.jgrapht.alg.flow;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MaximumMultiCommodityFlowAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.ToleranceDoubleComparator;
import org.jgrapht.alg.util.extension.Extension;
import org.jgrapht.alg.util.extension.ExtensionFactory;
import org.jgrapht.alg.util.extension.ExtensionManager;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.*;
import java.util.function.Supplier;

/**
 * Base class backing algorithms allowing to derive
 * <a href="https://en.wikipedia.org/wiki/Maximum_flow_problem">maximum-flow</a> from the supplied
 * <a href="https://en.wikipedia.org/wiki/Flow_network">flow network</a>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Alexey Kudinkin
 * @author Joris Kinable
 */
public abstract class MaximumMultiCommodityFlowAlgorithmBase<V, E>
        implements
        MaximumMultiCommodityFlowAlgorithm<V, E> {
    /**
     * Default tolerance.
     */
    public static final double DEFAULT_EPSILON = 1e-9;

    /* input network */
    protected Graph<V, E> network;
    /* indicates whether the input graph is directed or not */
    protected final boolean directedGraph;
    /* Used to compare floating point values */
    protected Comparator<Double> comparator;

    public ExtensionManager<V, ? extends VertexExtensionBase> vertexExtensionManager;
    protected ExtensionManager<E, ? extends AnnotatedFlowEdge> edgeExtensionManager;

    /* Max flow established after last invocation of the algorithm. */
    protected double maxFlowValue = -1;
    // list of the values foe each demand flow
    protected Map<Pair<V, V>, Double> maxFlowValueForEachDemand = null;
    /* Mapping of the flow on each edge. */
    protected Map<E, Double> maxFlow = null;
    /* List of mappings for each demand, */
    public Map<Pair<V, V>, Map<E, Double>> mapOfFlowsForEachDemand = null;
    int divisionCounter = 0;
    /* A copy of the network, that uses a length function as weights, needed for dijkstraShortestPath*/
    public Graph<VertexExtensionBase, AnnotatedFlowEdge> networkCopy;
    /* the weight that the copied edges are initialized with*/
    protected double delta = 0;
    /* accuracy depending on the wanted approximation rate*/
    protected double accuracy = 0;
    /* save a little bit of computation time*/
    int demandSize = 0;
    /*  need for the initalization*/
    double lengthOfLongestPath = 0.0;
    /* representation of sources and sinks*/
    List<Pair<VertexExtensionBase, VertexExtensionBase>> demands = null;

    /**
     * Construct a new maximum flow
     *
     * @param network the network
     * @param epsilon the tolerance for the comparison of floating point values
     */
    public MaximumMultiCommodityFlowAlgorithmBase(Graph<V, E> network, double epsilon) {

        this.network = network;
        this.directedGraph = network.getType().isDirected();
        this.comparator = new ToleranceDoubleComparator(epsilon);
        // network copy
        Supplier<VertexExtensionBase> vertexExtensionSupplier = () -> new VertexExtensionBase();
        Supplier<AnnotatedFlowEdge> annotatedFlowEdgeSupplier = () -> new AnnotatedFlowEdge();
        this.networkCopy = new DefaultDirectedWeightedGraph(vertexExtensionSupplier, annotatedFlowEdgeSupplier);
        lengthOfLongestPath = network.vertexSet().size();
    }

    /**
     * Prepares all data structures to start a new invocation of the Maximum Flow or Minimum Cut
     * algorithms
     *
     * @param sources                source
     * @param sinks                  sink
     * @param vertexExtensionFactory vertex extension factory
     * @param edgeExtensionFactory   edge extension factory
     * @param <VE>                   vertex extension type
     */
    protected <VE extends VertexExtensionBase> void init(double approximationRate,
                                                         List<V> sources, List<V> sinks, ExtensionFactory<VE> vertexExtensionFactory,
                                                         ExtensionFactory<AnnotatedFlowEdge> edgeExtensionFactory) {

        vertexExtensionManager = new ExtensionManager<>(vertexExtensionFactory);
        edgeExtensionManager = new ExtensionManager<>(edgeExtensionFactory);
        this.demandSize = sinks.size();
        demands = new LinkedList<Pair<VertexExtensionBase, VertexExtensionBase>>();
        for (int i = 0; i < demandSize; i++) {
            VertexExtensionBase source = vertexExtensionManager.getExtension(sources.get(i));
            VertexExtensionBase sink = vertexExtensionManager.getExtension(sinks.get(i));
            source.prototype = sources.get(i);
            sink.prototype = sinks.get(i);
            demands.add(new Pair(source, sink));

        }
        buildInternal();
        maxFlowValue = 0;
        maxFlow = null;
        mapOfFlowsForEachDemand = null;
        maxFlowValueForEachDemand = new HashMap<>();
        for (Pair<VertexExtensionBase, VertexExtensionBase> demand : demands) {
            maxFlowValueForEachDemand.put(new Pair(demand.getFirst().prototype, demand.getSecond().prototype), 0.0);
        }
    }

    /**
     * Create internal data structure
     */
    void buildInternal() {
        if (directedGraph) { // Directed graph

            // add vertices to networkCopy
            for (V v : network.vertexSet()) {
                VertexExtensionBase vx = vertexExtensionManager.getExtension(v);
                vx.prototype = v;
                networkCopy.addVertex(vx);
            }

            // add edges to network copy
            for (E e : network.edgeSet()) {
                V u = network.getEdgeTarget(e);
                VertexExtensionBase ux = vertexExtensionManager.getExtension(u);
                V v = network.getEdgeSource(e);
                VertexExtensionBase vx = vertexExtensionManager.getExtension(v);
                AnnotatedFlowEdge edgeCopy = createEdge(vx, ux, e, network.getEdgeWeight(e));

                // get rid of edges with zero capacity
                if (comparator.compare(edgeCopy.capacity, 0.0) > 0) {
                    networkCopy.addEdge(vx, ux, edgeCopy);
                    networkCopy.setEdgeWeight(vx, ux, delta);
                }
            }

        } else { // Undirected graph
            for (V v : network.vertexSet()) {
                VertexExtensionBase vx = vertexExtensionManager.getExtension(v);
                vx.prototype = v;
            }
            for (E e : network.edgeSet()) {
                VertexExtensionBase ux =
                        vertexExtensionManager.getExtension(network.getEdgeSource(e));
                VertexExtensionBase vx =
                        vertexExtensionManager.getExtension(network.getEdgeTarget(e));
                AnnotatedFlowEdge forwardEdge = createEdge(ux, vx, e, network.getEdgeWeight(e));
                //   AnnotatedFlowEdge backwardEdge = createBackwardEdge(forwardEdge);
                //ux.getOutgoing().add(forwardEdge);
                //   vx.getOutgoing().add(backwardEdge);
            }
        }


    }


    private AnnotatedFlowEdge createEdge(
            VertexExtensionBase source, VertexExtensionBase target, E e, double weight) {
        AnnotatedFlowEdge ex = edgeExtensionManager.getExtension(e);
        ex.source = source;
        ex.target = target;
        ex.capacity = weight;
        ex.prototype = e;

        // FlowMap
        ex.demandFlows = new HashMap();
        for (Pair demand : demands) {
            ex.demandFlows.put(demand, 0.0);
        }


        return ex;
    }
/*
    private AnnotatedFlowEdge createBackwardEdge(AnnotatedFlowEdge forwardEdge) {
        AnnotatedFlowEdge backwardEdge;
        E backwardPrototype =
                network.getEdge(forwardEdge.target.prototype, forwardEdge.source.prototype);

        if (directedGraph && backwardPrototype != null) { // if edge exists in directed input graph
            backwardEdge = createEdge(
                    forwardEdge.target, forwardEdge.source, backwardPrototype,
                    network.getEdgeWeight(backwardPrototype));
        } else {
            backwardEdge = edgeExtensionManager.createExtension();
            backwardEdge.source = forwardEdge.target;
            backwardEdge.target = forwardEdge.source;
            if (!directedGraph) { // Undirected graph: if (u,v) exists, then so much (v,u)
                backwardEdge.capacity = network.getEdgeWeight(backwardPrototype);
                backwardEdge.prototype = backwardPrototype;
            }
        }

        forwardEdge.inverse = backwardEdge;
        backwardEdge.inverse = forwardEdge;

        return backwardEdge;
    }

*/


    /**
     * Increase flow in the direction denoted by edge $(u,v)$. Any existing flow in the reverse
     * direction $(v,u)$ gets reduced first. More precisely, let $f_2$ be the existing flow in the
     * direction $(v,u)$, and $f_1$ be the desired increase of flow in direction $(u,v)$. If $f_1
     * \geq f_2$, then the flow on $(v,u)$ becomes $0$, and the flow on $(u,v)$ becomes $f_1-f_2$.
     * Else, if $f_1 \textlptr f_2$, the flow in the direction $(v, u)$ is reduced, i.e. the flow on
     * $(v, u)$ becomes $f_2 - f_1$, whereas the flow on $(u,v)$ remains zero.
     *
     * @param edge desired direction in which the flow is increased
     * @param flow increase of flow in the the direction indicated by the forwardEdge
     */
    protected void pushFlowThrough(AnnotatedFlowEdge edge, double flow) {
        AnnotatedFlowEdge inverseEdge = edge.getInverse();

        assert ((comparator.compare(edge.flow, 0.0) == 0)
                || (comparator.compare(inverseEdge.flow, 0.0) == 0));

        if (comparator.compare(inverseEdge.flow, flow) < 0) { // If f_1 >= f_2
            double flowDifference = flow - inverseEdge.flow;

            edge.flow += flowDifference;
            edge.capacity -= inverseEdge.flow; // Capacity on edge (u,v) PLUS flow on (v,u) gives
            // the MAXIMUM flow in the direction (u,v) i.e
            // edge.weight in the graph 'network'.

            inverseEdge.flow = 0;
            inverseEdge.capacity += flowDifference;
        } else { // If f1 < f2
            edge.capacity -= flow;
            inverseEdge.flow -= flow;
        }
    }

    /**
     * Create a map which specifies for each edge in the input map the amount of flow that flows
     * through it, added multiple maps for every demand
     *
     * @return a map which specifies for each edge in the input map the amount of flow that flows
     * through it
     */
    protected Map<E, Double> composeFlow() {
        Map<E, Double> maxFlow = new HashMap<>();
        for (E e : network.edgeSet()) {
            // total flow
            AnnotatedFlowEdge annotatedFlowEdge = edgeExtensionManager.getExtension(e);
            maxFlow.put(
                    e, directedGraph ? annotatedFlowEdge.flow
                            : Math.max(annotatedFlowEdge.flow, annotatedFlowEdge.inverse.flow));
        }
        return maxFlow;
    }

    protected Map<E, Double> composeFlow(V source, V sink) {
        Map<E, Double> maxFlow = new HashMap<>();
        if (mapOfFlowsForEachDemand == null) {
            mapOfFlowsForEachDemand = new HashMap<>();
            for (Pair<VertexExtensionBase, VertexExtensionBase> demand : demands) {
                Pair<V, V> vDemand = new Pair<V, V>(demand.getFirst().prototype, demand.getSecond().prototype);
                mapOfFlowsForEachDemand.put(vDemand, new HashMap<>());
                for (E e :
                        network.edgeSet()) {
                    mapOfFlowsForEachDemand.get(vDemand).put(e, 0.0);
                }
            }
        }
        for (E e : network.edgeSet()) {
            AnnotatedFlowEdge annotatedFlowEdge = edgeExtensionManager.getExtension(e);
            // Flow of demand
            Pair<V, V> vDemand = new Pair(source, sink);
            mapOfFlowsForEachDemand.get(vDemand).put(e, mapOfFlowsForEachDemand.get(vDemand).get(e) + annotatedFlowEdge.demandFlows.get(new Pair(vertexExtensionManager.getExtension(source), vertexExtensionManager.getExtension(sink))));
        }

        return maxFlow;
    }


    class VertexExtensionBase
            implements
            Extension {


        //private final List<AnnotatedFlowEdge> outgoing = new ArrayList<>();

        V prototype;


        // to String override

        public String toString() {
            return this.prototype.toString();
        }

        //public List<AnnotatedFlowEdge> getOutgoing() {
        //  return outgoing;
        // }
    }

    class AnnotatedFlowEdge
            implements
            Extension {
        /* Edge source */
        private VertexExtensionBase source;
        /* Edge target */
        private VertexExtensionBase target;
        /* Inverse edge */
        private AnnotatedFlowEdge inverse;


        E prototype; // Edge
        double capacity; // Maximum by which the flow in the direction can be increased (on top of
        // the flow already in this direction).
        double flow; // Flow in the direction denoted by this edge


        // new Data Structure[
        Map<Pair<VertexExtensionBase, VertexExtensionBase>, Double> demandFlows; // Flow for each demand in the direction denoted by the edge
        // ]new Data Structure

        public <VE extends VertexExtensionBase> VE getSource() {
            return (VE) source;
        }

        public void setSource(VertexExtensionBase source) {
            this.source = source;
        }

        public <VE extends VertexExtensionBase> VE getTarget() {
            return (VE) target;
        }

        public void setTarget(VertexExtensionBase target) {
            this.target = target;
        }

        public AnnotatedFlowEdge getInverse() {
            return inverse;
        }

        public boolean hasCapacity() {
            return comparator.compare(capacity, flow) > 0;
        }

        @Override
        public String toString() {
            return "(" + (source == null ? null : source.prototype) + ","
                    + (target == null ? null : target.prototype) + ",c:" + capacity + " f: " + flow
                    + ")";
        }
    }

    /**
     * Returns current source vertex, or <tt>null</tt> if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return current source
     */


    //public List<V> getCurrentSource() {
    //    return sources;
    // }

    /**
     * Returns current sink vertex, or <tt>null</tt> if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return current sink
     */
    // public List<V> getCurrentSink() {
    //    return sinks;
    //}

    /**
     * Returns maximum flow value, that was calculated during last <tt>
     * calculateMaximumFlow</tt> call.
     *
     * @return maximum flow value
     */
    public double getMaximumFlowValue() {
        return maxFlowValue;
    }

    /**
     * Returns maximum flow, that was calculated during last <tt>
     * calculateMaximumFlow</tt> call, or <tt>null</tt>, if there was no <tt>
     * calculateMaximumFlow</tt> calls.
     *
     * @return <i>read-only</i> mapping from edges to doubles - flow values
     */
    public Map<E, Double> getFlowMap() {
        if (maxFlow == null) // Lazily calculate the max flow map
            maxFlow = composeFlow();
        return maxFlow;
    }

    //get flow for specific demand
    public Map<E, Double> getFlowMapOfDemand(V source, V sink) {
        Map<E, Double> flow = null;
        if (mapOfFlowsForEachDemand == null) {
            composeFlow(source, sink);
        }
        flow = mapOfFlowsForEachDemand.get(new Pair(source, sink));
        return flow;
    }


    /**
     * Returns the direction of the flow on an edge $(u,v)$. In case $(u,v)$ is a directed edge
     * (arc), this function will always return the edge target $v$. However, if $(u,v)$ is an edge
     * in an undirected graph, flow may go through the edge in either side. If the flow goes from
     * $u$ to $v$, we return $v$, otherwise $u$. If the flow on an edge equals $0$, the returned
     * value has no meaning.
     *
     * @param e edge
     * @return the vertex where the flow leaves the edge
     */
    public V getFlowDirection(E e) {
        if (!network.containsEdge(e))
            throw new IllegalArgumentException(
                    "Cannot query the flow on an edge which does not exist in the input graph!");
        AnnotatedFlowEdge annotatedFlowEdge = edgeExtensionManager.getExtension(e);

        if (directedGraph)
            return annotatedFlowEdge.getTarget().prototype;

        AnnotatedFlowEdge inverseEdge = annotatedFlowEdge.getInverse();
        if (annotatedFlowEdge.flow > inverseEdge.flow)
            return annotatedFlowEdge.getTarget().prototype;
        else
            return inverseEdge.getTarget().prototype;
    }

}


