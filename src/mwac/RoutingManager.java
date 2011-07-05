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
	
	private static int requestId = 0;
	
	private Map<Integer,Integer> sentRREQ;
	
	private List<ProcessedRREQ> processedRREQ;
	private Map<Integer, RoutingTableEntry> routingTable;
	
	
	public RoutingManager(){
		sentRREQ = new HashMap<Integer, Integer>();
		processedRREQ = new ArrayList<ProcessedRREQ>();
		routingTable = new HashMap<Integer, RoutingTableEntry>();
	}
	
	public void addRoute(int dest, int repDest, List<Integer> route){
		routingTable.put(dest, new RoutingTableEntry(repDest, route));
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
		
		routingTable.put(dest, new RoutingTableEntry(rrep.getSource(), route));
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
	
	public RoutingTableEntry getRoutingInfo(int dest){
		return routingTable.get(dest);
	}
	
	public Map<Integer, RoutingTableEntry> getRoutingTable() {
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
