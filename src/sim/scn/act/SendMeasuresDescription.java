/**
 * 
 */
package sim.scn.act;

import java.util.List;

/**
 * @author Anca
 * 
 */
public class SendMeasuresDescription extends ActionDescription {

	List<Integer> ids;
	int totalActions;
	long interval;

	public SendMeasuresDescription(long issued, List<Integer> ids,
			int totalActions, long interval) {
		super(issued);
		this.ids = ids;
		this.totalActions = totalActions;
		this.interval = interval;
	}

	public List<Integer> getIds() {
		return ids;
	}

	public void setIds(List<Integer> ids) {
		this.ids = ids;
	}

	public int getTotalActions() {
		return totalActions;
	}

	public void setTotalActions(int totalActions) {
		this.totalActions = totalActions;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	@Override
	public String toString() {
		return "DefaultActionDescription [ids=" + ids + ", totalActions="
				+ totalActions + ", interval=" + interval + ", issued="
				+ issued + "]";
	}
}
