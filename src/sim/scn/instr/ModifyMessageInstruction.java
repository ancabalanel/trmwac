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
	
	public ModifyMessageInstruction(int receiver, String msgType) {
		super(receiver);
		this.msgType = msgType;
	}

	public String getMsgType() {
		return msgType;
	}

	@Override
	public String toString() {
		return "ModifyMessageInstruction [msgType=" + msgType + ", receiver="
				+ receiver.getLocalName() + "]";
	}
	
	
}
