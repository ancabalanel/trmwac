package sim.scn.act;

import mwac.Role;

public class ModificationDescription extends AttackDescription{

	/** The type of message to be modified by the attackers */
	String msgType;
	float probMod;
	
	public ModificationDescription(long issued, int numAttackers, Role role, String msgType, float probMod) {
		super(issued, numAttackers, role);
		this.msgType = msgType;
		this.probMod = probMod;
	}

	public String getMsgType() {
		return msgType;
	}
	
	public float getModProb(){
		return probMod;
	}
}
