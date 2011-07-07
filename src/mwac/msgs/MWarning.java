/**
 * 
 */
package mwac.msgs;

/**
 * @author Anca
 * 
 */
@SuppressWarnings("serial")
public class MWarning extends Message {

	int suspect;

	public MWarning(int source, int destination, int suspectId) {
		super(source, destination);
		this.suspect = suspectId;
	}

	public int getSuspect() {
		return suspect;
	}

	public void setSuspect(int suspect) {
		this.suspect = suspect;
	}

	@Override
	public String toString() {
		return "MWarning [suspect=" + suspect + ", source=" + source
				+ ", destination=" + destination + "]";
	}

}
