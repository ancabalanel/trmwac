/**
 * 
 */
package sim.scn.instr;

/**
 * @author Anca
 * 
 */
@SuppressWarnings("serial")
public class ModifyMessageInstruction extends Instruction {

	/** The type of message to be modified by the receiver of this instruction */
	String msgType; 
	float modProb;
	
	public ModifyMessageInstruction(int receiver, String msgType, float modProb) {
		super(receiver);
		this.msgType = msgType;
		this.modProb = modProb;
	}

	public String getMsgType() {
		return msgType;
	}
	
	public float getModProb(){
		return modProb;
	}

	@Override
	public String toString() {
		return "ModifyMessageInstruction [msgType=" + msgType + ", receiver="
				+ receiver.getLocalName() + "]";
	}
	
	
}
