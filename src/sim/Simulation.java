package sim;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sim.eval.Report;
import sim.events.Event;
import sim.events.ReceivedFrameEvent;
import sim.events.ReceivedMeasureEvent;
import sim.events.SentMeasureEvent;
import sim.events.UnauthorizedMessageEvent;
import sim.scn.NodeDescription;
import sim.scn.Organization;
import sim.scn.Scenario;
import sim.scn.act.ActionDescription;
import sim.scn.instr.Instruction;

@SuppressWarnings("serial")
public class Simulation extends Agent {

	Scenario scenario;
	Report report;
	
	private boolean reportClosed;
	
	AID[] sensorsAID;
	
	int receivedMeasures = 0;
	int sentMeasures = 0;
	
	List<String> receivedM = new ArrayList<String>();
	
	
	
	protected void setup() {
			
		Object[] args = getArguments();

		scenario = new Scenario((String) args[0]);
		report = new Report((String) args[0]);

		Organization org = scenario.getOrganization();

		System.out.println("Organization loaded...");
		
		List<ActionDescription> actions = scenario.getActions();
		
		
		
		sensorsAID = new AID[org.getNodeDescriptions().size()];
		
		// CREATE & START AGENTS
		for (NodeDescription nd : org.getNodeDescriptions()) {

			// preparing arguments 
			Object[] arguments = new Object[4];
			arguments[0] = nd.getRole().toString();
			arguments[1] = nd.getGroups();
			arguments[2] = org.getNodeNeighbourhood().get(nd.getId());
			arguments[3] = getLocalName(); // simAgentAID

			// create and start
			try {
				getContainerController().createNewAgent(nd.getId() + "","sim.Sensor", arguments).start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
		
		// HANDLING MESSAGES FROM AGENTS
		addBehaviour(new CyclicBehaviour(this) {
			
			long dateLastDispatch = System.currentTimeMillis() + 50000;
			long dateLastNotification = System.currentTimeMillis() + 50000;
			
			@Override
			public void action() {
				ACLMessage msg = receive();
				
				if(msg != null){
					// HANDLE REQUEST TO DISPATCH FRAME (Broadcast)
					if(msg.getPerformative() == ACLMessage.PROPAGATE){
						dispatch(msg);
						dateLastDispatch = System.currentTimeMillis();
						
					// HANDLE NOTIFICATIONS FROM AGENTS	
					} else if (msg.getPerformative() == ACLMessage.CONFIRM) {
						dateLastNotification = System.currentTimeMillis();
						try {
							
							Event event = (Event) msg.getContentObject();
							
							if(event instanceof ReceivedMeasureEvent){
								ReceivedMeasureEvent rme = (ReceivedMeasureEvent) event;
								report.addReceivedMeasure(rme.getSource(), rme.getMeasure());
							} else if (event instanceof SentMeasureEvent){
								SentMeasureEvent sme = (SentMeasureEvent) event;
								report.addSentMeasure(sme.getSource(), sme.getMeasure());
							} else if (event instanceof ReceivedFrameEvent){
								report.addFramesReceived(1);
							} else if (event instanceof UnauthorizedMessageEvent){
								UnauthorizedMessageEvent ume = (UnauthorizedMessageEvent) event;
								System.out.println(ume.getSource()
										+ ": UNAUTHORIZED "
										+ ume.getMessageType() + " sender "
										+ ume.getMsgSender() + " Source "
										+ ume.getMsgSource());
								
							}
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
				}
				
				// END SIMULATION
				long crtTime = System.currentTimeMillis();
				if(crtTime > dateLastNotification + 5000 && crtTime > dateLastDispatch + 5000){
					if(!reportClosed){
						report.close();
						reportClosed = true;
						System.out.println("Report file written. Bye!");
						System.exit(0);
					}					
				}
			}
		});
		
		
	
		// SEND OUT INSTRUCTIONS FOR AGENTS		
		for (int i = 0; i < actions.size() - 1; i++) {
			List<Instruction> iList = scenario.buildInstructionList(actions.get(i));
			
			for (Instruction instr : iList){
				
				sendInstruction(instr);
			}
			sleep(scenario.sleepTime(i));
		}

		List<Instruction> last = scenario.buildInstructionList(actions.get(actions.size()-1));
		for(Instruction instr : last)
			sendInstruction(instr);

		
	}
	
	// Wireless communication -- dispatch frame to all neighbours of the sender	
	private void dispatch(ACLMessage message){
		int sender = Integer.parseInt(message.getSender().getLocalName());
		
		List<Integer> receivers  = scenario.getOrganization().getNeighbours(sender);
		message.removeReceiver(getAID());
		
		report.addFramesSent(receivers.size() - 1);
		
		for(Integer r : receivers)
			message.addReceiver(new AID(r.toString(), AID.ISLOCALNAME));
		
		send(message);
	}
	
	// Send instructions to agents
	private void sendInstruction(Instruction instr){
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);			
		
		request.addReceiver(instr.getReceiver());
		
		try {
			request.setContentObject(instr);
			send(request);
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sleep(long ms){
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
		}
	}
}
