package sim.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mwac.FrameHandler;
import mwac.msgs.Frame;
import sim.Sensor;
import sim.scn.instr.Instruction;
import sim.scn.instr.InstructionHandler;

/**
 * This is the main loop of the agent. When this behaviour is added to a
 * <code>Sensor</code> agent, the agent continuously handles instructions and
 * broadcast frames coming from the <code>Simulation</code> agent.
 * 
 * @author Anca
 * @see Instruction
 * @see Frame
 */
@SuppressWarnings("serial")
public class MainLoop extends CyclicBehaviour {

	/** Template for receiving instructions from the Sim Agent */
	static MessageTemplate templateReceiveInstruction = MessageTemplate
			.MatchPerformative(ACLMessage.REQUEST);
	/** Template for receiving broadcasted frames (dispatched by the Sim agent) */
	static MessageTemplate templateReceiveFrame = MessageTemplate
			.MatchPerformative(ACLMessage.PROPAGATE);

	private Sensor agent;

	/**
	 * @param sensor
	 */
	public MainLoop(Sensor sensor) {
		agent = sensor;
	}

	@Override
	public void action() {

		for (int i = 0; i < 100; i++){
			ACLMessage msg = agent.receive();
	
			if (msg != null) {
				// RECEIVING INSTRUCTIONS FROM THE SIMULATOR AGENT
				if (templateReceiveInstruction.match(msg)) {
					try {
						Instruction instruction = (Instruction) msg.getContentObject();
						new InstructionHandler(agent, instruction);
					} catch (UnreadableException e1) {
						e1.printStackTrace();
					}
	
					// RECEIVING THE FRAMES DISPATCHED BY THE SIMULATOR AGENT
				} else if (templateReceiveFrame.match(msg)) {
					try {
						Frame frame = (Frame) msg.getContentObject();
						new FrameHandler(agent, frame);
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}