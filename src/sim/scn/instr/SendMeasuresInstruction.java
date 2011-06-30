/**
 * 
 */
package sim.scn.instr;


/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class SendMeasuresInstruction extends Instruction {

	/** Interval at which the measures are sent */
	long interval;
	/** Total number of measures which must be sent by the agent receiving this instruction */
	int numMeasures;
	/** The id of the agent receiving the instruction */
	int destination;
	
	public SendMeasuresInstruction(int receiver, long interval, int numMeasures, int destination) {
		super(receiver);
		this.interval = interval;
		this.numMeasures = numMeasures;
		this.destination = destination;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public int getNumMeasures() {
		return numMeasures;
	}

	public void setNumMeasures(int numMeasures) {
		this.numMeasures = numMeasures;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}
	
	
}
