/**
 * 
 */
package sim.events;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class ReceivedMeasureEvent extends Event {

	String measure;
	
	public ReceivedMeasureEvent(int source, String measure) {
		super(source);
		this.measure = measure;
	}

	public String getMeasure() {
		return measure;
	}
	
	
}
