/**
 * 
 */
package sim.events;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class ReceivedFrameEvent extends Event {
	int volume;
	
	public ReceivedFrameEvent(int source, int volume) {
		super(source);
		this.volume = volume;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}
	

}
