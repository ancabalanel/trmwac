/**
 * 
 */
package sim.scn.act;

import mwac.Role;

/**
 * @author Anca
 *
 */
public class NoForwardDescription extends AttackDescription {
	
	/** The percentage of dropped messages */
	float dropPercentage;
	
	public NoForwardDescription(long issued, int numAttackers, Role role, float drop) {
		super(issued, numAttackers, role);
		this.dropPercentage = drop;
	}

	public float getDropPercentage() {
		return dropPercentage;
	}
}
