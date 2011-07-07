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
	
	boolean usingTrust;
	boolean listeningToRRequests;
	
	boolean fabricating;

	boolean modifying;

	String modMsgType;
	
	float modProb;
	public boolean nofwd;
	public float dropProb;

	public Status(Sensor agent) {
		this.agent = agent;
		
		authorization = false;
		usingTrust = false;
		listeningToRRequests = false;

		fabricating = false;
		modifying = false;
		modProb = 0.0f;
		
		nofwd = false;
		dropProb = 0.0f;
	}

	public boolean drop() {
		if(nofwd)
			return Parameters.random.nextFloat() < dropProb;
		else
			return false;
	}

	public Sensor getAgent() {
		return agent;
	}

	public float getDropProb() {
		return dropProb;
	}

	public String getModMsgType() {
		return modMsgType;
	}

	public float getModProb() {
		return modProb;
	}

	public boolean isAuthorization() {
		return authorization;
	}

	public boolean isFabricating() {
		return fabricating;
	}

	public boolean isListeningToRRequests() {
		return listeningToRRequests;
	}

	/**
	 *  
	 * @return <code>true</code> if the agent with this status is malicious,
	 *         <code>false</code> if it is not malicious.
	 */
	public boolean isMalicious() {
		return fabricating || modifying || nofwd;
	}

	public boolean isModifying() {
		return modifying;
	}

	public boolean isNofwd() {
		return nofwd;
	}

	public boolean isUsingTrust() {
		return usingTrust;
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
		if(modifying)
			if(msg.getClass().getName().equals(modMsgType))
				return Parameters.random.nextFloat() < modProb;
			else
				return false;
		else
			return false;
	}

	public void setAgent(Sensor agent) {
		this.agent = agent;
	}

	public void setAuthorization(boolean authorization) {
		this.authorization = authorization;
	}

	public void setDropProb(float dropProb) {
		this.dropProb = dropProb;
	}

	public void setFabricating(boolean fabr) {
		this.fabricating = fabr;
	}

	public void setListeningToRRequests(boolean listeningToRRequests) {
		this.listeningToRRequests = listeningToRRequests;
	}

	public void setModifying(boolean mod) {
		this.modifying = mod;
	}

	public void setModMsgType(String modMsgType) {
		this.modMsgType = modMsgType;
	}

	public void setModProb(float modProb) {
		this.modProb = modProb;
	}

	public void setNofwd(boolean nofwd) {
		this.nofwd = nofwd;
	}

	public void setUsingTrust(boolean useTrust) {
		this.usingTrust = useTrust;
	}
	
	
}