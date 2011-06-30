/**
 * 
 */
package mwac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mwac.msgs.MRouteReply;
import mwac.msgs.MRouteRequest;

/**
 * @author Anca
 *
 */
public class RoutingManager {
	
	class ProcessedRREQ{
		int source;
		int requestId;
		
		ProcessedRREQ(int source, int requestId) {
			super();
			this.source = source;
			this.requestId = requestId;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProcessedRREQ other = (ProcessedRREQ) obj;
			if (requestId != other.requestId)
				return false;
			if (source != other.source)
				return false;
			return true;
		}


	}
	
	public class RoutingInfo{
		int repDest;
		List<Integer> route;
		
		public RoutingInfo(int repDest, List<Integer> route) {
			super();
			this.repDest = repDest;
			this.route = route;
		}

		public int getRepDest() {
			return repDest;
		}

		public List<Integer> getRoute() {
			return route;
		}

		@Override
		public String toString() {
			return "RoutingInfo [repDest=" + repDest + ", route=" + route + "]";
		}
		
		
		
	}
	public static int requestId = 0;
	
	Map<Integer,Integer> sentRREQ;
	
	List<ProcessedRREQ> processedRREQ;
	Map<Integer, RoutingInfo> routingTable;
	
	
	public RoutingManager(){
		sentRREQ = new HashMap<Integer, Integer>();
		processedRREQ = new ArrayList<ProcessedRREQ>();
		routingTable = new HashMap<Integer, RoutingManager.RoutingInfo>();
	}
	
	public void addRoute(int dest, int repDest, List<Integer> route){
		routingTable.put(dest, new RoutingInfo(repDest, route));
	}
	
	public void addRoute(int dest, MRouteReply rrep, int id){
		List<Integer> route = rrep.getRoute();
		List<Integer> newRoute = new ArrayList<Integer>();
		
		if(route.contains(id)){
			for(Integer i : route)
				if(i.intValue() != id)
					newRoute.add(i);
				else 
					break;
			route = newRoute;
		}
		
		routingTable.put(dest, new RoutingInfo(rrep.getSource(), route));
	}
	
	public int generateRREQ(int destination){
		requestId++;
		sentRREQ.put(requestId, destination);
		return requestId;
	}
	
	public int getDestination(int requestId){
		Integer dest = sentRREQ.get(requestId);
		if(dest == null)
			return -1;
		return dest;		 
	}
	
	public RoutingInfo getRoutingInfo(int dest){
		return routingTable.get(dest);
	}
	
	public Map<Integer, RoutingInfo> getRoutingTable() {
		return routingTable;
	} 
	
	public boolean haveRoute(int dest){
		return routingTable.containsKey(dest);
	}
	
	public void process(MRouteRequest rreq){
		processedRREQ.add(new ProcessedRREQ(rreq.getSource(), rreq.getRequestId()));
	}

	public boolean wasProcessed(MRouteRequest rreq){
		return processedRREQ.contains(new ProcessedRREQ(rreq.getSource(), rreq.getRequestId()));
	}

	
}
