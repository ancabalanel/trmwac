/**
 * 
 */
package mwac;

import java.util.ArrayList;
import java.util.List;

import mwac.msgs.Frame;
import mwac.msgs.MData;
import mwac.msgs.MIntroduction;
import mwac.msgs.MPresentation;
import mwac.msgs.MRouteReply;
import mwac.msgs.MRouteRequest;
import mwac.msgs.MRoutedData;
import mwac.msgs.Message;
import sim.Sensor;
import sim.behaviours.WatchListRemoverBehaviour;
import sim.eval.Parameters;
import sim.events.CorruptedRouteEvent;
import sim.events.ReceivedFrameEvent;
import sim.events.ReceivedMeasureEvent;
import sim.events.UnauthorizedMessageEvent;

// TODO ** = revise

/**
 * The <code>FrameHandler</code> class is used to extract a message from frames
 * and take actions according to the message type. The <code>handle*</code>
 * methods specify what the agent does when it receives a message of that type.
 * 
 * 
 * @author Anca
 * 
 */
public class FrameHandler {

	public static Message modifyMessage(Message msg) {
		if(msg.getClass().equals(MRouteRequest.class)){
			MRouteRequest rreq = (MRouteRequest) msg;
			rreq.setDestination(Parameters.generateFakeId());
			return rreq;
		} else if (msg.getClass().equals(MRouteReply.class)){
			MRouteReply rrep = (MRouteReply) msg;
			rrep.setSource(Parameters.generateFakeId());
			return rrep;
		} else if (msg.getClass().equals(MRoutedData.class)){
			MRoutedData rdata = (MRoutedData) msg;
			rdata.setDestination(Parameters.generateFakeId());
			return rdata;
		} else if (msg.getClass().equals(MData.class)){
			MData mdata = (MData) msg;
			mdata.setDestination(Parameters.generateFakeId());
			return mdata;
		}
		return msg;		
	}
	
	Sensor agent;

	Frame frame;	
	int fSender;
	int fReceiver;
	
	Message message;
	int mSource;	
	int mDestination;

	public FrameHandler(Sensor sensor, Frame f) {
		this.agent = sensor;
		this.frame = f;

		message = frame.getMessage();
		
		fSender = frame.getSender();
		fReceiver = frame.getReceiver();
		
		mSource = message.getSource();
		mDestination = message.getDestination();

		agent.sendNotification(new ReceivedFrameEvent(agent.getId()));

		if (agent.useTrust()) {
			matchMessage();
			
			if (intendedReceiver())
				handleMessage();
		} else if (intendedReceiver())
			handleMessage();

	}

	private boolean authorizeMessage() {
		boolean ok = true;
		Role sourceRole = agent.getNeighbourRole(mSource);
		Role senderRole = agent.getNeighbourRole(fSender);
		
		if (message instanceof MData) {
			if (agent.getRole() == Role.Simple || agent.getRole() == Role.Link) {
				ok = (message.getDestination() == agent.getId() && (message.getSource() == fSender || (sourceRole == Role.Unknown && senderRole == Role.Representative)));
			} else if (agent.getRole() == Role.Representative) { 
				ok = (message.getSource() == fSender);
			}
		} else if (message instanceof MRoutedData) {
			if(agent.getRole() == Role.Simple)
				ok = false;
			else if (agent.getRole() == Role.Link) {
				ok = !((message.getDestination() == agent.getId())
						|| (senderRole == Role.Simple) || (senderRole == Role.Link));
			} else if (agent.getRole() == Role.Representative) {
				ok = !(senderRole == Role.Simple || (senderRole == Role.Link && sourceRole != Role.Unknown));
			}
		} else if (message instanceof MRouteRequest || message instanceof MRouteReply) {
			if(agent.getRole() == Role.Simple){
				ok = false;
			} else if (agent.getRole() == Role.Link){
				ok = (senderRole == Role.Representative) && (message.getSource() == fSender || sourceRole == Role.Unknown || sourceRole == Role.Representative);
			} else if (agent.getRole() == Role.Representative){
				ok = (senderRole == Role.Link) && (sourceRole == Role.Unknown);
			}
		} 
	
		return ok;
	}

