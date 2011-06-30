package sim.scn.act;

/**
 * 
 * @author Anca
 *
 */
public class ActionDescription implements Comparable<ActionDescription>{
	
	/** Date relative to the start of the simulation when the action was issued */
	long issued;
	
	public ActionDescription(long issued){
		this.issued = issued;
	}

	@Override
	public int compareTo(ActionDescription o) {
		if(issued < o.issued)
			return -1;
		else if(issued == o.issued)
			return 0;
		else 
			return 1;
	}

	public long getIssued() {
		return issued;
	}	
}
