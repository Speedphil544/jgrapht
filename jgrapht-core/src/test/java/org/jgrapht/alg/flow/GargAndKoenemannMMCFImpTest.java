package org.jgrapht.alg.flow;

import org.jgrapht.alg.interfaces.MaximumMultiCommodityFlowAlgorithm;
import org.jgrapht.alg.util.ToleranceDoubleComparator;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class GargAndKoenemannMMCFImpTest {


    private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g;

    private MaximumMultiCommodityFlowAlgorithm<String, DefaultWeightedEdge> gargAndKoenemann;

    private DefaultWeightedEdge edge;

    private final String v1 = "v1";

    private final String v2 = "v2";

    private final String v3 = "v3";

    private final double approximationRate = 0.1;

    private double epsilon = 1e-200;

    private final Comparator<Double> comparator = new ToleranceDoubleComparator(epsilon);

    private double expectedFlow;


    private DefaultDirectedWeightedGraph createRandomGraph(int size, double prop, double min, double max) {
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g =
                new DefaultDirectedWeightedGraph(DefaultWeightedEdge.class);
        Random random = new Random();
        List<String> vertices = new LinkedList();
        for (int i = 1; i <= size; i++) {
            g.addVertex("v" + Integer.toString(i));
        }
        for (String vertex1 : g.vertexSet()) {
            for (String vertex2 : g.vertexSet()) {
                if (vertex1 != vertex2) {
                    if (prop - random.nextDouble() >= 0) {
                        DefaultWeightedEdge e = g.addEdge(vertex1, vertex2);
                        double weight = min + random.nextDouble() * (max - min);
                        g.setEdgeWeight(e, weight);
                    }
                }
            }
        }
        return g;
    }


    @Before
    public void init() {
        Supplier<String> vertexSupplier = () -> new String();
        Supplier<DefaultWeightedEdge> edgeSupplier = () -> new DefaultWeightedEdge();
        g = new DefaultDirectedWeightedGraph(vertexSupplier, edgeSupplier);
    }


    @Test
    /**
     Here we test the algorithm on a random graph (properties of the random of the graph can be modified)
     */
    public void RandomGraphTest() {
        g = createRandomGraph(3, 1, 1, 3);
        List<String> sources = new LinkedList();
        sources.add("v1");
        List<String> sinks = new LinkedList();
        sinks.add("v3");
        gargAndKoenemann = new GargAndKoenemannMMCFImp(g, epsilon);
        List<List<DefaultWeightedEdge>> closedEdges = new LinkedList<>();
        closedEdges.add(new LinkedList());
        closedEdges.add(new LinkedList());
        closedEdges.add(new LinkedList());
        MaximumMultiCommodityFlowAlgorithm.MaximumFlow flow = gargAndKoenemann.getMaximumFlow(sources, sinks,
                approximationRate, closedEdges);
        System.out.println(flow);

    }


    @Test
    /**
     Here we test the algorithm on a graph with 3 nodes (1,2,3)
     and two edges ([1,2],[2,3]). The edges have very different capacities.
     The demand: (1,2).
     We notice: the length of the edge with the higher capacity grows much slower than the one with the lower capacity.
     This might lead to a problem: we dont want to have a big ratio of our edge lengths.
     */
    public void Test1a() {


        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        System.out.println(g);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 0.1);
        edge = g.addEdge(v1, v3);
        g.setEdgeWeight(edge, 0.1);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 1);
        List<List<DefaultWeightedEdge>> closedEdges = new LinkedList<>();
        closedEdges.add(new LinkedList());
        closedEdges.add(new LinkedList());
        closedEdges.add(new LinkedList());
        closedEdges.get(1).add(edge);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        List<String> sources = new LinkedList();
        sources.add(v1);
        sources.add(v2);
        List<String> sinks = new LinkedList();
        sinks.add(v3);
        sinks.add(v3);
        double flow = 1.0;
        System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate, closedEdges));
        System.out.println(gargAndKoenemann.getFlowMapOfDemand(v1, v3));

    }

    @Test
    /**
     nodes: (1,2,3,4,5,6), edges: ([1,3],[2,3],[3,4],[4,5],[5,6]),  demands: (1,5) (2,6).
     Again, we have very different edge weight(take a look at the code).
     edge [3,4] grows fastest by far!
     [3,4] has less capacity than [1,3] and [4,5]. That is why [1,3] grows faster, than [1,3] and [4,5].
     But [2,3] and [4,6] grow faste than [1,3] und [4,5], since we always choose the shortest path.
     ->  [3,4] grows faster than all the other edges
     */
    public void Test1b() {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        String v4 = "v4";
        String v5 = "v5";
        String v6 = "v6";
        g.addVertex(v4);
        g.addVertex(v5);
        g.addVertex(v6);
        edge = g.addEdge(v1, v3);
        g.setEdgeWeight(edge, 10000);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 1);
        edge = g.addEdge(v3, v4);
        g.setEdgeWeight(edge, 100);
        edge = g.addEdge(v4, v5);
        g.setEdgeWeight(edge, 1000);
        edge = g.addEdge(v4, v6);
        g.setEdgeWeight(edge, 1);
        List<String> sources = new LinkedList();
        sources.add(v1);
        sources.add(v2);
        List<String> sinks = new LinkedList();
        sinks.add(v5);
        sinks.add(v6);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        //System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));


    }

    @Test
    /**Here we test the algorithm on a graph with following edges(and corresponding nodes ofc.): ([1,2],[2,3],[3,4],.
     * ..), c(e)=1.0 f.a. e in E. Two demands: (1,2), (2,n). We notice: everything works fine.
     */
    public void Test1c() {
        int numberOfEdges = 10000;
        for (int i = 0; i < numberOfEdges; i++) {
            g.addVertex("v" + Integer.toString(i));
        }
        edge = g.addEdge("v0", "v1");
        g.setEdgeWeight(edge, 0.00000001);
        edge = g.addEdge("v1", "v2");
        g.setEdgeWeight(edge, 1000000000);

        for (int i = 2; i < numberOfEdges - 1; i++) {
            edge = g.addEdge("v" + Integer.toString(i), "v" + Integer.toString(i + 1));
            g.setEdgeWeight(edge, 1000000000);
        }
        List<String> sources = new LinkedList();
        sources.add("v0");

        List<String> sinks = new LinkedList();
        sinks.add("v9999");

        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        // System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));
    }

    @Test
    /**
     * Here we test the algorithm on a graph with following edges(and corresponding nodes ofc.): ([1,2],[2,3],[3,4],.
     * ..),c(e)=1.0 f.a. e in E. Two demands: (1,2), (2,n). We notice: everything works fine.
     */
    public void Test2() {
        int numberOfEdges = 1000;
        for (int i = 0; i < numberOfEdges; i++) {
            g.addVertex("v" + Integer.toString(i));
        }
        for (int i = 0; i < numberOfEdges - 1; i++) {
            edge = g.addEdge("v" + Integer.toString(i), "v" + Integer.toString(i + 1));
            g.setEdgeWeight(edge, 1.0);
        }
        List<String> sources = new LinkedList();
        sources.add("v0");
        sources.add("v1");
        List<String> sinks = new LinkedList();
        sinks.add("v1");
        sinks.add("v" + Integer.toString(numberOfEdges - 1));
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        // System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));
    }


    @Test(expected = IllegalArgumentException.class)
    /**
     We test what happens if there is no sink
     */
    public void Test3a() {
        g.addVertex(v1);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList<>();
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        // double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     We test what happens if a source is equal to its sink
     */
    public void Test3b() {
        g.addVertex(v1);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList<>();
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        // double flow = gargAndKoenemann.getMaximumFlowValue(sources, sources, approximationRate);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     We test what happens if we have not the same number of sources and sinks
     */
    public void Test3c() {
        g.addVertex(v1);
        g.addVertex(v2);
        List<String> sources = new LinkedList();
        sources.add(v1);
        sources.add(v2);
        List<String> sinks = new LinkedList<>();
        sinks.add(v1);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        //double flow = gargAndKoenemann.getMaximumFlowValue(sources, sources, approximationRate);
    }

    @Test
    /**
     We test what happens if there are no valid paths
     */
    public void Test3d() {
        g.addVertex(v1);
        g.addVertex(v2);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v2);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        //double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        //System.out.println(flow);
    }


    @Test
    /** We have nodes (1,2,3,4,5...,special,end), edges([1,special],[2,special],[3,special],..., [special,
     end]), and demands: (1,end),(2,end),(3,end),...    all capacities equal 1.0
     */
    public void Test4() {
        List<String> sources = new LinkedList();
        List<String> sinks = new LinkedList();
        int demandSize = 100;
        for (int i = 0; i < demandSize; i++) {
            g.addVertex("v" + Integer.toString(i));
            sources.add("v" + Integer.toString(i));
            sinks.add("end");
        }
        g.addVertex("special");
        g.addVertex("end");
        edge = g.addEdge("special", "end");
        g.setEdgeWeight(edge, 100.0);
        for (int i = 0; i < demandSize; i++) {
            edge = g.addEdge("v" + Integer.toString(i), "special");
            g.setEdgeWeight(edge, 1.0);
        }
        ;
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        //  System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));

    }
}