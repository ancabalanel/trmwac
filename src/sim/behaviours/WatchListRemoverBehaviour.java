package sim.behaviours;

import jade.core.behaviours.WakerBehaviour;
import mwac.NeighbourTrust;
import mwac.WatchListEntry;
import sim.Sensor;
import sim.events.TrustDecreasedEvent;

@SuppressWarnings("serial")
public class WatchListRemoverBehaviour extends WakerBehaviour {

	Sensor agent;
	WatchListEntry entry;
	
	
	
	public WatchListRemoverBehaviour(Sensor a, long timeout, WatchListEntry we) {
		super(a, timeout);
		agent = (Sensor) myAgent; 
		this.entry = we;
	}
	
	
	protected void onWake() {
		// This entry was still in the watchlist when its timeout expired: it means that the frame was not forwarded
		if(agent.removeFromWatchList(entry)){
			// agent.DEBUG("REMOVING ... " + entry); // TODO remove
			int watchedNode = entry.getWatchedNode();
			
			agent.updateTrust(watchedNode);
			// agent.decreaseTrust(watchedNode, Parameters.DECREASE_STEP);
			NeighbourTrust nt = new NeighbourTrust(watchedNode, agent.getNeighbourTrust(watchedNode));
			agent.sendNotification(new TrustDecreasedEvent(agent.getId(), nt));
		} else {
			// everything ok
		}
		
		
		agent.removeBehaviour(this);
	}
	
}