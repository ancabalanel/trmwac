/**
 * 
 */
package sim.scn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mwac.Neighbour;
import mwac.Neighbourhood;
import mwac.Role;

/**
 * @author Anca
 * 
 */
public class Organization {

	boolean stable;

	List<NodeDescription> nodeDescriptions;
	Map<Integer, List<Integer>> nodeNeighbours;
	Map<Integer, Neighbourhood> nodeNeighbourhood;

	public Organization() {
		stable = false;
		nodeDescriptions = new ArrayList<NodeDescription>();
		nodeNeighbours = new HashMap<Integer, List<Integer>>();
		nodeNeighbourhood = new HashMap<Integer, Neighbourhood>();
	}

	public void add(NodeDescription node) {
		nodeDescriptions.add(node);
		computeNeighbours();
	}

	private void computeNeighbours() {
		for (NodeDescription n : nodeDescriptions) {

			// neighbour ids
			List<Integer> nnList = inRangeOf(n.id);
			
			// neighbour id, trust, role, groups
			List <Neighbour> nbList = new ArrayList<Neighbour>();			
			for (Integer nb : nnList) {
				NodeDescription ndesc = get(nb);
				if(nb!=null)
					nbList.add(new Neighbour(nb, 1.0f, ndesc.role, ndesc.groups));
			}
			Neighbourhood nbhd = new Neighbourhood(n.id, nbList);
			
			// neighbour ids
			nodeNeighbours.put(n.id, nnList);		
			// neighbour id, trust, role, groups
			nodeNeighbourhood.put(n.id, nbhd);
		}
	}

	public NodeDescription get(int id) {
		for (NodeDescription nd : nodeDescriptions)
			if (nd.id == id)
				return nd;
		return null;
	}

	public List<Integer> getLinkIds(int ws){
		List<Integer> links = new ArrayList<Integer>();
		
		for(NodeDescription nd : nodeDescriptions)
			if(nd.role == Role.Link && nd.id != ws)
				links.add(nd.id);
		
		return links;
	}

	public List<Integer> getNeighbours(int id) {
		return nodeNeighbours.get(id);
	}
	

	public List<NodeDescription> getNodeDescriptions() {
		return nodeDescriptions;
	}

	public List<Integer> getNodeIds() {
		List<Integer> nodes = new ArrayList<Integer>();
		nodes.addAll(nodeNeighbours.keySet());
		return nodes;
	}
	
	public Map<Integer, Neighbourhood> getNodeNeighbourhood() {
		return nodeNeighbourhood;
	}
	
	public Map<Integer, List<Integer>> getNodeNeighbours() {
		return nodeNeighbours;
	}

	public List<Integer> getRepresentativeIds(int ws){
		List<Integer> repr = new ArrayList<Integer>();
		
		for(NodeDescription nd : nodeDescriptions)
			if(nd.role == Role.Representative && nd.id != ws)
				repr.add(nd.id);
		
		return repr;
	}

	public List<Integer> getSimpleIds(int ws){
		List<Integer> simple = new ArrayList<Integer>();
		
		for(NodeDescription nd : nodeDescriptions)
			if(nd.role == Role.Simple && nd.id != ws)
				simple.add(nd.id);
		
		return simple;
	}
	
	
	public List<Integer> getIdsWithRole(Role role, int ws, boolean excludeMalicious){
		List<Integer> result = new ArrayList<Integer>();
		
		for(NodeDescription nd : nodeDescriptions){
			if(nd.role == role && nd.id != ws)
				if(excludeMalicious){
					if(!nd.malicious) 
						result.add(nd.id);
				}
				else
					result.add(nd.id);
		}
		
		return result;
	}

	private List<Integer> inRangeOf(int id) {
		List<Integer> neighbourIds = new ArrayList<Integer>();
		NodeDescription nd = get(id);
		if (nd != null)
			for (NodeDescription nb : nodeDescriptions)
				if (nb.id != id)
					if (NodeDescription.distance(nd, nb) <= nd.range)
						neighbourIds.add(nb.id);
		return neighbourIds;
	}

	public boolean isStable() {
		return stable;
	}

	public void setNodeDescriptions(List<NodeDescription> nodeDescriptions) {
		this.nodeDescriptions = nodeDescriptions;
		computeNeighbours();
	}
	
	public void setNodeNeighbourhood(Map<Integer, Neighbourhood> nodeNeighbourhood) {
		this.nodeNeighbourhood = nodeNeighbourhood;
	}

	public void setNodeNeighbours(Map<Integer, List<Integer>> nodeNeighbours) {
		this.nodeNeighbours = nodeNeighbours;
	}

	public void setStable(boolean stable) {
		this.stable = stable;
	}
	
	public void setMalicious(int nodeId, boolean malicious){
		for(NodeDescription nd : nodeDescriptions)
			if(nd.id == nodeId)
				nd.setMalicious(malicious);
	}
	
	public boolean isMalicious(int nodeId){
		for(NodeDescription nd : nodeDescriptions)
			if(nd.id == nodeId)
				return nd.malicious;
		return false;
	}

	@Override
	public String toString() {
		return "Organization [stable=" + stable + ", nodeDescriptions="
				+ nodeDescriptions + ", nodeNeighbours=" + nodeNeighbours + "]";
	}

	
}
