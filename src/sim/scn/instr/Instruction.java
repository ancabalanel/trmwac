/**
 * 
 */
package sim.scn.instr;

import jade.core.AID;

import java.io.Serializable;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class Instruction implements Serializable{
	AID receiver;
	
	public Instruction(int receiver){
		this.receiver = new AID(receiver + "", AID.ISLOCALNAME);
	}

	public AID getReceiver() {
		return receiver;
	}

	public void setReceiver(AID receiver) {
		this.receiver = receiver;
	}
	
	
}