	private int getRelayRData(MRoutedData rdata) {
		List<Integer> route = rdata.getRoute();
		int relay;

		if (route.isEmpty()) {
			relay = rdata.getDestination();
		} else {
			int index;

			if (agent.getRole() == Role.Representative)
				index = route.indexOf(agent.getId());
			else
				index = route.indexOf(fSender);
			if (index == -1)
				relay = route.get(0);
			else if (index < route.size() - 1)
				relay = route.get(index + 1);
			else
				relay = rdata.getDestination();
		}

		if (agent.getRole() == Role.Representative){ 
			try {
				relay = agent.getLinkToRepresentative(relay);
			} catch (Exception e) {
				if(agent.usesAuthorization())
					agent.sendNotification(new UnauthorizedMessageEvent(agent.getId(), rdata.getClass().getName(), rdata.getSource(), fSender));
				else 
					agent.sendNotification(new CorruptedRouteEvent(agent.getId(), route, fSender));
			}
		} else if (agent.getRole() == Role.Link) {
			Role relayRole = agent.getNeighbourRole(relay);
			if(relayRole != Role.Representative) {
				if(agent.usesAuthorization())
					agent.sendNotification(new UnauthorizedMessageEvent(agent.getId(), rdata.getClass().getName(), rdata.getSource(),fSender));
				else 
					agent.sendNotification(new CorruptedRouteEvent(agent.getId(), route, fSender));				
				return -1;
			}			
		}
		return relay;
	}

	private int getRelayRREP(MRouteReply rrep) {
		List<Integer> route = rrep.getRoute();
		int relay;

		if (route.isEmpty())
			relay = rrep.getDestination();
		else {
			int index;
			if (agent.getRole() == Role.Representative)
				index = route.indexOf(agent.getId());
			else
				index = route.indexOf(fSender);
			
			

			if (index == -1) {
				relay = route.get(route.size() - 1);
			} else if (index > 0)
				relay = route.get(index - 1);
			else
				relay = rrep.getDestination();
		}

		if (agent.getRole() == Role.Representative){ 
			try {
				relay = agent.getLinkToRepresentative(relay);
			} catch (Exception e) {				
				if(agent.usesAuthorization())
					agent.sendNotification(new UnauthorizedMessageEvent(agent.getId(), rrep.getClass().getName(), rrep.getSource(), fSender));
				else
					agent.sendNotification(new CorruptedRouteEvent(agent.getId(), route, fSender));
			}
		} else if (agent.getRole() == Role.Link){
			Role relayRole = agent.getNeighbourRole(relay);
			if(relayRole != Role.Representative) {
				if(agent.usesAuthorization())
					agent.sendNotification(new UnauthorizedMessageEvent(agent.getId(), rrep.getClass().getCanonicalName(), rrep.getSource(),fSender));
				else
					agent.sendNotification(new CorruptedRouteEvent(agent.getId(), route, fSender));
				return -1;
			}			
		}

		return relay;
	}

	private void handleMessage() {
		if(agent.useAuthorization())
			if (!authorizeMessage()){
				agent.sendNotification(new UnauthorizedMessageEvent(agent.getId(), message.getClass().getName(), mSource, fSender));
				return;
			}
		
		
		if (message instanceof MRouteRequest) {
			handleMessageRouteRequest((MRouteRequest) message);
			return;
		}
		if (message instanceof MRouteReply) {
			handleMessageRouteReply((MRouteReply) message);
			return;
		}
		if (message instanceof MRoutedData) {
			handleMessageRoutedData((MRoutedData) message);
		}
		if (message instanceof MData) {
			handleMessageData((MData) message);
		}

	}

