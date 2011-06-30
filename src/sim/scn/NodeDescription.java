/**
 * 
 */
package sim.scn;

import mwac.Groups;
import mwac.Role;

/**
 * @author Anca
 * 
 */
public class NodeDescription {

	int id;

	int x, y;

	int range;

	Role role = Role.None;
	Groups groups = new Groups();
	
	boolean malicious;

	public NodeDescription(int id, int x, int y, int range, Role role,	Groups groups) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.range = range;
		this.role = role;
		this.groups = groups;
		this.malicious = false;
	}

	public static double distance(NodeDescription n1, NodeDescription n2) {
		return Math.sqrt((double) ((n1.x - n2.x) * (n1.x - n2.x) + (n1.y - n2.y) * (n1.y - n2.y)));
	}

	
	
	public int getId() {
		return id;
	}

	public Role getRole() {
		return role;
	}

	public Groups getGroups() {
		return groups;
	}
	
	

	public boolean isMalicious() {
		return malicious;
	}

	public void setMalicious(boolean malicious) {
		this.malicious = malicious;
	}

	@Override
	public String toString() {
		return "NodeDescription [id=" + id + ", x=" + x + ", y=" + y
				+ ", range=" + range + ", role=" + role + ", groups=" + groups
				+ "]";
	}

}
