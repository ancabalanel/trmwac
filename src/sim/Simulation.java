package sim;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.List;

import sim.eval.Parameters;
import sim.eval.Report;
import sim.events.BecomeMaliciousEvent;
import sim.events.CorruptedRouteEvent;
import sim.events.DistrustNeighbourEvent;
import sim.events.Event;
import sim.events.ReceivedFrameEvent;
import sim.events.ReceivedMeasureEvent;
import sim.events.SentMeasureEvent;
import sim.events.TrustDecreasedEvent;
import sim.events.UnauthorizedMessageEvent;
import sim.scn.NodeDescription;
import sim.scn.Organization;
import sim.scn.Scenario;
import sim.scn.act.ActionDescription;
import sim.scn.instr.Instruction;

/**
 * The <code> Simulation </code> class is a subclass of the Jade
 * <code> Agent </code> class. A <code> Simulation </code> agent holds a
 * scenario for the sensor network simulation, and sends out instructions to
 * <code>Sensor</code> agents in the MWAC organization. It also plays the role
 * of the environment, by dispatching frames broadcasted by Sensors.
 * <p>
 * The Simulation agent (sim agent) builds a report at the end of a simulation
 * </p>
 * 
 * @author Anca
 * @see Sensor
 * @see Scenario
 * @see Report
 */
@SuppressWarnings("serial")
public class Simulation extends Agent {

	
	long simulationStart = System.currentTimeMillis();
	
	Scenario scenario;
	Report report;
	
	private boolean reportClosed;
	
	AID[] sensorsAID;
	
	@Override
	protected void setup() {
			
		Object[] args = getArguments();

		scenario = new Scenario((String) args[0]);
		report = new Report(getHtmlFileName((String) args[0]));
		

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
		
		simulationStart = System.currentTimeMillis();
		
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
								report.addUnauthorizedMessage(ume.getSource(), ume.getMessageType(), ume.getMsgSource(), ume.getMsgSender());
							} else if (event instanceof BecomeMaliciousEvent){
								report.addMaliciousAgent(event.getSource());
								scenario.getOrganization().setMalicious(event.getSource(), true);
							} else if (event instanceof CorruptedRouteEvent){
								CorruptedRouteEvent cre = (CorruptedRouteEvent) event;
								report.addCorruptedRoute(cre.getSource(), cre.getRoute(), cre.getFrameSender());
							} else if (event instanceof TrustDecreasedEvent) {
								TrustDecreasedEvent tde = (TrustDecreasedEvent) event;
								report.addNeighbourTrust(tde.getSource(), tde.getNbTrust());
							} else if (event instanceof DistrustNeighbourEvent){
								DistrustNeighbourEvent dis = (DistrustNeighbourEvent)event;
								report.addDistrustNode(dis.getSource(), dis.getNeighbour());
							}
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
				}
				
				// END SIMULATION
				long crtTime = System.currentTimeMillis();
				if (crtTime > dateLastNotification + Parameters.DELAY_END
						|| crtTime > dateLastDispatch + Parameters.DELAY_END 
						|| crtTime > simulationStart + Parameters.MAXIMUM_TIME) {
					if(!reportClosed){
						report.close();
						reportClosed = true;
						System.out.println("Report file written. Bye!");
						System.exit(0);
					}					
				}
			}
		});
		
		
		if (!actions.isEmpty()) {
			// SEND OUT INSTRUCTIONS FOR AGENTS
			for (int i = 0; i < actions.size() - 1; i++) {

				List<Instruction> instructionList = scenario.buildInstructionList(actions.get(i));

				for (Instruction instr : instructionList){
					sendInstruction(instr);
					
				}

				sleep(scenario.sleepTime(i));
			}

			List<Instruction> last = scenario.buildInstructionList(actions.get(actions.size() - 1));
			for (Instruction instr : last)
				sendInstruction(instr);
		}
	}

	/**
	 * Sends the <code>Frame</code> contained in the <code>ACLMessage</code> to
	 * all the neighbours of the senders
	 * 
	 * @param message
	 * @see Organization
	 */
	private void dispatch(ACLMessage message){
		int sender = Integer.parseInt(message.getSender().getLocalName());
		
		List<Integer> receivers  = scenario.getOrganization().getNeighbours(sender);
		message.removeReceiver(getAID());
		
		report.addFramesSent(receivers.size() - 1);
		
		for(Integer r : receivers)
			message.addReceiver(new AID(r.toString(), AID.ISLOCALNAME));
		
		send(message);
	}

	/**
	 * Sends an ACL message containing an <code>Instruction</code> to all the
	 * agents that should follow the instruction
	 * 
	 * @param instr the instruction sent by the <code>Simulation</code> agent
	 * @see Instruction
	 */
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

	/**
	 * Obtains the name of the file in which the results will be placed.
	 * 
	 * @param fileName
	 *            the name of the file containing the scenario description for
	 *            this simulation
	 * @return the name of the output file (a .html)
	 * @see Report
	 */
	private static String getHtmlFileName(String fileName){
		String tmp  = String.copyValueOf(fileName.toCharArray());
		return "output/" + tmp.substring(tmp.indexOf("/") + 1, tmp.indexOf(".")) + ".html";		
	}

	/**
	 * Specifies that the simulation halts for a while.
	 * 
	 * @param ms
	 *            number of milliseconds during which the agent sleeps
	 */
	private void sleep(long ms){
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
		}
	}
}
