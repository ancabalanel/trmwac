/**
 * 
 */
package mwac.msgs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class MRoutedData extends Message{
	
	MData data;
	List<Integer> route;
	

	public MRoutedData(int source, int destination, MData data, List<Integer> route){
		super(source,destination);
		this.data = data;
		this.route = route;
	}
	

	public MRoutedData(int source, int destination, String data) {
		this(source, destination, new MData(source, destination, data), new ArrayList<Integer>());
	}
	
	public MRoutedData(int source, int destination) {
		this(source, destination, "");
	}

	public MData getData() {
		return data;
	}

	public void setData(MData data) {
		this.data = data;
	}

	public List<Integer> getRoute() {
		return route;
	}

	public void setRoute(List<Integer> route) {
		this.route = route;
	}

	@Override
	public String toString() {
		return "MRoutedData [data=" + data + ", route=" + route + ", source="
				+ source + ", destination=" + destination + "]";
	}


}
