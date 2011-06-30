/**
 * 
 */
package sim.events;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class UnauthorizedMessageEvent extends Event {

	String messageType;
	int msgSource;
	int msgSender;
	
	public UnauthorizedMessageEvent(int source, String mType, int msgSource, int msgSender) {
		super(source);
		this.messageType = mType;
		this.msgSource = msgSource;
		this.msgSender =  msgSender;
	}

	public String getMessageType() {
		return messageType;
	}

	public int getMsgSource() {
		return msgSource;
	}

	public int getMsgSender() {
		return msgSender;
	}
	
	
}
