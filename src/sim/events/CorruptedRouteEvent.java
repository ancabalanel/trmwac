/**
 * 
 */
package sim.events;

import java.util.List;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class CorruptedRouteEvent extends Event {
	
	List<Integer> route; 
	int frameSender;
	
	public CorruptedRouteEvent(int source, List<Integer> route, int sender) {
		super(source);
		this.route = route;
		this.frameSender = sender;
	}

	public List<Integer> getRoute() {
		return route;
	}

	public int getFrameSender() {
		return frameSender;
	}
}
