package mwac;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Anca
 * 
 */
public class Neighbourhood {

	int owner;
	List<Neighbour> neighbours = new ArrayList<Neighbour>();

	public Neighbourhood(int owner) {
		this.owner = owner;
	}

	public Neighbourhood(int owner, List<Neighbour> nb) {
		this(owner);
		this.neighbours = nb;
	}

	/**
	 * @param neighbour
	 * @return true if something changed false if no new neighbour has been
	 *         added, or old neighbour modified
	 */
	public boolean add(Neighbour neighbour) {

		if (!neighbours.contains(neighbour)) {
			neighbours.add(neighbour);
			return false;
		} else {
			int index = indexOfID(neighbour.id);
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
		List<Integer> allLinksToRep = new ArrayList<Integer>();
		for (Neighbour n : neighbours) {
			if (n.getGroups().contains(repId))
				allLinksToRep.add(n.id);
		}
		
		Collections.shuffle(allLinksToRep);
		return allLinksToRep.get(0);
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

	public int getNumRepresentatives() {
		int numR = 0;
		for (Neighbour n : neighbours)
			if (n.role == Role.Representative)
				numR++;
		return numR;
	}

	public int getOwner() {
		return owner;
	}

	/**
	 * Used by Simple and Link agents to retrieve a representative agent in
	 * their group
	 * 
	 * @return the representative agent, for a simple agent, or a random
	 *         representative in the case of Links
	 */
	public int getRepresentative() {
		List<Integer> reps = new ArrayList<Integer>();
		for (Neighbour n : neighbours) {
			if (n.role == Role.Representative)
				reps.add(n.id);
		}

		Collections.shuffle(reps);
		return reps.get(0);
		//return reps.get(reps.size()-1);	
	}
	
	public Role getRole(int id) {
		for (Neighbour n : neighbours)
			if (n.id == id)
				return n.getRole();
		return Role.Unknown;
	}

	public int getSize() {
		return neighbours.size();
	}

	private int indexOfID(int id) {
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

	public float getNeighbourTrust(int id) {
		for(Neighbour n : neighbours){
			if (n.id == id) {
				return n.getTrust();
			}
		}
		return -1.0f; // neighbour does not exist
	}
}
