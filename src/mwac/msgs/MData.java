/**
 * 
 */
package mwac.msgs;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class MData extends Message{

	String data;
	
	public MData(int source, int destination, String data){
		super(source, destination);
		this.data = data;		
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "MData [data=" + data + ", source=" + source + ", destination="
				+ destination + "]";
	}
	
	
}