	private void handleMessageData(MData data) {

		int destination = data.getDestination();

		if (agent.getId() == destination) { // I message reached destination, I'm done
			agent.sendNotification(new ReceivedMeasureEvent(agent.getId(), data.getData()));
		//	agent.DEBUG("received(2) " + frame);
			return;
		} else {
			if (agent.getRole() == Role.Representative) {

				if (agent.hasNeighbour(destination)) {
					// deliver message...				
					if(agent.mustModify(data)){
						Message modified = modifyMessage(data);
						agent.sendFrame(wrapMessage(modified));
					} else if (!agent.mustDrop())			
						sendAndWatchMessage(data);
						//agent.sendFrame(wrapMessage(data)); 
				} else if (agent.hasRoute(destination)) {				

					// If I have a route...
					RoutingTableEntry rinfo = agent.getRoutingInfo(destination);

					List<Integer> route = rinfo.getRoute();
					int repId = rinfo.getRepDest();

					// send the data on that route
					MRoutedData rdata = new MRoutedData(agent.getId(), repId, data, route);
					
					if(agent.mustModify(data)){
						Message modified = modifyMessage(data);
						agent.sendFrame(wrapMessage(modified));
					}else if (!agent.mustDrop()) 				
						sendAndWatchMessage(rdata);
					
				} else { // If I don't have a route
					int reqId = agent.generateRREQ(data.getDestination());
					MRouteRequest rreq = new MRouteRequest(agent.getId(), destination,	reqId, new ArrayList<Integer>());

					agent.process(rreq); // remember not to process again
					
					agent.rememberData(rreq.getRequestId(), new WaitingDataInfo(data)); // remember what message to send
					/* route requests are not dropped
					if(agent.mustModify(data)){
						Message modified = modifyMessage(data);
						agent.sendFrame(wrapMessage(modified)); 
					} else if (!agent.mustDrop()) */ 
					
					sendAndWatchMessage(rreq); 					
				}
			} else {
				// there must be a mistake, if I am not the destination nor a representative
			}
		}
	}
	
	
	// AUXILIARY METHODS
	
	private void handleMessageRoutedData(MRoutedData rdata) {

		// this is the id of the representative of the destination
		int repDest = rdata.getDestination();
		
		MData data = rdata.getData();
		agent.DEBUG("RECEIVED ROUTED DATA " + frame);
		
		// this is the id of the actual destination
		int dataDest = data.getDestination(); 

		if (agent.getRole() == Role.Simple) {
			// there must be a mistake
			return;
		}

		if (agent.getId() == repDest) { // The routed data has reached its destination (representative)

			if (agent.getRole() == Role.Representative) {
				if (agent.getId() == dataDest) { // data has reached its final destination
										// (which is also its representative)
					agent.sendNotification(new ReceivedMeasureEvent(agent.getId(), data.getData()));
				//	agent.DEBUG("received(1) " + frame);
					return;
				} else {
					if (agent.hasNeighbour(dataDest)) {
						if(agent.mustModify(rdata)){
							Message modified = modifyMessage(rdata);
							agent.sendFrame(wrapMessage(modified));
						} else if (!agent.mustDrop())
							agent.sendFrame(wrapMessage(data)); // deliver the data to its actual destination
					} else {
						// there must be a mistake
					}
				}
			} else { // ... destination is not a representative
				// there must be a mistake: the destination of routed data may only be a Representative
			}
		} else { // The routed data has not reached its destination yet
			if (agent.mustModify(rdata)) {
				Message modified = modifyMessage(rdata);
				agent.sendFrame(wrapMessage(modified));
			} else if (!agent.mustDrop())
				sendAndWatchMessage(rdata); 
		}
	}
	
