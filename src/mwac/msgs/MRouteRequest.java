/**
 * 
 */
package mwac.msgs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class MRouteRequest extends Message{

	int requestId;
	List<Integer> route;
	
	/**
	 * @param source
	 * @param destination
	 */
	public MRouteRequest(int source, int destination) {
		this(source, destination, 0, new ArrayList<Integer>());
	}
	
	public MRouteRequest(int source, int destination, int requestId, List<Integer> route){
		super(source, destination);
		this.requestId = requestId;
		this.route = route;
	}


	
	public int getRequestId() {
		return requestId;
	}
	
	public int getVolume(){
		return super.getVolume() + 4 + 4 * route.size();
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public List<Integer> getRoute() {
		return route;
	}

	public void setRoute(List<Integer> route) {
		this.route = route;
	}

	@Override
	public String toString() {
		return "MRouteRequest [requestId=" + requestId + ", route=" + route
				+ ", source=" + source + ", destination=" + destination + "]";
	}


}
