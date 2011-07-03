package sim;

/**
 * 
 */

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mwac.DataInfo;
import mwac.Groups;
import mwac.Neighbourhood;
import mwac.Role;
import mwac.RoutingInfo;
import mwac.RoutingManager;
import mwac.msgs.Frame;
import mwac.msgs.MData;
import mwac.msgs.MRouteReply;
import mwac.msgs.MRouteRequest;
import mwac.msgs.MRoutedData;
import sim.behaviours.MainLoop;
import sim.behaviours.SendPeriodicMeasures;
import sim.events.Event;
import sim.scn.instr.Instruction;

/**
 * The <code> Sensor </code> extends the <code> Agent </code> class from Jade.
 * The <code> Sensor </code> agent represents an MWAC agent, part of an
 * instrumentation system.
 * <p>
 * The agent can receive <code>Instruction</code>s from the sim agent, and
 * execute a different behaviour, according to what it is supposed to do:
 * <ul>
 * <li>send periodic measures to a workstation</li>
 * <li>send fabricated messages</li>
 * <li>forward modified messages</li>
 * <li>lose messages</li>
 * </ul>
 * <code>Sensors</code> also send notifications of different events to the
 * <code>Simulation</code> agent.
 * </p>
 * 
 * @author Anca
 * @see Instruction
 * @see Role
 * @see Groups
 * @see Neighbourhood
 * @see Event
 */
@SuppressWarnings("serial")
public class Sensor extends Agent {


	/** used by malicious agents to generate fake ids and routes */
	private static int UNKNOWN_ID = 100;
	/** used by malicious agents to generate fake ids and routes */
	static Random random = new Random();

	
	public static int generateFakeId(){
		return UNKNOWN_ID + random.nextInt(100);
	}	
	public static List<Integer> generateFakeRoute(){
		List<Integer> fRoute = new ArrayList<Integer>();
		int length = random.nextInt(10);
		for (int i = 0; i < length; i++){
			fRoute.add(random.nextInt(UNKNOWN_ID));
		}
		return fRoute;
	}
	
	AID simAgentAID; // simulation agent id
	
	private int id;
	private Role role;

	private Groups groups;

	private Neighbourhood neighbourhood;
	private RoutingManager routingManager;
	
	

	public Status status;
	

	private Map<Integer, DataInfo> mts;

	private List<String> measuresToSend;

	SendPeriodicMeasures sendMeasuresBehaviour = null;

	public void addRoute(int dest, int repDest, List<Integer> route){
		routingManager.addRoute(dest, repDest, route);
	}
	protected void DEBUG(String message) {
		System.out.println(id + "\t" + message);
	}

	public void fabricateMessage(String msgType) {
		
		// these are not to be changed
		int sender = id;
		int receiver = Frame.BROADCAST;
		
		
		int source = random.nextDouble() < 0.5 ? id : generateFakeId();
		int destination = generateFakeId();
		
		if (msgType.equals("RouteRequest")) {
			
			int requestId = generateFakeId();
			List<Integer> route = random.nextDouble() < 0.5 ? new ArrayList<Integer>() : generateFakeRoute();
			
			MRouteRequest rreq = new MRouteRequest(source, destination,	requestId, route);
			sendFrame(new Frame(sender, receiver, rreq));

		} else if (msgType.equals("RouteReply")) {
			
			int requestId = generateFakeId();
			List<Integer> route = generateFakeRoute();

			MRouteReply rrep = new MRouteReply(source, destination, requestId,	route);
			sendFrame(new Frame(sender, receiver, rrep));
			
		} else if (msgType.equals("RoutedData")) {
			MData data = new MData(source, destination, "fabricated");
			List<Integer> route = generateFakeRoute();
			
			MRoutedData rdata = new MRoutedData(source, destination, data,	route);
			sendFrame(new Frame(sender, Frame.BROADCAST, rdata));
		} else if (msgType.equals("Data")){
			
			MData data = new MData(source, destination, "fabricated"); 
			sendFrame(new Frame(sender, receiver, data));
		}
	}


	// AUXILIARY METHODS
	

	public void generateMeasuresToSend(int num) {
		for (int i = 0; i < num; i++)
			getMeasuresToSend().add(id + "-measure-" + (i + 1));
	}

	public int generateRREQ(int destination) {
		return routingManager.generateRREQ(destination);
	}

	public int getId() {
		return id;
	}

	public int getLinkToRepresentative(int rep){
		return neighbourhood.getLinkToRep(rep);
	}

	public List<String> getMeasuresToSend() {
		return measuresToSend;
	}


	public int getRepresentative(){
		return neighbourhood.getRepresentative();
	}

	public Role getRole() {
		return role;
	}

	public Groups getGroups(){
		return groups;
	}
	
	public RoutingInfo getRoutingInfo(int dest){
		return routingManager.getRoutingInfo(dest);
	}
	
	public SendPeriodicMeasures getSendMeasuresBehaviour() {
		return sendMeasuresBehaviour;
	}
	public boolean hasNeighbour(int dest){
		return neighbourhood.contains(dest);
	}

	public boolean hasRoute(int dest){
		return routingManager.haveRoute(dest);
	}
	
	public boolean hasProcessed(MRouteRequest rreq){
		return routingManager.wasProcessed(rreq);
	}
	
	public void process(MRouteRequest rreq){
		routingManager.process(rreq);
	}
	
	public void rememberData(int reqId, DataInfo dataInfo) {
		mts.put(reqId, dataInfo);		
	}
	
	public DataInfo retrieveData(int reqId){
		return mts.get(reqId);
	}
	
	public void sendFrame(Frame frame){
		ACLMessage aclMessage = new ACLMessage(ACLMessage.PROPAGATE);
		aclMessage.addReceiver(simAgentAID);
		if (frame != null) {
			try {
				aclMessage.setContentObject(frame);
				send(aclMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// DEBUG("NOT SENDING FRAME");
		}
	}
	
	public void sendNotification(Event event) {
		ACLMessage notification = new ACLMessage(ACLMessage.CONFIRM);
		notification.addReceiver(simAgentAID);
		try {
			notification.setContentObject(event);
			addBehaviour(new AchieveREInitiator(this, notification));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setMeasuresToSend(List<String> measuresToSend) {
		this.measuresToSend = measuresToSend;
	}
	
	public void setSendMeasuresBehaviour(SendPeriodicMeasures sendMeasuresBehaviour) {
		this.sendMeasuresBehaviour = sendMeasuresBehaviour;
	}

	@Override
	protected void setup() {

		this.id = Integer.parseInt(getLocalName());
		this.routingManager = new RoutingManager();
		this.setMeasuresToSend(new ArrayList<String>());
		this.mts = new HashMap<Integer, DataInfo>();

		Object[] args = getArguments();

		role = Role.valueOf((String) args[0]);
		groups = (Groups) (args[1]);
		neighbourhood = (Neighbourhood) (args[2]);
		simAgentAID = new AID((String) args[3], AID.ISLOCALNAME);

		status = new Status(this);
		//status.authorization = true;
		
		addBehaviour(new MainLoop(this));
		
	}
	
	@Override
	protected void takeDown() {
		super.takeDown();
	}
	public Role getNeighbourRole(int relay) {
		return neighbourhood.getRole(relay);
	}
	
	public boolean usesAuthorization(){
		return status.authorization;
	}
}