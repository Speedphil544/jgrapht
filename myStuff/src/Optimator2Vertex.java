import java.io.Serializable;
import java.util.List;

/**
 * Beschreibt einen Knoten in einem Graph.
 * <p>
 * 15.03.2018
 * 
 * @author Martin
 * @version 0.22
 *
 */
public class Optimator2Vertex implements Serializable {

	private static final long serialVersionUID = 1L;

	private int dienstgrad;

	private int zeitscheibe;

	private int status;

	/**
	 * Initialisiert Knoten für <code>Optimator2SingleGraph</code>.
	 * <p>
	 * 15.03.2018
	 * 
	 * @param dienstgrad
	 *        Dienstgrad des Knotens
	 * @param zeitscheibe
	 *        Zeitscheibe des Knotens
	 * @param status
	 *        Status des Knotens
	 */
	public Optimator2Vertex(int dienstgrad, int zeitscheibe, int status) {
		this.dienstgrad = dienstgrad;
		this.zeitscheibe = zeitscheibe;
		this.status = status;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof Optimator2Vertex) {
			Optimator2Vertex that = (Optimator2Vertex) other;
			return this.dienstgrad == that.dienstgrad && this.zeitscheibe == that.zeitscheibe
					&& this.status == that.status;
		}
		return false;
	}

	public int getDienstgrad() {
		return dienstgrad;
	}

	public int getStatus() {
		return status;
	}

	public int getZeitscheibe() {
		return zeitscheibe;
	}

	@Override
	public int hashCode() {
		int hashCode = status;
		hashCode = 37 * hashCode + zeitscheibe;
		hashCode = 37 * hashCode + dienstgrad;
		return hashCode;
	}

	public List<Integer> toList() {
		return List.of(dienstgrad, zeitscheibe, status);
	}

	@Override
	public String toString() {
		return String.valueOf(dienstgrad) + "|" + String.valueOf(zeitscheibe) + "|" + String.valueOf(status);
	}

}
