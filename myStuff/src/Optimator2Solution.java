
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jgrapht.graph.DefaultEdge;

/**
 * Lösung eines Problems mit Optimator II auf allen Laufbahnen mit ganzzahligen
 * Lösungswerten.
 * <p>
 * Die Klasse bildet sowohl Lösungen ab, die mithilfe eines
 * <code>Optimator2MultipleGraph</code> berechnet werden, als auch Lösungen, die
 * aus <code>Optimator2SingleSolutionIntegral</code> zusammengesetzt werden.
 * <p>
 * 14.05.2018
 * 
 * @author Martin
 * @version 0.19
 *
 */
public class Optimator2Solution extends HashMap<Optimator2SubGraph, Map<DefaultEdge, Double>> {

	private static final long serialVersionUID = 1L;

	public static Optimator2Solution importiereLoesung(File file, Optimator2Graph optimatorGraph)
			throws InvalidFormatException, IOException {
		Optimator2Solution solution = new Optimator2Solution(optimatorGraph);
		solution.importiereLoesungAbstract(file);
		return solution;
	}

	Optimator2Graph optimatorGraph;

	private Optimator2Solution(Optimator2Graph optimatorGraph) {
		this.optimatorGraph = optimatorGraph;
	}

	public Optimator2Solution(Optimator2Graph optimatorGraph,
			Map<Optimator2SubGraph, Map<DefaultEdge, Double>> yWerte) {
		this.optimatorGraph = optimatorGraph;
	}

	public Optimator2Graph getOptimatorGraph() {
		return optimatorGraph;
	}

	void importiereLoesungAbstract(File file) throws IOException, InvalidFormatException {
		OPCPackage pkg;
		XSSFWorkbook wb;

		pkg = OPCPackage.open(file);
		wb = new XSSFWorkbook(pkg);

		Map<List<Integer>, Double> excelWerte = new LinkedHashMap<>();
		Sheet sheet = wb.getSheet("y");
		int anzahlSpalten = sheet.getRow(0).getLastCellNum();
		for (Row row : sheet) {
			if (row.getRowNum() == 0) {
				continue;
			}
			List<Integer> key = new ArrayList<>();
			double value = 0;
			for (Cell cell : row) {
				key.add((int) cell.getNumericCellValue());
				value = cell.getNumericCellValue();
			}
			// remove last value from key (which is saved in value, as a double as intended)
			key.remove(anzahlSpalten - 1);
			excelWerte.put(key, value);
		}
		for (Entry<Integer, Optimator2SubGraph> entry : optimatorGraph.entrySet()) {
			put(entry.getValue(), new LinkedHashMap<>());
			for (DefaultEdge edge : entry.getValue().edgeSet()) {
				Optimator2Vertex source = entry.getValue().getEdgeSource(edge);
				Optimator2Vertex target = entry.getValue().getEdgeTarget(edge);
				List<Integer> key = Stream.of(source.toList(), target.toList(), List.of(entry.getKey()))
						.flatMap(List::stream).collect(Collectors.toList());
				if (excelWerte.containsKey(key)) {
					get(entry.getValue()).put(edge, excelWerte.get(key));
				} else {
					get(entry.getValue()).put(edge, 0.0);
				}
			}
		}
	}

}
