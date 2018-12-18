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
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GargAndKoenemannMMCFImpTest {


    private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g;

    private MaximumMultiCommodityFlowAlgorithm<String, DefaultWeightedEdge> gargAndKoenemann;

    private DefaultWeightedEdge edge;

    private final String v1 = "v1";

    private final String v2 = "v2";

    private final String v3 = "v3";

    private final double approximationRate = 0.01;

    private double epsilon = 1e-20;

    private final Comparator<Double> comparator = new ToleranceDoubleComparator(epsilon);

    private double expectedFlow;


    @Before
    public void init() {
        g = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    @Test
    public void simpleTest1() {
        g.addVertex(v1);
        g.addVertex(v2);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 100.0);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v2);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        assertTrue(comparator.compare(Math.abs(100.0 - flow), approximationRate * 100) <= 0);
    }


    @Test
    public void simpleTest2() {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 100.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 50.0);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v3);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        assertTrue(comparator.compare(Math.abs(50 - flow), approximationRate * 50) <= 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void exceptionTest1() {
        g.addVertex(v1);
        List<String> sources = new LinkedList();
        sources.add(v1);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sources, approximationRate);
        System.out.println(flow);
    }

    @Test
    // todo: change something in gargAndKoenemann so that we return zeroflow when there are no cennections between sources and sinks
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
    public void simpleTest3() {
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
        String v11= "v11";
        String v12= "v12";
        String v13= "v13";
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
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);

        System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));
        //assertTrue(comparator.compare(Math.abs(2 - flow), approximationRate * 2) <= 0);
    }

    @Test
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
        String v11= "v11";
        String v12= "v12";
        String v13= "v13";
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
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g,epsilon);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        Map flowmap = gargAndKoenemann.getFlowMap();


       //System.out.println(flowmap+" " +flow);
        assertTrue(comparator.compare(Math.abs(3.5 - flow), approximationRate * 3.5) <= 0);
    }







}