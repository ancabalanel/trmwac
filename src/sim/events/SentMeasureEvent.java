package sim.events;

@SuppressWarnings("serial")
public class SentMeasureEvent extends Event{

	String measure;
	
	public SentMeasureEvent(int source, String measure) {
		super(source);
		this.measure = measure;
	}

	public String getMeasure(){
		return measure;
	}
}
