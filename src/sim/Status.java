package sim;

import mwac.msgs.Message;
import sim.eval.Parameters;

/**
 * The <code>Status</code> class maintains the status of a
 * <code>Sensor</code> agent, with respect to its behaviour (malicious or
 * trustworthy). It also includes information about whether the agent uses
 * authorization of messages and a trust model.
 * 
 * @author Anca
 * 
 */
public class Status {
	Sensor agent;
	
	boolean authorization;
	
	boolean useTrust;
	boolean promiscuousMode;

	boolean fabricator;
	
	boolean modifier;
	String modMsgType;
	float modProb;

	public boolean nofwd;
	public float dropProb;

	public Status(Sensor agent) {
		this.agent = agent;
		
		promiscuousMode = false;
		authorization = false;
		useTrust = false;

		fabricator = false;
		modifier = false;
		modProb = 0.0f;
		
		nofwd = false;
		dropProb = 0.0f;
	}

	/**
	 * Decides whether to drop a message or not.
	 * 
	 * @return <code>true</code> if the message should be dropped,
	 *         <code>false</code> otherwise
	 */
	public boolean drop() {
		if(nofwd)
			return Parameters.random.nextFloat() < dropProb;
		else
			return false;
	}

	/**
	 * Returns whether to modify a message or not.
	 * 
	 * @param msg
	 *            the message about which it must be decided whether to
	 *            modify or not
	 * @return <code>true</code> if the message must be modified,
	 *         <code>false</code> otherwise
	 */
	public boolean mustModify(Message msg){
		if(modifier)
			if(msg.getClass().getName().equals(modMsgType))
				return Parameters.random.nextFloat() < modProb;
			else
				return false;
		else
			return false;
	}

	/**
	 *  
	 * @return <code>true</code> if the agent with this status is malicious,
	 *         <code>false</code> if it is not malicious.
	 */
	public boolean isMalicious() {
		return fabricator || modifier || nofwd;
	}

	public Sensor getAgent() {
		return agent;
	}

	public void setAgent(Sensor agent) {
		this.agent = agent;
	}

	public boolean isAuthorization() {
		return authorization;
	}

	public void setAuthorization(boolean authorization) {
		this.authorization = authorization;
	}

	public boolean isUseTrust() {
		return useTrust;
	}

	public void setUseTrust(boolean useTrust) {
		this.useTrust = useTrust;
	}

	public boolean isPromiscuousMode() {
		return promiscuousMode;
	}

	public void setPromiscuousMode(boolean promiscuousMode) {
		this.promiscuousMode = promiscuousMode;
	}

	public boolean isFabricator() {
		return fabricator;
	}

	public void setFabricator(boolean fabricator) {
		this.fabricator = fabricator;
	}

	public boolean isModifier() {
		return modifier;
	}

	public void setModifier(boolean modifier) {
		this.modifier = modifier;
	}

	public String getModMsgType() {
		return modMsgType;
	}

	public void setModMsgType(String modMsgType) {
		this.modMsgType = modMsgType;
	}

	public float getModProb() {
		return modProb;
	}

	public void setModProb(float modProb) {
		this.modProb = modProb;
	}

	public boolean isNofwd() {
		return nofwd;
	}

	public void setNofwd(boolean nofwd) {
		this.nofwd = nofwd;
	}

	public float getDropProb() {
		return dropProb;
	}

	public void setDropProb(float dropProb) {
		this.dropProb = dropProb;
	}
	
	
}