	private void handleMessageRouteReply(MRouteReply rrep) {

		int destination = rrep.getDestination();
		agent.DEBUG("RECEIVING ROUTE REPLY .... " + frame);

		if (agent.getId() == destination) { // I am the one who initiated the request

			if (agent.getRole() == Role.Representative) {

				// retrieve the data corresponding to the reply that just arrived
				WaitingDataInfo waitingDataInfo = agent.retrieveData(rrep.getRequestId());

				if (waitingDataInfo == null) // no data needs to be sent back
					return;

				MData mdata = waitingDataInfo.getMdata();
				int finalDest = mdata.getDestination(); // final destination

				List<Integer> route = rrep.getRoute(); // new route

				// Update the routing table with the new route
				if (agent.hasRoute(finalDest)) { // If I had a route
					List<Integer> oldRoute = agent.getRoutingInfo(finalDest).getRoute();
					if (route.size() < oldRoute.size()) // ... if the old route was longer, replace it
						agent.addRoute(finalDest, rrep.getSource(), route);
				} else { // If i had no route, add the new route
					agent.addRoute(finalDest, rrep.getSource(), route);
				}

				// If the message was not sent already...
				if (!waitingDataInfo.isWasSent()) {
					
					// Send the routed data, back to the source of the reply (representative of the destination)
					MRoutedData rdata = new MRoutedData(agent.getId(), rrep.getSource(), mdata, route);

					if(agent.mustModify(rrep)){
						Message modified = modifyMessage(rrep);
						agent.sendFrame(wrapMessage(modified));
					} else if (!agent.mustDrop()) 
						sendAndWatchMessage(rdata); 
					waitingDataInfo.setWasSent(true);
				}

			} else {
				// there must be a mistake: only representatives can initiate route requests
			}
		} else { // I am not the initiator of the request
			if(agent.mustModify(rrep)){
				Message modified = modifyMessage(rrep);
				agent.sendFrame(wrapMessage(modified));
			} else if (!agent.mustDrop()) 
				sendAndWatchMessage(rrep); 
		}
	}

	/**
	 * Decisions about forwarding or replying to the request are taken.
	 * 
	 * @param rreq
	 *            the route request message
	 * 
	 */
	private void handleMessageRouteRequest(MRouteRequest rreq) {

		int destination = rreq.getDestination();

		// If I haven't processed this request before...
		if (!agent.hasProcessed(rreq)) {
			// agent.DEBUG("ROUTE REQUEST .... " + frame);
			// remember received rreq
			agent.process(rreq);
			
			// For representatives:
			if (agent.getRole() == Role.Representative) {
				// If I am the destination, or the representative of the destination
				if (agent.getId() == destination || agent.hasNeighbour(destination)) {

					MRouteReply rrep = new MRouteReply(agent.getId(), rreq);

					/*if(agent.mustModify(rreq)){
						Message modified = modifyMessage(rreq);
						agent.sendFrame(wrapMessage(modified));
					} else if (!agent.mustDrop())*/
						sendAndWatchMessage(rrep); 			

				} else { // If I don't know the destination

					// put my id on the route and forward the request
					List<Integer> route = rreq.getRoute();
					route.add(agent.getId());
					rreq.setRoute(route);
					
					/*if(agent.mustModify(rreq)){
						Message modified = modifyMessage(rreq);
						agent.sendFrame(wrapMessage(modified));						
					} else 	if (!agent.mustDrop()) */
						sendAndWatchMessage(rreq);   
				}
			} else { // For Links:
				/* if(agent.mustModify(rreq)){
					Message modified = modifyMessage(rreq);
					agent.sendFrame(wrapMessage(modified));
				} else if (!agent.mustDrop()) */
					sendAndWatchMessage(rreq); 
			}

			

		} else {
			// ignore already processed route requests
		}
	}
	
	private boolean intendedReceiver() {
		return frame.getReceiver() == agent.getId()
				|| frame.getReceiver() == Frame.BROADCAST
				|| ((agent.getRole() == Role.Representative) && (frame.getReceiver() == Frame.BROADCAST_REPRESENTATIVE))
				|| ((agent.getRole() == Role.Link) && (frame.getReceiver() == Frame.BROADCAST_LINK));
	}

