package sim.behaviours;

import jade.core.behaviours.TickerBehaviour;
import sim.Sensor;
import sim.scn.instr.FabricateMessageInstruction;

@SuppressWarnings("serial")
public class SendPeriodicFabricatedMessage extends TickerBehaviour {

	private Sensor agent;
	FabricateMessageInstruction fab;
	int times = 0;

	public SendPeriodicFabricatedMessage(Sensor sensor, long period, FabricateMessageInstruction instr) {
		super(sensor, period);
		agent = (Sensor)myAgent;
		this.fab = instr;

	}

	@Override
	protected void onTick() {
		if (times < fab.getTotalMsgs()) {

			agent.fabricateMessage(fab.getMessageType());

			times++;
		} else {
			agent.removeBehaviour(this);
		}
	}
}