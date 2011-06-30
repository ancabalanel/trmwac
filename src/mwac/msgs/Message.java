/**
 * 
 */
package mwac.msgs;

import java.io.Serializable;


/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public abstract class Message implements Serializable{
	int source;
	int destination;
	
	public Message(int source, int destination) {
		super();
		this.source = source;
		this.destination = destination;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public abstract String toString();
	
}
