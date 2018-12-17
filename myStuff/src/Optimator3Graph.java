import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.io.ImportException;

/**
 * Repräsentiert einen Graph für Optimator II mit einer einzelnen Laufbahn.
 * <p>
 * Knoten für eingehende Laufbahnen haben Indizes, die kleiner als die
 * Regenerations-Knoten sind. Knoten für ausgehende Laufbahnen haben Indizes,
 * die größer als die Schülerschwund-Knoten sind. Falls mit dem Laufbahnwechsel
 * gleichzeitig ein Statuswechsel vollzogen wird, findet dieser im Graph der
 * abgebenden Laufbahn statt.
 * <p>
 * 07.03.2018
 * 
 * @author Martin
 * @version 0.49
 *
 */
public class Optimator3Graph extends TreeMap<Integer, Optimator3SubGraph> {

	private static final long serialVersionUID = 1L;

	/**
	 * Importiert den Graph.
	 * <p>
	 * Aus der Datei [filename]_Gruppen.txt werden die im Graph aktiven Gruppen
	 * ausgelesen. Jeder Subgraph wird aus der Datei [filename]_[index].xml
	 * ausgelesen.
	 * <p>
	 * 23.03.2018
	 * 
	 * @param directory
	 *        Ordner, aus dem der Graph importiert wird
	 * @param optimatorGraph
	 *        leerer Graph, in den importiert wird
	 * @throws ImportException
	 *         Fehler beim Import
	 */
	public static Optimator3Graph importiereGraph(File directory) throws ImportException {
		Optimator3Graph optimatorGraph = new Optimator3Graph();
		// Importiere aktive Gruppen im Graph
		Set<Integer> indizes = new LinkedHashSet<>();
		try (Scanner scanner = new Scanner(new File(directory, "Graph_Gruppen.txt"))) {
			scanner.useDelimiter(", |\\[|\\]");
			while (scanner.hasNextInt()) {
				indizes.add(scanner.nextInt());
			}
			// Importiere Graph
			for (int index : indizes) {
				optimatorGraph.put(index,
						Optimator3SubGraph.importiereGraph(new File(directory, "Graph_" + index + ".xml")));
			}
		} catch (FileNotFoundException e) {
			throw new ImportException(e);
		}
		return optimatorGraph;
	}

	/**
	 * Initialisiert einen Graph mit gegebener Laufbahn und Laufbahngruppe.
	 * <p>
	 * 07.03.2018
	 * 
	 * @param laufbahngruppe
	 *        Laufbahngruppe des Graphen
	 * @param laufbahn
	 *        Laufbahn des Graphen
	 */
	public Optimator3Graph() {
		super();
	}

	/**
	 * Stellt fest, ob Graphen übereinstimmen.
	 * <p>
	 * Gibt <code>true</code> zurück, der andere Graph ein
	 * <code>Optimator2SingleGraph</code> ist, Laufbahngruppe und Laufbahn identisch
	 * sind und die Subgraphen übereinstimmen.
	 * <p>
	 * 27.03.2018
	 * 
	 * @param other
	 *        zu vergleichendes Objekt
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof Optimator2Graph) {
			Optimator2Graph that = (Optimator2Graph) other;
			return super.equals(that);
		}
		return false;
	}
}
