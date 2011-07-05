package sim.behaviours;

import jade.core.behaviours.WakerBehaviour;
import mwac.TrustManager;
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
			int watchedNode = entry.getWatchedNode();
			agent.decreaseTrust(watchedNode, TrustManager.DECREASE_STEP);
			agent.sendNotification(new TrustDecreasedEvent(agent.getId(),watchedNode));
		} else {
			// everything ok
		}
		
		
		agent.removeBehaviour(this);
	}
	
}