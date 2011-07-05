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
					return msgd.getSource() == msg1r.getSource()
							&& msg2.getDestination() == msg1r.getDestination();
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
					return msgd.getSource() == msg1r.getSource()
							&& msg1.getDestination() == msg1r.getDestination();
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

	/** Trust threshold */
	public final static float TRUST_THRESHOLD = 0.6f;

	/** Trust recovery parameter */
	public static final float LAMBDA = 0.1f;
	
	/** Default time (ms) to listen to a message */
	public static final long WATCH_TIME = 1000;

	/** decrease step */
	public static final float DECREASE_STEP = 0.1f;
	
	/** Managed agent */
	Sensor agent;
	
	/** Map<id, crt number of interactions> keeps track of the interaction number with each neighbour */
	Map<Integer,Integer> interactionCounter;
		
	/** The monitored nodes and messages */
	List<WatchListEntry> watchList;
	
	
	public TrustManager(Sensor agent) {
		this.agent = agent;
		interactionCounter = new HashMap<Integer,Integer>();
		watchList = new ArrayList<WatchListEntry>();
	}
	
	
	public void add(WatchListEntry we){
		watchList.add(we);
	}
	public void decreaseTrust(int id, float amount) {
		if (agent.getNeighbourTrust(id) > TRUST_THRESHOLD) {
			agent.modifyTrust(id, -amount);

			if (agent.getNeighbourTrust(id) < TRUST_THRESHOLD){
				agent.sendNotification(new DistrustNeighbourEvent(agent.getId(), id));
				
			}
		}
	}

	public WatchListEntry getWatchedMessage(Message msg, int watchedNode){
		for(WatchListEntry we : watchList)
			if(we.getWatchedNode() == watchedNode && match(we.getMessage(), msg)){
				return we;
			}
		return null;
	}

	public void increaseTrust(int id, float amount) {
		agent.modifyTrust(id, -amount);
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

	public boolean removeFromWatchList(WatchListEntry entry) {
		return watchList.remove(entry);
	}
	
	public void trustRecovery() {
		// TODO 
	}

}
