import java.io.File;
import java.util.Map;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.GraphMLImporter;
import org.jgrapht.io.ImportException;
import org.jgrapht.io.VertexProvider;

/**
 * Klasse für den Subgraph in allen Graph-Klassen.
 * <p>
 * 27.03.2018
 * 
 * @author Martin
 * @version 0.17
 * 
 */
public class Optimator2SubGraph extends DefaultDirectedGraph<Optimator2Vertex, DefaultEdge> {

	private static final long serialVersionUID = 1L;

	/**
	 * Importiert einen SubGraph.
	 * <p>
	 * 24.04.2018
	 * 
	 * @param file
	 *        Datei des SubGraph
	 * @param graphClass
	 *        Klasse des enthaltenden <code>OptimatorGraph</code>
	 * @param knotenLaufbahnwechsel
	 *        Knoten für Laufbahnwechsel, darf <code>null</code> sein
	 * @return <code>SubGraph</code> aus der Datei
	 * @throws ImportException
	 *         Fehler beim Importieren
	 */
	public static Optimator2SubGraph importiereGraph(File file) throws ImportException {
		VertexProvider<Optimator2Vertex> vertexProvider = new VertexProvider<>() {
			@Override
			public Optimator2Vertex buildVertex(String id, Map<String, Attribute> attributes) {
				int dienstgrad = Integer.valueOf(attributes.get("dienstgrad").getValue());
				int zeitscheibe = Integer.valueOf(attributes.get("zeitscheibe").getValue());
				int status = Integer.valueOf(attributes.get("status").getValue());
				return new Optimator2Vertex(dienstgrad, zeitscheibe, status);
			}
		};
		EdgeProvider<Optimator2Vertex, DefaultEdge> edgeProvider = (from, to, label, attributes) -> new DefaultEdge();
		GraphMLImporter<Optimator2Vertex, DefaultEdge> importer = new GraphMLImporter<>(vertexProvider, edgeProvider);
		Optimator2SubGraph subGraph = new Optimator2SubGraph();
		importer.importGraph(subGraph, file);
		return subGraph;
	}

	public Optimator2SubGraph() {
		super(DefaultEdge.class);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof Optimator2SubGraph) {
			Optimator2SubGraph that = (Optimator2SubGraph) other;
			if (this.vertexSet().equals(that.vertexSet()) && this.edgeSet().size() == that.edgeSet().size()) {
				for (DefaultEdge edge : this.edgeSet()) {
					if (that.getEdge(this.getEdgeSource(edge), this.getEdgeTarget(edge)) == null) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
}
