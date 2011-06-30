/**
 * 
 */
package sim.scn.instr;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class NoForwardInstruction extends Instruction {

	/** Probability to drop messages */
	float dropProbability;
	
	public NoForwardInstruction(int receiver, float drop) {
		super(receiver);
		this.dropProbability = drop;
	}

	public float getDropProbability() {
		return dropProbability;
	}

	@Override
	public String toString() {
		return "NoForwardInstruction [dropProbability=" + dropProbability
				+ ", receiver=" + receiver.getLocalName() + "]";
	}
	
	
}
