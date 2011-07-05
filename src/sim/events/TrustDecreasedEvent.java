/**
 * 
 */
package sim.events;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class TrustDecreasedEvent extends Event {

	int watchedNode;
	
	public TrustDecreasedEvent(int source, int watched) {
		super(source);
		this.watchedNode = watched;
	}

	public int getWatchedNode() {
		return watchedNode;
	}

}
