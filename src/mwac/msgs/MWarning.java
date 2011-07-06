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

	int possiblyMalicious;

	public MWarning(int source, int destination, int possiblyMalicious) {
		super(source, destination);
		this.possiblyMalicious = possiblyMalicious;
	}

	public int getPossiblyMalicious() {
		return possiblyMalicious;
	}

	public void setPossiblyMalicious(int possiblyMalicious) {
		this.possiblyMalicious = possiblyMalicious;
	}

	@Override
	public String toString() {
		return "MWarning [possiblyMalicious=" + possiblyMalicious + ", source="
				+ source + ", destination=" + destination + "]";
	}
}
