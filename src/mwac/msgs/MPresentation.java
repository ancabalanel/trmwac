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
public class MPresentation extends Message {
	Role role = Role.None;
	Groups groups = new Groups();
	
	/**
	 * 
	 * @param source
	 * @param destination
	 * @param role
	 * @param groups
	 */
	public MPresentation(int source, int destination, Role role, Groups groups){
		super(source, destination);
		this.role = role;
		this.groups = groups;
	}
	
	
	
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Groups getGroups() {
		return groups;
	}

	public void setGroups(Groups groups) {
		this.groups = groups;
	}



	@Override
	public String toString() {
		return "MPresentation [role=" + role + ", source=" + source	+ ", destination=" + destination + "]";
	}
	

}
