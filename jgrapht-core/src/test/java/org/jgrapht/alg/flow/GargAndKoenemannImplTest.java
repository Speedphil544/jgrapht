package org.jgrapht.alg.flow;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.alg.interfaces.MaximumMultiCommodityFlowAlgorithm;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;
import

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GargAndKoenemannImplTest extends MaximumFlowAlgorithmTest {


    private DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g;

    private MaximumMultiCommodityFlowAlgorithm<String, DefaultWeightedEdge> gargAndKoenemann;

    private DefaultWeightedEdge edge;

    private final String v1 = "v1";

    private final String v2 = "v2";

    private final String v3 = "v3";

    @Override
    MaximumMultiCommodityFlowAlgorithm<Integer, DefaultWeightedEdge> createSolver(
            Graph<Integer, DefaultWeightedEdge> network)
    {
        return new GargAndKoenemannMMCFImp<>(network);
    }

    @Before
    public void init()
    {
        g = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    @Test
    public void simpleTest1()
    {
        g.addVertex(v1);
        g.addVertex(v2);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 100.0);

        List sources = new LinkedList();
        sources.add(v1);
        List sinks = new LinkedList();
        sinks.add(v1);
        double accuracy =0.1;

        gargAndKoenemann =  new GargAndKoenemannMMCFImp<>(g);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks,accuracy);
        assertEquals(100.0, flow, 0);
    }

    @Test
    public void simpleTest2()
    {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 100.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 50.0);
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.getMaximumFlowValue(v1, v3);
        assertEquals(50.0, flow, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionTest1()
    {
        g.addVertex(v1);
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.getMaximumFlowValue(v1, v1);
        System.out.println(flow);
    }

    @Test
    public void disconnectedTest()
    {
        g.addVertex(v1);
        g.addVertex(v2);
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.getMaximumFlowValue(v1, v2);
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
        dinic = new DinicMFImpl<>(g);
        double flow = dinic.getMaximumFlowValue(v1, v2);
        assertEquals(2.0, flow, 0);
    }
}

}
