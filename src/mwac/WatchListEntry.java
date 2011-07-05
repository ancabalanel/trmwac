/**
 * 
 */
package mwac;

import mwac.msgs.Message;

/**
 * @author Anca
 * 
 */
public class WatchListEntry {
	int watchedNode;
	int interactionNo;

	Message message;

	public WatchListEntry(int nodeId, int interactionNo, Message message) {
		super();
		this.watchedNode = nodeId;
		this.interactionNo = interactionNo;
		this.message = message;
	}

	public int getWatchedNode() {
		return watchedNode;
	}

	public void setWatchedNode(int watchedNode) {
		this.watchedNode = watchedNode;
	}

	public int getInteractionNo() {
		return interactionNo;
	}

	public void setInteractionNo(int interactionNo) {
		this.interactionNo = interactionNo;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "WatchListEntry [watched=" + watchedNode + ", interaction=" + interactionNo + ", message=" + message +  "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + interactionNo;
		result = prime * result + watchedNode;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WatchListEntry other = (WatchListEntry) obj;
		if (interactionNo != other.interactionNo)
			return false;
		if (watchedNode != other.watchedNode)
			return false;
		return true;
	}

	
}
