package mwac;

import java.util.List;

public class RoutingTableEntry{
	int repDest;
	List<Integer> route;
	
	public RoutingTableEntry(int repDest, List<Integer> route) {
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