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
import sim.events.CorruptedRouteEvent;
import sim.events.ReceivedFrameEvent;
import sim.events.ReceivedMeasureEvent;
import sim.events.UnauthorizedMessageEvent;

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

	Sensor agent;
	Frame frame;

	Message message;
	
	int fSender;
	int fReceiver;

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

		if (agent.status.isPromiscuousMode())
			handleMessage();
		else if (intendedReceiver())
			handleMessage();

	}

	private boolean intendedReceiver() {
		return frame.getReceiver() == agent.getId()
				|| frame.getReceiver() == Frame.BROADCAST
				|| ((agent.getRole() == Role.Representative) && (frame.getReceiver() == Frame.BROADCAST_REPRESENTATIVE))
				|| ((agent.getRole() == Role.Link) && (frame.getReceiver() == Frame.BROADCAST_LINK));
	}

	private void handleMessage() {
		if(agent.status.isAuthorization())
			if (!authorizeMessage()){
				agent.sendNotification(new UnauthorizedMessageEvent(agent.getId(), 
						message.getClass().getName(), mSource, fSender));
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
			
			// For representatives:
			if (agent.getRole() == Role.Representative) {
				// If I am the destination, or the representative of the
				// destination
				if (agent.getId() == destination || agent.hasNeighbour(destination)) {

					MRouteReply rrep = new MRouteReply(agent.getId(), rreq);

					if(agent.status.mustModify(rreq)){
						Message modified = modifyMessage(rreq);
						agent.sendFrame(wrapMessage(modified));
					} else if (!agent.status.drop())
						agent.sendFrame(wrapMessage(rrep)); // ... send a route reply

				} else { // If I don't know the destination

					// put my id on the route and forward the request
					List<Integer> route = rreq.getRoute();
					route.add(agent.getId());
					rreq.setRoute(route);
					
					if(agent.status.mustModify(rreq)){
						Message modified = modifyMessage(rreq);
						agent.sendFrame(wrapMessage(modified));
						
					} else 	if (!agent.status.drop())
						agent.sendFrame(wrapMessage(rreq)); 
				}
			} else { // For Links:
				if(agent.status.mustModify(rreq)){
					Message modified = modifyMessage(rreq);
					agent.sendFrame(wrapMessage(modified));
				} else if (!agent.status.drop())
					agent.sendFrame(wrapMessage(rreq)); // just forward the request
			}

			agent.process(rreq);// remember received rreq

		} else {
			// ignore already processed route requests
		}
	}

	private void handleMessageRouteReply(MRouteReply rrep) {

		int destination = rrep.getDestination();

		if (agent.getId() == destination) { // I am the one who initiated the request

			// DEBUG("Received ROUTE REPLY: " + rrep);
			if (agent.getRole() == Role.Representative) {

				// retrieve the data corresponding to the reply that just arrived
				DataInfo dataInfo = agent.retrieveData(rrep.getRequestId());

				if (dataInfo == null) // no data needs to be sent back
					return;

				MData mdata = dataInfo.getMdata();
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
				if (!dataInfo.isWasSent()) {
					// Send the routed data, back to the source of the reply
					// (representative of the destination)
					MRoutedData rdata = new MRoutedData(agent.getId(), rrep.getSource(), mdata, route);

					if(agent.status.mustModify(rrep)){
						Message modified = modifyMessage(rrep);
						agent.sendFrame(wrapMessage(modified));
					} else if (!agent.status.drop()) {
						agent.sendFrame(wrapMessage(rdata));
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
			if(agent.status.mustModify(rrep)){
				Message modified = modifyMessage(rrep);
				agent.sendFrame(wrapMessage(modified));
			} else if (!agent.status.drop())
				agent.sendFrame(wrapMessage(rrep)); // just forward the request
		}
	}

	private void handleMessageRoutedData(MRoutedData rdata) {

		// this is the id of the representative of the destination
		int repDest = rdata.getDestination();
		
		MData data = rdata.getData();
		
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
					return;
				} else {
					if (agent.hasNeighbour(dataDest)) {
						if(agent.status.mustModify(rdata)){
							Message modified = modifyMessage(rdata);
							agent.sendFrame(wrapMessage(modified));
						} else if (!agent.status.drop())
							agent.sendFrame(wrapMessage(data)); // deliver the data to its actual destination
					} else {
						// there must be a mistake
					}
				}
			} else { // ... destination is not a representative
				// there must be a mistake: the destination of routed data may
				// only be a Representative
			}
		} else { // The routed data has not reached its destination yet
			if(agent.status.mustModify(rdata)){
				Message modified = modifyMessage(rdata);
				agent.sendFrame(wrapMessage(modified));				
			} else if (!agent.status.drop())
				agent.sendFrame(wrapMessage(rdata)); // just forward the message
		}

	}

	private void handleMessageData(MData data) {

		// int source = data.getSource();
		int destination = data.getDestination();

		if (agent.getId() == destination) { // I message reached destination, I'm done
			agent.sendNotification(new ReceivedMeasureEvent(agent.getId(), data.getData()));
			return;
		} else {
			if (agent.getRole() == Role.Representative) {

				if (agent.hasNeighbour(destination)) {
					// deliver message...				
					if(agent.status.mustModify(data)){
						Message modified = modifyMessage(data);
						agent.sendFrame(wrapMessage(modified));
					} else if (!agent.status.drop())						
						agent.sendFrame(wrapMessage(data));
				} else if (agent.hasRoute(destination)) {

					// If I have a route...
					RoutingInfo rinfo = agent.getRoutingInfo(destination);

					List<Integer> route = rinfo.getRoute();
					int repId = rinfo.getRepDest();

					// send the data on that route
					MRoutedData rdata = new MRoutedData(agent.getId(), repId, data, route);
					
					if(agent.status.mustModify(data)){
						Message modified = modifyMessage(data);
						agent.sendFrame(wrapMessage(modified));
					}else if (!agent.status.drop())
						agent.sendFrame(wrapMessage(rdata));

					// no need for notification here: it has been sent from the
					// "SendPeriodicMeasures" behaviour

					// DEBUG("Have route...sending ROUTED DATA " + rdata);

				} else { // If I don't have a route
					int reqId = agent.generateRREQ(data.getDestination());
					MRouteRequest rreq = new MRouteRequest(agent.getId(), destination,	reqId, new ArrayList<Integer>());

					agent.process(rreq); // remember not to process again
					agent.rememberData(rreq.getRequestId(), new DataInfo(data)); // remember what message to send
					if(agent.status.mustModify(data)){
						Message modified = modifyMessage(data);
						agent.sendFrame(wrapMessage(modified)); 
					} else 	if (!agent.status.drop())
						agent.sendFrame(wrapMessage(rreq));// search for route
					// no need for notification here.
					// DEBUG("No route...sending ROUTE REQUEST " + rreq);
				}
			} else {
				// there must be a mistake, if I am not the destination nor a representative
			}
		}
	}
	
	
	// AUXILIARY METHODS
	
	/**
	 * 
	 * @param msg
	 * @param frameSender
	 * @return true if the message complies with the protocol, false if
	 */
	private boolean authorizeMessage() {
		boolean ok = true;
		Role sourceRole = agent.getNeighbourRole(mSource);
		Role senderRole = agent.getNeighbourRole(fSender);
		
		if (message instanceof MData) {
			// Simple agents can only be the destination of Data; 
			// the source of Data is either a neighbour or
			// the source is unknown and the sender is a Representative neighbour
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
	
	private Message modifyMessage(Message message) {
		if(message.getClass().equals(MRouteRequest.class)){
			MRouteRequest rreq = (MRouteRequest) message;
			rreq.setDestination(Sensor.generateFakeId());
			return rreq;
		} else if (message.getClass().equals(MRouteReply.class)){
			MRouteReply rrep = (MRouteReply) message;
			rrep.setSource(Sensor.generateFakeId());
			return rrep;
		} else if (message.getClass().equals(MRoutedData.class)){
			MRoutedData rdata = (MRoutedData) message;
			rdata.setDestination(Sensor.generateFakeId());
			return rdata;
		} else if (message.getClass().equals(MData.class)){
			MData mdata = (MData) message;
			mdata.setDestination(Sensor.generateFakeId());
			return mdata;
		}
		return message;		
	}


	/**
	 * Builds a frame, according to the type of message.
	 * 
	 * @param message
	 * @param frameSender
	 *            the id of the agent from which the frame containing the
	 *            message came. Used only by RREP and RData messages, to compute
	 *            the receiver field of the frame.
	 * @return the frame to send
	 */
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
}
