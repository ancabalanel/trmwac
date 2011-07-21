package sim.behaviours;

import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;

import mwac.Role;
import mwac.WaitingDataInfo;
import mwac.WatchListEntry;
import mwac.msgs.Frame;
import mwac.msgs.MData;
import mwac.msgs.MRouteRequest;
import mwac.msgs.MRoutedData;
import sim.Sensor;
import sim.eval.Parameters;
import sim.events.SentMeasureEvent;

/**
 * 
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class SendPeriodicMeasures extends TickerBehaviour {

	int destination;
	Sensor agent;

	public SendPeriodicMeasures(Sensor sensor, long period, int destination) {
		super(sensor, period);
		this.destination = destination;
		agent = (Sensor) myAgent;
	}

	@Override
	protected void onTick() {

		if (!agent.getMeasuresToSend().isEmpty()) {

			String next = agent.getMeasuresToSend().remove(0);
			MData mdata = new MData(agent.getId(), destination, next);

			// If i know the destination, send directly (the role doesn't matter)
			if (agent.hasNeighbour(destination)) {
				agent.sendFrame(new Frame(agent.getId(), mdata.getDestination(), mdata));
				agent.sendNotification(new SentMeasureEvent(agent.getId(), mdata.getData()));
			} else {
				// For representatives:
				if (agent.getRole() == Role.Representative) {
				
					// if i have a route
					if (agent.hasRoute(destination)) { 
						// agent.DEBUG("I have route"); // this should not appear...
						List<Integer> route = agent.getRoutingInfo(destination).getRoute();
						int repDestination = agent.getRoutingInfo(destination).getRepDest();
						MRoutedData rdata = new MRoutedData(agent.getId(), repDestination, mdata, route);

						// ... send the data on that route, and notify the sim agent
						
						int nextHop = Frame.BROADCAST_LINK;
						if(route.isEmpty())
							nextHop = rdata.getDestination();
						else
							nextHop = route.get(0);

						int fReceiver = agent.getLinkToRepresentative(nextHop);
						
						agent.sendFrame(new Frame(agent.getId(), fReceiver, rdata));
						
						if(agent.isUsingTrust()){
							int interactionNumber = agent.addToWatchList(rdata, fReceiver);
							WatchListEntry we = new WatchListEntry(fReceiver, interactionNumber, rdata);							
							agent.addBehaviour(new WatchListRemoverBehaviour(agent, Parameters.WATCH_TIME, we));			
						}
						agent.sendNotification(new SentMeasureEvent(agent.getId(), mdata.getData()));

					} else { // if i don't have a route ...

						int reqId = agent.generateRREQ(destination);
						MRouteRequest rreq = new MRouteRequest(agent.getId(), destination, reqId, new ArrayList<Integer>());

						// remember data to send, for when i receive the route reply
						agent.rememberData(reqId, new WaitingDataInfo(mdata));					 
											
						// remember request id
						agent.process(rreq);						
						
						agent.sendFrame(new Frame(agent.getId(), Frame.BROADCAST_LINK, rreq));
						agent.sendNotification(new SentMeasureEvent(agent.getId(), mdata.getData())); // notify sim agent
					}
				} else { // For non representatives:

					int representative = agent.getRepresentative();

					if(agent.isUsingTrust()){
						int interactionNumber = agent.addToWatchList(mdata, representative);
						WatchListEntry we = new WatchListEntry(representative, interactionNumber, mdata);						
						agent.addBehaviour(new WatchListRemoverBehaviour(agent, Parameters.WATCH_TIME, we));			
					}
					
					// ... send data to my representative agent 
					agent.sendFrame(new Frame(agent.getId(), representative, mdata));
					agent.sendNotification(new SentMeasureEvent(agent.getId(), mdata.getData()));
				}
			}

		} else {
			// I have no more measures to send
			agent.removeBehaviour(this);
		}
	}
}