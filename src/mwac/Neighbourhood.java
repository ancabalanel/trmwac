package mwac;

/**
 * 
 */

import java.util.ArrayList;
import java.util.List;

import sim.eval.Parameters;

/**
 * @author Anca
 * 
 */
public class Neighbourhood {

	int owner;
	List<Neighbour> neighbours;

	public Neighbourhood(int owner, List<Neighbour> nb) {
		this.owner = owner;
		this.neighbours = nb;
	}
	
	public Neighbourhood(int owner) {
		this(owner, new ArrayList<Neighbour>());
	}

	public boolean add(Neighbour neighbour) {

		if (!neighbours.contains(neighbour)) {
			neighbours.add(neighbour);
			return false;
		} else {
			int index = indexOf(neighbour.id);
			if (index >= 0) {
				neighbours.remove(index);
				neighbours.add(index, neighbour);
			}
			return true;
		}
	}

	public boolean contains(int id) {

		for (Neighbour nb : neighbours)	
			if (nb.id == id)
				return true;
		return false;
	}
	
	public int getLinkToRep(int repId) {
		List<Neighbour> allLinksToRep = new ArrayList<Neighbour>();
		for (Neighbour n : neighbours) {
			if (n.getGroups().contains(repId))
				allLinksToRep.add(n);
		}
		Neighbour selected = Parameters.chooseTrustedNeighbour(allLinksToRep);
		if (selected!=null)
			return selected.id;
		else
			return -1;
		
	}
	
	public List<Neighbour> getNeighbours() {
		return neighbours;
	}

	
	public List<Integer> getNeighbours(Role role){
		List<Integer> neighboursWithRole = new ArrayList<Integer>();
		for(Neighbour n : neighbours)
			if(n.role == role)
				neighboursWithRole.add(n.id);
		return neighboursWithRole;
	}

	public float getNeighbourTrust(int id) {
		for(Neighbour n : neighbours){
			if (n.id == id) {
				return n.getTrust();
			}
		}
		return -1.0f; // neighbour does not exist
	}

	public int getNumRepresentatives() {
		int numR = 0;
		for (Neighbour n : neighbours)
			if (n.role == Role.Representative)
				numR++;
		return numR;
	}
	
	public int getNumTrustedRepresentatives(){
		int numR = 0;
		for (Neighbour n : neighbours)
			if (n.role == Role.Representative && n.trust > Parameters.TRUST_THRESHOLD)
				numR++;
		return numR;
	}

	public int getOwner() {
		return owner;
	}
	
	public int getRepresentative() {
		List<Neighbour> reps = new ArrayList<Neighbour>();
		for (Neighbour n : neighbours) {
			if (n.role == Role.Representative && n.trust > Parameters.TRUST_THRESHOLD) 
				reps.add(n);
		}

		Neighbour selected = Parameters.chooseTrustedNeighbour(reps);
		if (selected!=null)
			return selected.id;
		else
			return -1;
	}

	public Role getRole(int id) {
		for (Neighbour n : neighbours)
			if (n.id == id)
				return n.getRole();

		for(Neighbour n : neighbours){
			Groups g = n.groups;
			if(g.contains(id))
				return Role.NNRep;
		}
		return Role.Unknown;
	}

	public int getSize() {
		return neighbours.size();
	}

	private int indexOf(int id) {
		for (int i = 0; i < neighbours.size(); i++)
			if (neighbours.get(i).id == id)
				return i;
		return -1;
	}

	public boolean isEmpty() {
		return neighbours.isEmpty();
	}

	public void modifyTrust(int id, float amount){
		for(Neighbour n : neighbours){
			if (n.id == id) {
				n.setTrust(n.getTrust() + amount);
				break;
			}
		}
	}

	public void setNeighbours(List<Neighbour> neighbours) {
		this.neighbours = neighbours;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}
	
	@Override
	public String toString() {
		return "Neighbours of " + owner + ": " + neighbours;
	}
}
