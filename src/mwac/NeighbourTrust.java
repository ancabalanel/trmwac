/**
 * 
 */
package mwac;

import jade.util.leap.Serializable;

/**
 * @author Anca
 * 
 */
@SuppressWarnings("serial")
public class NeighbourTrust implements Serializable{
	int neighbour;
	float currentTrust;

	public NeighbourTrust(int neighbour, float currentTrust) {
		super();
		this.neighbour = neighbour;
		this.currentTrust = currentTrust;
	}

	public int getNeighbour() {
		return neighbour;
	}

	public void setNeighbour(int neighbour) {
		this.neighbour = neighbour;
	}

	public float getCurrentTrust() {
		return currentTrust;
	}

	public void setCurrentTrust(float currentTrust) {
		this.currentTrust = currentTrust;
	}

	@Override
	public String toString() {
		return "NeighbourTrust [neighbour=" + neighbour + ", currentTrust="
				+ currentTrust + "]";
	}

	
}
