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

    private final double approximationRate= 0.1;

    private final Comparator<Double> comparator = new ToleranceDoubleComparator(0.0000001);


    @Before
    public void init() {
        g = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

   /* @Test
    public void getMaximumFlow() {
    }

    @Test
    public void gargAndKoenemann() {
    }
*/


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
        assertTrue(comparator.compare(Math.abs(100.0-flow),approximationRate)<=0);
    }
}