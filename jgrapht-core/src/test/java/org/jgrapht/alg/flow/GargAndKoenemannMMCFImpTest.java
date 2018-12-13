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

    private double epsilon = 1e-20;

    private final Comparator<Double> comparator = new ToleranceDoubleComparator(epsilon);


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

    @Test // todo: change something in gargAndKoenemann so that we return zeroflow when there are no cennections between sources and sinks
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
    public void simpleTest3()
    {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        String v4 = "v4";
        g.addVertex(v4);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v3, v4);
        g.setEdgeWeight(edge, 2.0);
        edge = g.addEdge(v2, v4);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v1, v3);
        g.setEdgeWeight(edge, 1.0);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v2);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        assertTrue(comparator.compare(Math.abs(2 - flow), approximationRate * 2) <= 0);
    }

}