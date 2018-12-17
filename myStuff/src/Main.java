import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jgrapht.io.ImportException;

public class Main {

	public static final File OPTIMATOR_2_GRAPH_FILE = new File("Graph", "Optimator2Graph_klein");
	public static final File OPTIMATOR_3_GRAPH_FILE = new File("Graph", "Optimator3Graph_klein");
	public static final File OPTIMATOR_2_SOLUTION_FILE = new File("Solution", "Optimator2Solution_klein.xlsx");

	public static void main(String[] args) throws ImportException, InvalidFormatException, IOException {
		Optimator2Graph optimator2Graph = Optimator2Graph.importiereGraph(OPTIMATOR_2_GRAPH_FILE);
		System.out.println(optimator2Graph.size());
		Optimator3Graph optimator3Graph = Optimator3Graph.importiereGraph(OPTIMATOR_3_GRAPH_FILE);
		System.out.println(optimator3Graph.size());
		Optimator2Solution optimator2Solution = Optimator2Solution.importiereLoesung(OPTIMATOR_2_SOLUTION_FILE,
				optimator2Graph);
		System.out.println(optimator2Solution.size());
	}

}
