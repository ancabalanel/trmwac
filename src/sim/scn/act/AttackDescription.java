package sim.scn.act;

import mwac.Role;
/**
 * 
 * @author Anca
 * 
 */
public abstract class AttackDescription extends ActionDescription {

	/** number of attackers */
	int numAttackers;
	/** Role of the attackers */
	Role roleAttackers;

	public AttackDescription(long issued, int numAttackers, Role role) {
		super(issued);
		this.numAttackers = numAttackers;
		this.roleAttackers = role;
	}

	public int getNumAttackers() {
		return numAttackers;
	}

	public void setNumAttackers(int numAttackers) {
		this.numAttackers = numAttackers;
	}

	public Role getRoleAttackers() {
		return roleAttackers;
	}

	public void setRoleAttackers(Role roleAttackers) {
		this.roleAttackers = roleAttackers;
	}

	@Override
	public String toString() {
		return "AttackDescription [numAttackers=" + numAttackers
				+ ", roleAttackers=" + roleAttackers + ", issued=" + issued
				+ "]";
	}

}
