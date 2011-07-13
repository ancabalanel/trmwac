/**
 * 
 */
package mwac.msgs;

import java.util.List;

/**
 * @author Anca
 * 
 */
@SuppressWarnings("serial")
public class MRouteReply extends Message {

	/** The id of the corresponding route request */
	int requestId;
	/** The route to be confirmed: not to be changed */
	List<Integer> route;

	public MRouteReply(int source, MRouteRequest rreq) {
		super(source, rreq.getSource());
		this.requestId = rreq.getRequestId();
		this.route = rreq.getRoute();
	}
	
	/**
	 * Used by message fabricators
	 * @param source
	 * @param destination
	 * @param requestId
	 * @param route
	 */
	public MRouteReply(int source, int destination, int requestId, List<Integer> route){
		super(source, destination);
		this.requestId = requestId;
		this.route = route;
	}
	
	public List<Integer> getRoute() {
		return route;
	}

	public void setRoute(List<Integer> route) {
		this.route = route;
	}

	public int getRequestId() {
		return requestId;
	}

	public int getVolume() {
		return super.getVolume() + 4 + 4 * route.size();
	}
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	@Override
	public String toString() {
		return "MRouteReply [requestId=" + requestId + ", route=" + route
				+ ", source=" + source + ", destination=" + destination + "]";
	}

	

}
