package mwac;
/**
 * 
 */



/**
 * @author Anca
 * 
 */
public class Neighbour implements Comparable<Neighbour> {

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
	public int compareTo(Neighbour o) {
		if (trust > o.trust)
			return -1;
		else if (trust == o.trust)
			return 0;
		else 
			return 1;
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

	public Groups getGroups() {
		return groups;
	}

	public int getId() {
		return id;
	}

	public Role getRole() {
		return role;
	}

	public float getTrust() {
		return trust;
	}

	public void setGroups(Groups groups) {
		this.groups = groups;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public void setTrust(float trust) {
		this.trust = trust;
	}

	@Override
	public String toString() {
		return "\nNeighbour [id=" + id + ", trust=" + trust + ", role=" + role + ", groups=" + groups + "]";
	}

	


}
