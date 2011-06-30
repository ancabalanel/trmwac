/**
 * 
 */
package sim.events;

import java.io.Serializable;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class Event implements Serializable {

	public int source;
	
	public Event(int source){
		this.source = source;
	}

	public int getSource() {
		return source;
	}
	
	
}
