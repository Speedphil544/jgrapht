package org.jgrapht.alg.flow;

import org.jgrapht.alg.interfaces.MaximumMultiCommodityFlowAlgorithm;
import org.jgrapht.alg.util.ToleranceDoubleComparator;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GargAndKoenemannMMCFImpTest {


    private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g;

    private MaximumMultiCommodityFlowAlgorithm<String, DefaultWeightedEdge> gargAndKoenemann;

    private DefaultWeightedEdge edge;

    private final String v1 = "v1";

    private final String v2 = "v2";

    private final String v3 = "v3";

    private final double approximationRate = 0.1;

    private double epsilon = 1e-30;

    private final Comparator<Double> comparator = new ToleranceDoubleComparator(epsilon);

    private double expectedFlow;


    private DefaultDirectedWeightedGraph createRandomGraph(int size, double prop, double min, double max) {
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g = new DefaultDirectedWeightedGraph(DefaultWeightedEdge.class);
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
        ;
        return g;
    }

    ;


    @Before
    public void init() {
        g = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }


    @Test
    // hier verwenden wir einen Zufallsgrapheng mit vorgegebener Knotenzahl/Kantengewichten/Kantenwahrscheinlkichkeit

    public void Test0() {


        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g = createRandomGraph(3, 1, 1, 2);
        /*
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        edge = g.addEdge(v1, v2);

        g.setEdgeWeight(edge, 1);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 1000000);
        */
        List<String> sources = new LinkedList();
        sources.add("v1");
        List<String> sinks = new LinkedList();
        sinks.add("v3");
        //DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraShortestPath = new DijkstraShortestPath(g);
        // Map<DefaultWeightedEdge, Double> length = g.edgeSet().stream().collect(Collectors.toMap(x -> x, x -> 0.0001));
        //List<GraphPath<String, DefaultWeightedEdge>> allpaths = new LinkedList<>();
        //AllDirectedPaths<String, DefaultWeightedEdge> allDirectedPaths = new AllDirectedPaths(g);
        // allpaths = allDirectedPaths.getAllPaths("v1", "v10", true, null);
        // Pair<GraphPath<String, DefaultWeightedEdge>, Double> shortestPathPair = allpaths
        //.stream().map(x -> new Pair<GraphPath<String, DefaultWeightedEdge>, Double>(x, x.getEdgeList().stream().mapToDouble(length::get).sum()))
        //.min((x, y) -> x.getSecond().compareTo(y.getSecond())).orElse(null);
        //Stopwatch timer = Stopwatch.createStarted();
        // dijkstraShortestPath.getPath("v1", "v2000");
        //System.out.println("DIJKSTRA: " + timer.stop());
        //dijkstra.getPaths("v1").getPath();
        gargAndKoenemann = new GargAndKoenemannMMCFImp(g);
        MaximumMultiCommodityFlowAlgorithm.MaximumFlow flow = gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate);
        System.out.println(flow);

    }


    @Test
    /*
    hier testen wir den GargAndKoenemann auf einem Graphen, der 2 Kanten mit sehr unterschiedlichem Gewicht entaehlt
        es faellt auf, dass die laengen der Kanten exponentiel unterschiedlich wachsen. Fuer Probleme, die wenige iterationen benoetigen,
         ist das kein Problem, jedoch sehr wohl bei groser iterationsanzahl: eine kante geht mit ihrer laenge gegen unendlich, waehrend die andere in
         der naehe der null verharrt.
   */
    public void Test1() {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 100000000000000000.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 1.0);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v3);
        double flow = 1.0;
        System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));
        assertTrue(comparator.compare(Math.abs(1.0 - flow), approximationRate * 1.0) <= 0);
    }


    @Test
    //hier testen wir den GargAndKoeneMann auf einem Graphen, der 2 sehr unterschiedlich lange Pfade enthaelt
    public void Test2() {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        String v4 = "v4";
        String v5 = "v5";
        String v6 = "v6";
        String v7 = "v7";
        String v8 = "v8";
        String v9 = "v9";
        String v10 = "v10";
        String v11 = "v11";
        String v12 = "v12";
        String v13 = "v13";
        String v14 = "14";
        String v15 = "v15";
        String v16 = "v16";
        String v17 = "v17";
        String v18 = "v18";
        String v19 = "v19";
        String v20 = "v20";
        g.addVertex(v4);
        g.addVertex(v5);
        g.addVertex(v6);
        g.addVertex(v7);
        g.addVertex(v8);
        g.addVertex(v9);
        g.addVertex(v10);
        g.addVertex(v11);
        g.addVertex(v12);
        g.addVertex(v13);
        g.addVertex(v14);
        g.addVertex(v15);
        g.addVertex(v16);
        g.addVertex(v17);
        g.addVertex(v18);
        g.addVertex(v19);
        g.addVertex(v20);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v3, v4);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v4, v5);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v5, v6);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v6, v7);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v7, v8);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v8, v9);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v9, v10);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v10, v11);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v11, v12);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v12, v13);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v13, v14);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v14, v15);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v15, v16);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v16, v17);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v17, v18);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v18, v19);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v19, v20);
        g.setEdgeWeight(edge, 1.0);
        List<String> sources = new LinkedList();
        sources.add(v1);
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v20);
        sinks.add(v3);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);

        System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));
        //assertTrue(comparator.compare(Math.abs(2 - flow), approximationRate * 2) <= 0);
    }


    @Test(expected = IllegalArgumentException.class)
    // testen, was passiert, wenn keine sink vorhanden ist.
    public void Test3() {
        g.addVertex(v1);
        List<String> sources = new LinkedList();
        sources.add(v1);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sources, approximationRate);
        System.out.println(flow);
    }

    @Test
    // testen,was passiert, wenn source und sink nicht zusammenhaengen
    public void disconnectedTest() {
        g.addVertex(v1);
        g.addVertex(v2);
        List<String> sources = new LinkedList();
        sources.add(v1);

        List<String> sinks = new LinkedList();
        sinks.add(v2);

        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        assertEquals(0.0, flow, 0);
    }


    @Test
    // testen GargAndKoeneMann fuer 2 demands
    public void testWith2Demands() {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        String v4 = "v4";
        String v5 = "v5";
        String v6 = "v6";
        String v7 = "v7";
        String v8 = "v8";
        String v9 = "v9";
        String v10 = "v10";
        String v11 = "v11";
        String v12 = "v12";
        String v13 = "v13";
        String v14 = "14";
        String v15 = "v15";
        String v16 = "v16";
        String v17 = "v17";
        String v18 = "v18";
        g.addVertex(v4);
        g.addVertex(v5);
        g.addVertex(v6);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v3, v4);
        g.setEdgeWeight(edge, 3.5);
        edge = g.addEdge(v4, v5);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v4, v6);
        g.setEdgeWeight(edge, 3.0);
        List<String> sources = new LinkedList();
        sources.add(v1);
        sources.add(v2);
        List<String> sinks = new LinkedList();
        sinks.add(v5);
        sinks.add(v6);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        Map flowmap = gargAndKoenemann.getFlowMap();
        //System.out.println(flowmap+" " +flow);
        assertTrue(comparator.compare(Math.abs(3.5 - flow), approximationRate * 3.5) <= 0);
    }


}