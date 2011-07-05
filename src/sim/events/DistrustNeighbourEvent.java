/**
 * 
 */
package sim.events;

/**
 * @author Anca
 * 
 */
@SuppressWarnings("serial")
public class DistrustNeighbourEvent extends Event {
	int neighbour;

	public DistrustNeighbourEvent(int source, int neighbour) {
		super(source);
		this.neighbour = neighbour;
	}

	public int getNeighbour() {
		return neighbour;
	}
	
	

}
