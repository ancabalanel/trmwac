/**
 * 
 */
package mwac.msgs;

import mwac.Groups;
import mwac.Role;

/**
 * @author Anca
 * 
 */
@SuppressWarnings("serial")
public class MWhoAreMyNeighbours extends MPresentation{
	
	/**
	 * 
	 * @param source
	 * @param destination
	 * @param role
	 * @param groups
	 */
	public MWhoAreMyNeighbours(int source, int destination, Role role, Groups groups) {
		super(source, destination, role, groups);	
	}
	

	@Override
	public String toString() {
		return "WhoAreMyNeighbours [role=" + role + ", source=" + source	+ ", destination=" + destination + "]";
	}
}
