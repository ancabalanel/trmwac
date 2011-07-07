/**
 * 
 */
package mwac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mwac.msgs.MData;
import mwac.msgs.MRouteReply;
import mwac.msgs.MRouteRequest;
import mwac.msgs.MRoutedData;
import mwac.msgs.Message;
import sim.Sensor;
import sim.eval.Parameters;
import sim.events.DistrustNeighbourEvent;

/**
 * @author Anca
 * 
 */
public class TrustManager {

	public static boolean match(Message msg1, Message msg2) {

		if (msg1.getClass() == msg2.getClass()) {
			if (msg1 instanceof MRouteRequest) {
				MRouteRequest msg1r = (MRouteRequest) msg1;
				MRouteRequest msg2r = (MRouteRequest) msg2;
				return msg1r.getSource() == msg2r.getSource()
						&& msg1r.getDestination() == msg2r.getDestination()
						&& msg1r.getRequestId() == msg2r.getRequestId();
			} else if (msg1 instanceof MRouteReply) {
				MRouteReply msg1r = (MRouteReply) msg1;
				MRouteReply msg2r = (MRouteReply) msg2;
				return msg1r.getSource() == msg2r.getSource()
						&& msg1r.getDestination() == msg2r.getDestination()
						&& msg1r.getRequestId() == msg2r.getRequestId();
			} else if (msg1 instanceof MRoutedData) {
				MRoutedData msg1r = (MRoutedData) msg1;
				MRoutedData msg2r = (MRoutedData) msg2;
				return msg1r.getSource() == msg2r.getSource()
						&& msg1r.getDestination() == msg2r.getDestination();
			} else if (msg1 instanceof MData){
				return msg1.getSource() == msg2.getSource()
						&& msg1.getDestination() == msg2.getDestination();
			} else
				return false;			
		} else {
			MRouteRequest msg1r = null;
			MRouteReply msg2r = null;
			MData msgd = null;
			MRoutedData msgr = null;

			if(msg1 instanceof MData){
				msgd = (MData) msg1;
				if (msg2 instanceof MRouteRequest){
					msg1r = (MRouteRequest) msg2;
					return msgd.getDestination() == msg1r.getDestination();
				} else if (msg2 instanceof MRoutedData){
					msgr = (MRoutedData) msg2;
					return msgd.getSource() == msgr.getData().getSource()
							&& msgd.getDestination() == msgr.getData().getDestination();
				} else 
					return false;
			} else if (msg2 instanceof MData){
				msgd = (MData) msg2;
				if (msg1 instanceof MRouteRequest){
					msg1r = (MRouteRequest) msg1;
					return msgd.getDestination() == msg1r.getDestination();
				} else if (msg1 instanceof MRoutedData){
					msgr = (MRoutedData) msg1;
					return msgd.getSource() == msgr.getData().getSource()
							&& msgd.getDestination() == msgr.getData().getDestination();
				} else 
					return false;
			} else if (msg1 instanceof MRouteRequest && msg2 instanceof MRouteReply) {
				msg1r = (MRouteRequest) msg1;
				msg2r = (MRouteReply) msg2;
				return msg1r.getSource() == msg2r.getDestination()
				&& msg1r.getRequestId() == msg2r.getRequestId();
			} else if (msg1 instanceof MRouteReply && msg2 instanceof MRouteRequest) {
				msg1r = (MRouteRequest) msg2;
				msg2r = (MRouteReply) msg1;
				return msg1r.getSource() == msg2r.getDestination()
				&& msg1r.getRequestId() == msg2r.getRequestId();
			} else 
				return false;
		}
	}

	
	
	/** Managed agent */
	Sensor agent;
	
	/** Map<id, crt number of interactions> keeps track of the interaction number with each neighbour */
	Map<Integer,Integer> interactionCounter;
	
	
	Map<Integer,Integer> badInteractionCounter;
	
	/** The monitored nodes and messages */
	List<WatchListEntry> watchList;
	
	
	public TrustManager(Sensor agent) {
		this.agent = agent;
		interactionCounter = new HashMap<Integer,Integer>();
		badInteractionCounter = new HashMap<Integer, Integer>();
		watchList = new ArrayList<WatchListEntry>();
	}
	
	
	public void add(WatchListEntry we){
		watchList.add(we);
	}
	
	public void decreaseTrust(int id, float amount) {
		
	}
	
	public WatchListEntry getWatchListEntry(Message msg, int watchedNode){
		for(WatchListEntry we : watchList)
			if(we.getWatchedNode() == watchedNode && match(we.getMessage(), msg)){
				return we;
			}
		return null;
	}

	

	public int incrementBadInteractionCount(int id){
		Integer count = badInteractionCounter.get(id);
		if (count == null) {
			badInteractionCounter.put(id, 1);
			return 1;
		}
		else{
			count = count + 1;
			badInteractionCounter.put(id, count);
			return count;
		}
	}

	public int incrementInteractionCount(int id){
		Integer count = interactionCounter.get(id);
		if (count == null) {
			interactionCounter.put(id, 1);
			return 1;
		}
		else{
			count = count + 1;
			interactionCounter.put(id, count);
			return count;
		}
	}
	
	public void removeAllThatMatch(Message msg){
		List<WatchListEntry> toRemove = new ArrayList<WatchListEntry>();
		for(WatchListEntry we : watchList)
			if(match(we.getMessage(), msg))
				toRemove.add(we);
		
		watchList.removeAll(toRemove);
	}

	public boolean removeFromWatchList(WatchListEntry entry) {
		return watchList.remove(entry);
	}
	
	public void trustRecovery() {
		// TODO 
	}
	
	public void updateTrust(int id){
		int badInteraction = incrementBadInteractionCount(id);
		float amount = badInteraction * Parameters.TRUST_DECREASE_UNIT;
		
		if (agent.getNeighbourTrust(id) > Parameters.TRUST_THRESHOLD) {
			agent.modifyTrust(id, -amount);
			
			if (agent.getNeighbourTrust(id) < Parameters.TRUST_THRESHOLD){
				
				if(agent.getNumTrustedRepresentatives() == 0 && agent.getSendMeasuresBehaviour() != null)
					agent.removeBehaviour(agent.getSendMeasuresBehaviour());
				
				agent.sendNotification(new DistrustNeighbourEvent(agent.getId(), id));
				agent.warnNeighbours(id);				
			}
		} else {
			//agent.DEBUG("Trust for " + id + " already below threshold ");
		}		
		
	}

}
