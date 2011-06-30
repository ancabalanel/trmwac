package sim;

/**
 * 
 */

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mwac.Groups;
import mwac.Neighbourhood;
import mwac.Role;
import mwac.RoutingManager;
import mwac.RoutingManager.RoutingInfo;
import mwac.msgs.Frame;
import mwac.msgs.MData;
import mwac.msgs.MIntroduction;
import mwac.msgs.MPresentation;
import mwac.msgs.MRouteReply;
import mwac.msgs.MRouteRequest;
import mwac.msgs.MRoutedData;
import mwac.msgs.Message;
import sim.events.Event;
import sim.events.ReceivedFrameEvent;
import sim.events.ReceivedMeasureEvent;
import sim.events.SentMeasureEvent;
import sim.events.UnauthorizedMessageEvent;
import sim.scn.instr.FabricateMessageInstruction;
import sim.scn.instr.Instruction;
import sim.scn.instr.ModifyMessageInstruction;
import sim.scn.instr.NoForwardInstruction;
import sim.scn.instr.SendMeasuresInstruction;

/**
 * @author Anca
 * 
 */
@SuppressWarnings("serial")
public class Sensor extends Agent {

	private static MessageTemplate templateReceiveInstruction = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
	private static MessageTemplate templateReceiveFrame = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);

	private static int UNKNOWN = 100;
	private static Random random = new Random();

	int id;
	Role role;
	Groups groups;
	Neighbourhood neighbourhood;

	AID simAgentAID;

	Status status;

	RoutingManager routingManager;

	Map<Integer, DataInfo> mts;
	List<String> measuresToSend;

	SendPeriodicMeasures sendMeasuresBehaviour = null;

	class DataInfo {
		MData mdata;
		boolean wasSent;

		public DataInfo(MData mdata) {
			this.mdata = mdata;
			wasSent = false;
		}

		public void setWasSent() {
			wasSent = true;
		}
	}

	class MainCycle extends CyclicBehaviour{

		@Override
		public void action() {
			ACLMessage msg = receive();

			if (msg != null) {
				
				// RECEIVING INSTRUCTIONS FROM THE SIMULATOR AGENT
				if (templateReceiveInstruction.match(msg)) {
					try {
						Instruction instr = (Instruction) msg.getContentObject();
						handleInstruction(instr);
					} catch (UnreadableException e1) {
						e1.printStackTrace();
					}
				
				// RECEIVING THE FRAMES DISPATCHED BY THE SIMULATOR AGENT
				} else if (templateReceiveFrame.match(msg)) {
					try {
						Frame frame = (Frame) msg.getContentObject();
						handleFrame(frame);
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
			}
		}		
	}
	
	protected void setup() {

		this.id = Integer.parseInt(getLocalName());
		this.routingManager = new RoutingManager();
		this.measuresToSend = new ArrayList<String>();
		this.mts = new HashMap<Integer, DataInfo>();

		Object[] args = getArguments();

		role = Role.valueOf((String) args[0]);
		groups = (Groups) (args[1]);
		neighbourhood = (Neighbourhood) (args[2]);
		simAgentAID = new AID((String) args[3], AID.ISLOCALNAME);

		status = new Status();
		status.authorization = true;
		
		addBehaviour(new MainCycle());
		
	}

	@Override
	protected void takeDown() {
		super.takeDown();
	}

	private void handleFrame(Frame frame) {
		Message msg = frame.getMessage();

		sendNotification(new ReceivedFrameEvent(id));

		if (status.promiscuousMode)
			handleMessage(msg, frame.getSender());
		else if (indendedReceiver(frame))
			handleMessage(msg, frame.getSender());
		
	}

	private void handleMessage(Message msg, int frameSender) {
		
		if(status.authorization)
			if (!authorizeMessage(msg, frameSender)){
				sendNotification(new UnauthorizedMessageEvent(id, msg.getClass().getCanonicalName(), msg.getSource(), frameSender));
				return;
			}
		
		
		if (msg instanceof MRouteRequest) {
			handleMessageRouteRequest((MRouteRequest) msg, frameSender);
			return;
		}
		if (msg instanceof MRouteReply) {
			handleMessageRouteReply((MRouteReply) msg, frameSender);
			return;
		}
		if (msg instanceof MRoutedData) {
			handleMessageRoutedData((MRoutedData) msg, frameSender);
		}
		if (msg instanceof MData) {
			handleMessageData((MData) msg, frameSender);
		}
	}

	/**
	 * Handle route requests messages
	 * 
	 * @param rreq
	 *            the message
	 * @param frameSender
	 */
	private void handleMessageRouteRequest(MRouteRequest rreq, int frameSender) {

		int destination = rreq.getDestination();

		// If I haven't processed this request before...
		if (!routingManager.wasProcessed(rreq)) {
			
			// For representatives:
			if (role == Role.Representative) {
				// If I am the destination, or the representative of the
				// destination
				if (id == destination || neighbourhood.contains(destination)) {

					MRouteReply rrep = new MRouteReply(id, rreq);

					if (!status.drop())
						sendFrame(rrep, id); // ... send a route reply

				} else { // If I don't know the destination

					// put my id on the route
					List<Integer> route = rreq.getRoute();
					route.add(id);
					rreq.setRoute(route);
					if (!status.drop())
						sendFrame(rreq, frameSender); // ... and forward the
														// request
				}
			} else { // For Links:
				if (!status.drop())
					sendFrame(rreq, frameSender); // just forward the request
			}

			routingManager.process(rreq);// remember received rreq

		} else {
			// ignore already processed route requests
		}
	}

	private void handleMessageRouteReply(MRouteReply rrep, int frameSender) {

		int destination = rrep.getDestination();

		if (id == destination) { // I am the one who initiated the request

			// DEBUG("Received ROUTE REPLY: " + rrep);
			if (role == Role.Representative) {

				// retrieve the data corresponding to the reply that just arrived
				DataInfo dataInfo = mts.get(rrep.getRequestId());

				if (dataInfo == null) // no data needs to be sent back
					return;

				MData mdata = dataInfo.mdata;
				int finalDest = mdata.getDestination(); // final destination

				List<Integer> route = rrep.getRoute(); // new route

				// Update the routing table with the new route
				if (routingManager.haveRoute(finalDest)) { // If I had a route
					List<Integer> oldRoute = routingManager.getRoutingInfo(finalDest).getRoute();
					if (route.size() < oldRoute.size()) // ... if the old route was longer, replace it
						routingManager.addRoute(finalDest, rrep.getSource(), route);
				} else { // If i had no route, add the new route
					routingManager.addRoute(finalDest, rrep.getSource(), route);
				}

				// If the message was not sent already...
				if (!dataInfo.wasSent) {
					// Send the routed data, back to the source of the reply
					// (representative of the destination)
					MRoutedData rdata = new MRoutedData(id, rrep.getSource(), mdata, route);

					if (!status.drop()) {
						sendFrame(rdata, id);
						dataInfo.setWasSent(); // mark data as sent
					}
					// no notification needed here: it has already been sent in
					// "SendPeriodicMeasures" behaviour
				}

			} else {
				// there must be a mistake: only representatives can initiate
				// route requests
			}
		} else { // I am not the initiator of the request
			if (!status.drop())
				sendFrame(rrep, frameSender); // just forward the request
		}
	}

	private void handleMessageRoutedData(MRoutedData rdata, int frameSender) {

		int repDest = rdata.getDestination(); // this is the id of the
												// representative of the
												// destination
		MData data = rdata.getData();
		int dataDest = data.getDestination(); // this is the id of the actual
												// destination

		if (role == Role.Simple) {
			// there must be a mistake
			return;
		}

		if (id == repDest) { // The routed data has reached its destination
								// (representative)

			if (role == Role.Representative) {
				if (id == dataDest) { // data has reached its final destination
										// (which is also its representative)
					sendNotification(new ReceivedMeasureEvent(id,
							data.getData()));
					return;
				} else {
					if (neighbourhood.contains(dataDest)) {
						if (!status.drop())
							sendFrame(data, id); // deliver the data to its
													// actual destination
					} else {
						// there must be a mistake
					}
				}
			} else { // ... destination is not a representative
				// there must be a mistake: the destination of routed data may
				// only be a Representative
			}
		} else { // The routed data has not reached its destination yet
			if (!status.drop())
				sendFrame(rdata, frameSender); // just forward the message
		}

	}

	private void handleMessageData(MData data, int frameSender) {

		// int source = data.getSource();
		int destination = data.getDestination();

		if (id == destination) { // I message reached destination, I'm done
			sendNotification(new ReceivedMeasureEvent(id, data.getData()));
			return;
		} else {
			if (role == Role.Representative) {

				if (neighbourhood.contains(destination)) {
					// deliver message...
					if (!status.drop())
						sendFrame(data, id);
				} else if (routingManager.haveRoute(destination)) {

					// If I have a route...
					RoutingInfo rinfo = routingManager
							.getRoutingInfo(destination);

					List<Integer> route = rinfo.getRoute();
					int repId = rinfo.getRepDest();

					// send the data on that route
					MRoutedData rdata = new MRoutedData(id, repId, data, route);
					if (!status.drop())
						sendFrame(rdata, id);

					// no need for notification here: it has been sent from the
					// "SendPeriodicMeasures" behaviour

					// DEBUG("Have route...sending ROUTED DATA " + rdata);

				} else { // If I don't have a route
					int reqId = routingManager.generateRREQ(data
							.getDestination());
					MRouteRequest rreq = new MRouteRequest(id, destination,
							reqId, new ArrayList<Integer>());

					routingManager.process(rreq); // remember not to process again
					mts.put(rreq.getRequestId(), new DataInfo(data)); // remember what message to send
					if (!status.drop())
						sendFrame(rreq, id); // search for route
					// no need for notification here.
					// DEBUG("No route...sending ROUTE REQUEST " + rreq);
				}
			} else {
				// there must be a mistake, if I am not the destination nor a
				// representative
			}
		}
	}
	
	private void handleInstruction(Instruction instr){
		if (instr instanceof SendMeasuresInstruction) 
			handleInstructionSendMeasures((SendMeasuresInstruction) instr);
		
		else if (instr instanceof FabricateMessageInstruction) 
			handleInstructionFabricateMessage((FabricateMessageInstruction) instr);
		
		else if (instr instanceof NoForwardInstruction) 
			handleInstructionNoForward((NoForwardInstruction) instr);
		
		else if (instr instanceof ModifyMessageInstruction) 
			handleModifyMessage((ModifyMessageInstruction) instr);	
	}
	
	private void handleInstructionSendMeasures(SendMeasuresInstruction send ){
		long interval = send.getInterval();

		int destination = send.getDestination();
		generateMeasuresToSend(send.getNumMeasures());

		sendMeasuresBehaviour = new SendPeriodicMeasures(Sensor.this, interval, destination);
		Sensor.this.addBehaviour(sendMeasuresBehaviour);
	}
	
	private void handleInstructionFabricateMessage(FabricateMessageInstruction fabricate){
		addBehaviour(new SendPeriodicFabricatedMessage(Sensor.this, fabricate.getInterval(),fabricate));
	}
	
	private void handleInstructionNoForward(NoForwardInstruction noFwd){

		if (sendMeasuresBehaviour != null)
			sendMeasuresBehaviour.stop();
		
		status.nofwd = true;
		status.dropProb = noFwd.getDropProbability();

		DEBUG("Received no forward " + noFwd);
	}

	private void handleModifyMessage(ModifyMessageInstruction modify) {
		DEBUG("Received modify" + modify);
	}
	
	
	// AUXILIARY METHODS
	
	/**
	 * 
	 * @param msg
	 * @param frameSender
	 * @return true if the message complies with the protocol, false if
	 */
	private boolean authorizeMessage(Message msg, int frameSender) {
		boolean ok = true;
		Role sourceRole = neighbourhood.getRole(msg.getSource());
		Role senderRole = neighbourhood.getRole(frameSender);
		
		if (msg instanceof MData) {
			// Simple agents can only be the destination of Data; 
			// the source of Data is either a neighbour or
			// the source is unknown and the sender is a Representative neighbour
			if (role == Role.Simple || role == Role.Link) {
				ok = (msg.getDestination() == id && (msg.getSource() == frameSender || (sourceRole == Role.Unknown && senderRole == Role.Representative)));
			} else if (role == Role.Representative) { 
				ok = (msg.getSource() == frameSender);
			}
		} else if (msg instanceof MRoutedData) {
			if(role == Role.Simple)
				ok = false;
			else if (role == Role.Link) {
				ok = !((msg.getDestination() == id)
						|| (senderRole == Role.Simple) || (senderRole == Role.Link));
			} else if (role == Role.Representative) {
				ok = !(senderRole == Role.Simple || (senderRole == Role.Link && sourceRole != Role.Unknown));
			}
		} else if (msg instanceof MRouteRequest || msg instanceof MRouteReply) {
			if(role == Role.Simple){
				ok = false;
			} else if (role == Role.Link){
				ok = (senderRole == Role.Representative) && (msg.getSource() == frameSender || sourceRole == Role.Unknown || sourceRole == Role.Representative);
			} else if (role == Role.Representative){
				ok = (senderRole == Role.Link) && (sourceRole == Role.Unknown);
			}
		} 
	
		return ok;
	}

	/**
	 * Checks if this agent is intended to receive this frame
	 * 
	 * @param frame
	 * @return true if the id and role of this agent match the intended receiver
	 *         of the frame
	 */
	private boolean indendedReceiver(Frame frame) {
		return frame.getReceiver() == id
				|| frame.getReceiver() == Frame.BROADCAST
				|| ((role == Role.Representative) && (frame.getReceiver() == Frame.BROADCAST_REPRESENTATIVE))
				|| ((role == Role.Link) && (frame.getReceiver() == Frame.BROADCAST_LINK));
	}

	/**
	 * Builds a frame, according to the type of message.
	 * 
	 * @param msg
	 * @param frameSender
	 *            the id of the agent from which the frame containing the
	 *            message came. Used only by RREP and RData messages, to compute
	 *            the receiver field of the frame.
	 * @return the frame to send
	 */
	private Frame wrapMessage(Message msg, int frameSender) {
		Frame frame = new Frame(id, Frame.BROADCAST, msg);

		// ...
		if (msg instanceof MIntroduction || msg instanceof MPresentation) {
			frame = new Frame(id, Frame.BROADCAST, msg);

			// ROUTE REQUEST
		} else if (msg instanceof MRouteRequest) {
			if (role == Role.Representative) {
				frame = new Frame(id, Frame.BROADCAST_LINK, msg);
			} else {
				frame = new Frame(id, Frame.BROADCAST_REPRESENTATIVE, msg);
			}

			// ROUTE REPLY
		} else if (msg instanceof MRouteReply) {

			int relay;
			MRouteReply rrep = (MRouteReply) msg;
			
			relay = getRelayRREP(rrep, frameSender);
			if(relay > 0)
				frame = new Frame(id, relay, rrep);
			else frame = null;

			// ROUTED DATA
		} else if (msg instanceof MRoutedData) {
			int relay;
			MRoutedData rdata = (MRoutedData) msg;

			relay = getRelayRData(rdata, frameSender);
			if(relay > 0)
				frame = new Frame(id, relay, rdata);
			else
				frame = null;

			// DATA
		} else if (msg instanceof MData) {
			if (neighbourhood.contains(msg.getDestination()))
				frame = new Frame(id, msg.getDestination(), msg);
			else {
				if (role != Role.Representative) {
						int repId = neighbourhood.getRepresentative();
						frame = new Frame(id, repId, msg);
				} else {
					// destination unknown
				}
			}
		}
		return frame;
	}

	private int getRelayRREP(MRouteReply rrep, int frameSender) {
		List<Integer> route = rrep.getRoute();
		int relay;

		if (route.isEmpty())
			relay = rrep.getDestination();
		else {
			int index;
			if (role == Role.Representative)
				index = route.indexOf(id);
			else
				index = route.indexOf(frameSender);
			
			

			if (index == -1) {
				relay = route.get(route.size() - 1);
			} else if (index > 0)
				relay = route.get(index - 1);
			else
				relay = rrep.getDestination();
			//DEBUG(rrep + " INDEX = " + index + " RELAY " + relay);
		}

		if (role == Role.Representative){ 
			try { relay = neighbourhood.getLinkToRep(relay); } catch (Exception e) {
				// TODO: messed up route event
				DEBUG("(RREP) Messed up route" + route);
			}
		} else if (role == Role.Link){
			Role relayRole = neighbourhood.getRole(relay);
			if(relayRole != Role.Representative) {
				DEBUG("RELAY NOT KNOWN OR ROUTE CONTAINS LINKS!!!!" + rrep);
				if(status.authorization){
					sendNotification(new UnauthorizedMessageEvent(id, rrep.getClass().getCanonicalName(), rrep.getSource(),frameSender));
					return -1;
				}
			}			
		}

		return relay;
	}

	private int getRelayRData(MRoutedData rdata, int frameSender) {
		List<Integer> route = rdata.getRoute();
		int relay;

		if (route.isEmpty()) {
			relay = rdata.getDestination();
		} else {
			int index;

			if (role == Role.Representative)
				index = route.indexOf(id);
			else
				index = route.indexOf(frameSender);
			if (index == -1)
				relay = route.get(0);
			else if (index < route.size() - 1)
				relay = route.get(index + 1);
			else
				relay = rdata.getDestination();
		}

		if (role == Role.Representative){ 
			try {
				relay = neighbourhood.getLinkToRep(relay);
			} catch (Exception e) {
				// TODO messed up route event
				DEBUG("(RDATA) Messed up route" + route);
			}
		} else if (role == Role.Link) {
			Role relayRole = neighbourhood.getRole(relay);
			if(relayRole != Role.Representative) {
				if(status.authorization){
					DEBUG("ROUTE CONTAINS LINKS OR RELAY NOT KNOWN!!!!");
					sendNotification(new UnauthorizedMessageEvent(id, rdata.getClass().getCanonicalName(), rdata.getSource(),frameSender));
					return -1;
				}
			}			
		}
		return relay;
	}

	private void generateMeasuresToSend(int num) {
		for (int i = 0; i < num; i++)
			measuresToSend.add(id + "-measure-" + (i + 1));
	}

	private void sendFrame(Message msg, int frameSender) {
		ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPAGATE);
		aclMessage.addReceiver(simAgentAID);

		Frame frame = wrapMessage(msg, frameSender);
		if (frame != null){
			try {
				aclMessage.setContentObject(frame);
				send(aclMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			DEBUG("NOT SENDING FRAME");
		}
	}

	private void sendMaliciousFrame(Frame frame){
		ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPAGATE);
		aclMessage.addReceiver(simAgentAID);
	
		try {
			aclMessage.setContentObject(frame);
			send(aclMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendNotification(Event event) {
		ACLMessage notification = new ACLMessage(ACLMessage.CONFIRM);
		notification.addReceiver(simAgentAID);
		try {
			notification.setContentObject(event);
			addBehaviour(new AchieveREInitiator(this, notification));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fabricateMessage(String msgType) {
		
		// these are not to be changed
		int sender = id;
		int receiver = Frame.BROADCAST;
		
		
		int source = id; // UNKNOWN
		int destination = UNKNOWN;
		
		if (msgType.equals("RouteRequest")) {
			int requestId = UNKNOWN + random.nextInt(100); // choose a convenient request id
			List<Integer> route = new ArrayList<Integer>();
			MRouteRequest rreq = new MRouteRequest(source, destination,	requestId, route);
			sendMaliciousFrame(new Frame(sender, receiver, rreq));

		} else if (msgType.equals("RouteReply")) {
			int requestId = UNKNOWN + random.nextInt(100);
			List<Integer> route = new ArrayList<Integer>();
			
			route.add(2);
			route.add(3);
			route.add(4);
			MRouteReply rrep = new MRouteReply(source, destination, requestId,	route);
			DEBUG("SENDING FABRICATED ROUTE REPLY :" + rrep);
			sendMaliciousFrame(new Frame(sender, receiver, rrep));
			
		} else if (msgType.equals("RoutedData")) {
			MData data = new MData(source, 5, "fabricated");
			List<Integer> route = new ArrayList<Integer>();
			route.add(11);
			route.add(9);
			MRoutedData rdata = new MRoutedData(source, destination, data,	route);
			sendMaliciousFrame(new Frame(sender, Frame.BROADCAST, rdata));
		} else if (msgType.equals("Data")){
			MData data = new MData(UNKNOWN, destination, "fabricated"); 
			sendMaliciousFrame(new Frame(sender, receiver, data));
		}
	}
	
	public void modifyMessage(Message message) {
	
	}

	private void DEBUG(String message) {
		System.out.println(id + "\t" + message);
	}

	class SendPeriodicMeasures extends TickerBehaviour {

		int destination;

		public SendPeriodicMeasures(Agent a, long period, int destination) {
			super(a, period);
			this.destination = destination;
		}

		@Override
		protected void onTick() {

			if (!measuresToSend.isEmpty()) {

				String next = measuresToSend.remove(0);
				MData mdata = new MData(id, destination, next);

				// If i know the destination, send directly.
				// The role doesn't matter
				if (neighbourhood.contains(destination)) {
					sendFrame(mdata, id);
					sendNotification(new SentMeasureEvent(id, mdata.getData()));
				} else {
					// For representatives:
					if (role == Role.Representative) {

						if (routingManager.haveRoute(destination)) { // if i
																		// have
																		// a
																		// route...

							List<Integer> route = routingManager
									.getRoutingInfo(destination).getRoute();
							MRoutedData rdata = new MRoutedData(id,
									destination, mdata, route);

							// ... send the data on that route, and notify the
							// sim agent
							sendFrame(rdata, id);
							sendNotification(new SentMeasureEvent(id,
									mdata.getData()));

						} else { // if i don't have a route ...

							int reqId = routingManager
									.generateRREQ(destination);
							MRouteRequest rreq = new MRouteRequest(id,
									destination, reqId,
									new ArrayList<Integer>());

							// remember data to send, for when i receive the route reply
							mts.put(reqId, new DataInfo(mdata)); 
																	
							routingManager.process(rreq); // remember request id

							sendFrame(rreq, id); // search for route
							sendNotification(new SentMeasureEvent(id, mdata.getData())); // notify sim agent
						}
					} else { // For non representatives:

						// ... send data to my representative agent (see
						// wrapMessage), and notify the sim agent
						sendFrame(mdata, id);
						sendNotification(new SentMeasureEvent(Sensor.this.id, mdata.getData()));
					}
				}

			} else {// I have no more measures to send

				sendMeasuresBehaviour.stop();
			}
		}
	}

	class SendPeriodicFabricatedMessage extends TickerBehaviour {

		FabricateMessageInstruction fab;
		int times = 0;

		public SendPeriodicFabricatedMessage(Agent a, long period,
				FabricateMessageInstruction instr) {
			super(a, period);
			this.fab = instr;

		}

		@Override
		protected void onTick() {
			if (times < fab.getTotalMsgs()) {

				fabricateMessage(fab.getMessageType());

				times++;
			} else {
				Sensor.this.removeBehaviour(this);
			}
		}
	}

	class Status {
		boolean promiscuousMode;
		boolean authorization;

		boolean useTrust;

		boolean fabricator;
		boolean modifier;

		boolean nofwd;
		float dropProb;

		public Status() {
			promiscuousMode = false;
			authorization = false;
			useTrust = false;

			fabricator = false;
			modifier = false;
			nofwd = false;
			dropProb = 0.0f;
		}

		public boolean drop() {
			return nofwd && random.nextFloat() < dropProb;
		}

		public boolean isMalicious() {
			return fabricator || modifier || nofwd;
		}
	}
}