	private void matchMessage() {
		
		WatchListEntry we = agent.getWatchedMessageEntry(message, fSender);
	
		if (we != null){
			agent.removeFromWatchList(we);
		}
	}

	public void sendAndWatchMessage(Message message){
		Frame frameToSend = wrapMessage(message);
		
		
		if(agent.useTrust()){
			int watchedNode = frameToSend.getReceiver();
	
			if(watchedNode > 0 && watchedNode != message.getDestination()){
				int interactionNumber = agent.addToWatchList(message, watchedNode);			
				WatchListEntry we = new WatchListEntry(watchedNode, interactionNumber, message);
				agent.addBehaviour(new WatchListRemoverBehaviour(agent, Parameters.WATCH_TIME, we));					
			} /* else if (watchedNode == Frame.BROADCAST_LINK){
				List<Integer> watchedNodes = agent.getNeighbours(Role.Link);
				watchedNodes.remove((Integer)fSender); // watch all nodes besides the one who sent the frame (if existent)
				for(Integer wn : watchedNodes){
					if (wn != message.getDestination()) {
						int interactionNumber = agent.addToWatchList(message, wn);
						WatchListEntry we = new WatchListEntry(wn, interactionNumber, message);
						agent.addBehaviour(new WatchListRemoverBehaviour(agent, Parameters.WATCH_TIME, we));
					}
				}
			} else if (watchedNode == Frame.BROADCAST_REPRESENTATIVE){
				List<Integer> watchedNodes = agent.getNeighbours(Role.Representative);
				watchedNodes.remove((Integer)fSender);
				for(Integer wn : watchedNodes){
					if (wn != message.getDestination()) {
						int interactionNumber = agent.addToWatchList(message, wn);
						WatchListEntry we = new WatchListEntry(wn, interactionNumber, message);
						agent.addBehaviour(new WatchListRemoverBehaviour(agent, Parameters.WATCH_TIME, we));
					}
				}
			} */
		}
		agent.sendFrame(frameToSend); // just forward the request
	}
	
	private Frame wrapMessage(Message msg) {
		Frame frame = new Frame(agent.getId(), Frame.BROADCAST, msg);

		// ...
		if (msg instanceof MIntroduction || msg instanceof MPresentation) {
			frame = new Frame(agent.getId(), Frame.BROADCAST, msg);

			// ROUTE REQUEST
		} else if (msg instanceof MRouteRequest) {
			if (agent.getRole() == Role.Representative) {
				frame = new Frame(agent.getId(), Frame.BROADCAST_LINK, msg);
			} else {
				frame = new Frame(agent.getId(), Frame.BROADCAST_REPRESENTATIVE, msg);
			}

			// ROUTE REPLY
		} else if (msg instanceof MRouteReply) {

			int relay;
			MRouteReply rrep = (MRouteReply) msg;
			
			relay = getRelayRREP(rrep);
			if(relay > 0)
				frame = new Frame(agent.getId(), relay, rrep);
			else frame = null;

			// ROUTED DATA
		} else if (msg instanceof MRoutedData) {
			int relay;
			MRoutedData rdata = (MRoutedData) msg;

			relay = getRelayRData(rdata);
			if(relay > 0)
				frame = new Frame(agent.getId(), relay, rdata);
			else
				frame = null;

			// DATA
		} else if (msg instanceof MData) {
			if (agent.hasNeighbour(msg.getDestination()))
				frame = new Frame(agent.getId(), msg.getDestination(), msg);
			else {
				if (agent.getRole() != Role.Representative) {
						int repId = agent.getRepresentative();
						frame = new Frame(agent.getId(), repId, msg);
				} else {
					// destination unknown
				}
			}
		}
		return frame;
	}
}
