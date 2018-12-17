import java.io.Serializable;

/**
 * Beschreibt einen Knoten in einem Graph.
 * <p>
 * 15.03.2018
 * 
 * @author Martin
 * @version 0.22
 *
 */
public class Optimator3Vertex implements Serializable {

	private static final long serialVersionUID = 1L;

	private int dienstgrad;

	private int zeitscheibe;

	private int status;

	private int netzwerk;

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
	public Optimator3Vertex(int dienstgrad, int zeitscheibe, int status, int netzwerk) {
		this.dienstgrad = dienstgrad;
		this.zeitscheibe = zeitscheibe;
		this.status = status;
		this.netzwerk = netzwerk;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof Optimator3Vertex) {
			Optimator3Vertex that = (Optimator3Vertex) other;
			return this.dienstgrad == that.dienstgrad && this.zeitscheibe == that.zeitscheibe
					&& this.status == that.status && this.netzwerk == that.netzwerk;
		}
		return false;
	}

	public int getDienstgrad() {
		return dienstgrad;
	}

	public int getNetzwerk() {
		return netzwerk;
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
		hashCode = 37 * hashCode + netzwerk;
		return hashCode;
	}

	@Override
	public String toString() {
		return dienstgrad + "|" + zeitscheibe + "|" + status + "|" + netzwerk;
	}

}
