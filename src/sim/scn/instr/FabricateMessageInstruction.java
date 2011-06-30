/**
 * 
 */
package sim.scn.instr;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class FabricateMessageInstruction extends Instruction {

	/** The type of fabricated messages */
	String messageType;
	/** Interval at which the fabricated messages are sent */
	long interval;
	/** Total number of fabricated messages each attacker sends */
	int totalMsgs; 
	
	public FabricateMessageInstruction(int receiver, String mType, long interval, int totalMsgs) {
		super(receiver);
		this.messageType = mType;
		this.interval = interval;
		this.totalMsgs = totalMsgs;
	}

	public long getInterval() {
		return interval;
	}

	public int getTotalMsgs() {
		return totalMsgs;
	}

	public String getMessageType() {
		return messageType;
	}

	@Override
	public String toString() {
		return "FabricateMessageInstruction [messageType=" + messageType
				+ ", interval=" + interval + ", totalMsgs=" + totalMsgs
				+ ", receiver=" + receiver.getLocalName() + "]";
	}
	
	
}
