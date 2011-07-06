/**
 * 
 */
package sim.events;

import mwac.NeighbourTrust;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class TrustDecreasedEvent extends Event {

	NeighbourTrust nbTrust; 
	
	public TrustDecreasedEvent(int source, NeighbourTrust nbTrust) {
		super(source);
		this.nbTrust = nbTrust;
	}

	public NeighbourTrust getNbTrust() {
		return nbTrust;
	}
}
