package sim.scn.act;

import mwac.Role;

public class ModificationDescription extends AttackDescription{

	/** The type of message to be modified by the attackers */
	String msgType;
	
	public ModificationDescription(long issued, int numAttackers, Role role, String msgType) {
		super(issued, numAttackers, role);
		this.msgType = msgType;
	}

	public String getMsgType() {
		return msgType;
	}
}
