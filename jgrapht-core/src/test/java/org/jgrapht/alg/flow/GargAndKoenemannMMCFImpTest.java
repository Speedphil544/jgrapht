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

import static org.junit.Assert.assertTrue;

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
        return g;
    }


    @Before
    public void init() {
        g = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }


    @Test
    /*
    hier verwenden wir einen Zufallsgraphen mit vorgegebener Knotenzahl, Kantenwahrscheinlkichkeit,min/maxlkapazitaeten
    */
    public void ZufallsGraphTest() {
        g = createRandomGraph(3, 1, 1, 2);
        List<String> sources = new LinkedList();
        sources.add("v1");
        List<String> sinks = new LinkedList();
        sinks.add("v3");
        gargAndKoenemann = new GargAndKoenemannMMCFImp(g);
        MaximumMultiCommodityFlowAlgorithm.MaximumFlow flow = gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate);
        System.out.println(flow);

    }



    @Test
    /*
    Hier testen wir den GargAndKoenemann auf einem Graphen, der 3 Knoten (1,2,3)
    und 2 Kanten([1,2],[2,3]) mit sehr unterschiedlichem Gewicht entaehlt.
    Dazu den folgenden demand: (1,2)
    Es faellt auf, dass die Laengen der Kanten exponentiel unterschiedlich wachsen. Wenn das Gewicht sehr
    unterschiedlich ausgepraegt ist und wir eine hohe Anzahl an Iterationen haben, sind die Laengen der Kanten sehr
    unterschiedlich...
    */
    public void unterschiedlicheKapazitaetTest() {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 1.0);
        gargAndKoenemann = new GargAndKoenemannAdvancedMMCFlowImp<>(g, epsilon);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v3);
        double flow = 1.0;
        System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));
        assertTrue(comparator.compare(Math.abs(1.0 - flow), approximationRate * 1.0) <= 0);
    }

    /*
   @Test
   Wie im vorigen Test hat der Graph 3 Knoten(1,2,3) und 2 Kanten([1,2],[2,3]).
   Aber die Kapazitaeten sind diesmal gleich mit 1.0, und wir haben zwei demands: (1,2), (1,3).
   Da der pfad fuer den zweiten demand immer laenger ist als der pfad fuer den ersten demand, wird zuerst
   die laenge der kante [1,2] auf einen wert groeser als  1.0 gebracht, bevor die kante [2,3]
   ueberhaupt angeschaut wird...
   Zu diesem zeitpunkt hat folglich [1,2] eine laenge groesser 1.0 und [2,3] die laenge delta.
    public void zweiDemandsAufEinemPfadTest() {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        edge = g.addEdge(v1, v2);
        g.setEdgeWeight(edge, 1.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 1.0);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        List<String> sources = new LinkedList();
        sources.add(v1);
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v2);
        sinks.add(v3);
        double flow = 1.0;
        System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));
        //assertTrue(comparator.compare(Math.abs(1.0 - flow), approximationRate * 1.0) <= 0);
    }
    */


    @Test
    /*hier testen wir den GargAndKoeneMann auf einem Graphen, mit folgenden kanten: ([1,2],[2,3],[3,4],...),
     Kapazitaet jeder kante ist 1.0 und wir haben 2 demands: (1,2), (2,n).
     Diese konstellation ist nicht weiter schlimm, da die kante aus dem pfad des ersten demands maximal
     n mal so lang ist wie die laengste aus dem zweiten demand.
     */
    public void unteschiedlichLangePfadeTest() {
        int numberOFEdges = 1000;
        for (int i = 0; i < numberOFEdges; i++) {
            g.addVertex("v" + Integer.toString(i));
        }
        for (int i = 0; i < numberOFEdges - 1; i++) {
            edge = g.addEdge("v" + Integer.toString(i), "v" + Integer.toString(i + 1));
            g.setEdgeWeight(edge, 1.0);
        }
        List<String> sources = new LinkedList();
        sources.add("v0");
        sources.add("v1");
        List<String> sinks = new LinkedList();
        sinks.add("v1");
        sinks.add("v" + Integer.toString(numberOFEdges - 1));
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g, epsilon);
        System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));
    }


    @Test(expected = IllegalArgumentException.class)
    /*
    testen, was passiert, wenn keine senke vorhanden ist.
    */
    public void Test3() {
        g.addVertex(v1);
        List<String> sources = new LinkedList();
        sources.add(v1);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sources, approximationRate);
    }

    @Test
    /*
    testen,was passiert, wenn quelle und senke nicht zusammenhaengen
    */
    public void disconnectedTest() {
        g.addVertex(v1);
        g.addVertex(v2);
        List<String> sources = new LinkedList();
        sources.add(v1);
        List<String> sinks = new LinkedList();
        sinks.add(v2);
        gargAndKoenemann = new GargAndKoenemannMMCFImp<>(g);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        assertTrue(comparator.compare(Math.abs(0.0 - flow), approximationRate * 1.0) <= 0);
    }


    @Test
    /*
    haben die knoten (1,2,3,4,5,6), die Kanten ([1,3],[2,3],[3,4],[4,5],[5,6]) und demands: (1,5) (2,6)
    wobei wir wieder sehr unterschiedliche kapazitaeten verteilen (bitte dem code entnehmen)
    es faellt auf, dass die laenge der kante [3,4] mit abstand am schnellsten waechst:
    [3,4] hat viel weniger kapazitaet als [1,3] und [4,5]. [1,3] waechst also, wie wir dank
    unterschiedlicheKapazitaetTest wissen, sehr viel schneller als [1,3] und [4,5].
    Aber [2,3] und [4,6] koennen nicht schneller wachsen als [1,3] und [4,5], da ja immer der kuerzeste pfad gewaehlt wird.
    Also waechst [3,4] sehr schneller als alle vier anderen kanten.
    */
    public void zweiDemandsMitUnterschiedichenKapazitaetenTest() {
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
        g.setEdgeWeight(edge, 1000000.0);
        edge = g.addEdge(v2, v3);
        g.setEdgeWeight(edge, 11.0);
        edge = g.addEdge(v3, v4);
        g.setEdgeWeight(edge, 50);
        edge = g.addEdge(v4, v5);
        g.setEdgeWeight(edge, 10);
        edge = g.addEdge(v4, v6);
        g.setEdgeWeight(edge, 11);
        List<String> sources = new LinkedList();
        sources.add(v1);
        sources.add(v2);
        List<String> sinks = new LinkedList();
        sinks.add(v5);
        sinks.add(v6);
        gargAndKoenemann = new GargAndKoenemannAdvancedMMCFlowImp<>(g, epsilon);
        double flow = gargAndKoenemann.getMaximumFlowValue(sources, sinks, approximationRate);
        System.out.println(flow);
    }


    @Test
    /* haben die Knoten (1,2,3,4,5...,special,end), die kanten ([1,special],[2,special],[3,special],..., [special,end]),
    sowie demands: (1,end),(2,end),(3,end),...
    kapazitaeten sind alle gleich 1.0
    Diese konstellation ist  nicht weiter schlimm, [special,end] waechst nicht schneller als die anderen kanten.
    */
    public void vieleDemandsTest() {
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
        System.out.println(gargAndKoenemann.getMaximumFlow(sources, sinks, approximationRate));

    }
}