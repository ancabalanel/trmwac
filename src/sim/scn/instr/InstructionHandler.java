/**
 * 
 */
package sim.scn.instr;

import sim.Sensor;
import sim.behaviours.SendPeriodicFabricatedMessage;
import sim.behaviours.SendPeriodicMeasures;
import sim.events.BecomeMaliciousEvent;

/**
 * @author Anca
 * 
 */
public class InstructionHandler {
	Sensor agent;
	Instruction instruction;
	
	public InstructionHandler(Sensor agent, Instruction instruction) {
		this.agent = agent;
		this.instruction = instruction;
	
		if (instruction instanceof SendMeasuresInstruction) 
			handleInstructionSendMeasures((SendMeasuresInstruction) instruction);
		
		else if (instruction instanceof FabricateMessageInstruction) 
			handleInstructionFabricateMessage((FabricateMessageInstruction) instruction);
		
		else if (instruction instanceof NoForwardInstruction) 
			handleInstructionNoForward((NoForwardInstruction) instruction);
		
		else if (instruction instanceof ModifyMessageInstruction) 
			handleModifyMessage((ModifyMessageInstruction) instruction);	
	}
	
	private void handleInstructionSendMeasures(SendMeasuresInstruction send ){
		long interval = send.getInterval();

		int destination = send.getDestination();
		agent.generateMeasuresToSend(send.getNumMeasures());

		agent.setSendMeasuresBehaviour(new SendPeriodicMeasures(agent, interval, destination));
		agent.addBehaviour(agent.getSendMeasuresBehaviour());
	}
	
	private void handleInstructionFabricateMessage(FabricateMessageInstruction fabricate){
		agent.sendNotification(new BecomeMaliciousEvent(agent.getId()));
		agent.addBehaviour(new SendPeriodicFabricatedMessage(agent, fabricate.getInterval(),fabricate));
		
		agent.setUseTrust(false);
		agent.setUseAuthorization(false);
	}
	
	private void handleInstructionNoForward(NoForwardInstruction noFwd){
		agent.sendNotification(new BecomeMaliciousEvent(agent.getId()));
		
		if (agent.getSendMeasuresBehaviour() != null)
			agent.getSendMeasuresBehaviour().stop();
		
		agent.setNofwd(true);
		agent.setDropProb(noFwd.getDropProbability());
		
		agent.setUseTrust(false);
		agent.setUseAuthorization(false);
	}

	private void handleModifyMessage(ModifyMessageInstruction modify) {
		agent.sendNotification(new BecomeMaliciousEvent(agent.getId()));
		
		if (agent.getSendMeasuresBehaviour() != null)
			agent.getSendMeasuresBehaviour().stop();
		
		agent.setModifier(true);
		agent.setModMsgType(modify.getMsgType());
		agent.setModProb(modify.getModProb());
		
		agent.setUseTrust(false);
		agent.setUseAuthorization(false);
	}
	
}
