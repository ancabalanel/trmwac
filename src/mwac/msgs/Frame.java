/**
 * 
 */
package mwac.msgs;

import jade.util.leap.Serializable;

/**
 * @author Anca
 * 
 */
@SuppressWarnings("serial")
public class Frame implements Serializable{
	
	public static final int BROADCAST = -1;
	public static final int BROADCAST_REPRESENTATIVE = -2;
	public static final int BROADCAST_LINK = -3;
	
	int sender;
	int receiver;
	
	Message message;
	
	public Frame(Message message){
		this(0,0,message);
	}

	public Frame(int sender, int receiver, Message message) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
	}

	
	public int getSender() {
		return sender;
	}


	public void setSender(int sender) {
		this.sender = sender;
	}

	

	public int getReceiver() {
		return receiver;
	}


	public void setReceiver(int receiver) {
		this.receiver = receiver;
	}


	public Message getMessage() {
		return message;
	}

	public boolean isSenderSet(){
		return sender > 0;	
	}
	
	public boolean isReceiverSet(){
		return receiver != 0;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public static String idToStr(int id){
		if(id == BROADCAST) 
			return "BCAST";
		if(id == BROADCAST_REPRESENTATIVE)
			return "BCAST_REP";
		if(id == BROADCAST_LINK)
			return "BCAST_LINK";
		return id + "";
	}
	@Override
	public String toString() {
		return "Frame [sender=" + sender + ", receiver=" + idToStr(receiver) + ", message=" + message + "]";
	}

}
