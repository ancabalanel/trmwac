/**
 * 
 */
package sim.scn.act;

import mwac.Role;

/**
 * @author Anca
 *
 */
public class FabricationDescription extends AttackDescription {
	
	/** The type of the fabricated message */ 
	String messageType; 
	/** Interval at which the fabricated messages are sent */
	long interval;
	/** total number of fabricated messages each attacker sends */
	int totalMsgs; 
	
	public FabricationDescription(long issued, int numAttackers, Role role, String mType, long interval, int totalMsgs) {
		super(issued, numAttackers, role);
		this.messageType = mType;
		this.interval = interval;
		this.totalMsgs = totalMsgs;
	}

	public String getMessageType() {
		return messageType;
	}

	public long getInterval() {
		return interval;
	}

	public int getTotalMsgs() {
		return totalMsgs;
	}

	
	
}
