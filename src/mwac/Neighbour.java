package mwac;
/**
 * 
 */



/**
 * @author Anca
 * 
 */
public class Neighbour {

	int id;
	float trust;
	Role role;
	Groups groups;

	public Neighbour(int id, float trust, Role role, Groups groups) {
		super();
		this.id = id;
		this.trust = trust;
		this.role = role;
		this.groups = groups;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Neighbour) {
			Neighbour other = (Neighbour) obj;
			return (this.id == other.id && this.trust == other.trust
					&& this.role == other.role && groups.equals(other.groups));
		} else
			return false;
	}
	
	@Override
	public String toString() {
		return "\nNeighbour [id=" + id + ", trust=" + trust + ", role=" + role + ", groups=" + groups + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getTrust() {
		return trust;
	}

	public void setTrust(float trust) {
		this.trust = trust;
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

	


}
