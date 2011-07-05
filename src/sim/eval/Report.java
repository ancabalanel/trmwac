/**
 * 
 */
package sim.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Anca
 *
 */
public class Report {
	
	Map<Integer, List<String>> agentSentMeasures;
	Map<Integer, List<String>> agentReceivedMeasures;
	Map<Integer, List<Integer>> distrustMap;
	
	int totalFramesSent;
	int totalFramesReceived;
	
	List<Integer> malicious;
	
	int numCorruptedRouteEvents;
	int unauthorizedMessages;
	
	private PrintWriter out;
	
	public Report (String htmlFile){
		
		agentSentMeasures = new HashMap<Integer, List<String>>();
		agentReceivedMeasures = new HashMap<Integer, List<String>>();
		distrustMap = new HashMap<Integer, List<Integer>>();
		malicious = new ArrayList<Integer>();
		
		totalFramesReceived = 0;
		totalFramesSent = 0;
		
		numCorruptedRouteEvents = 0;
		unauthorizedMessages = 0;
		
		try {
			out = new PrintWriter(new File(htmlFile));		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	// TODO: review
	public double computeLostPercentage(){
		return (100.0 - ((double) getTotalMeasuresReceived() / (double) getTotalMeasuresSent()) * 100.0);
	}
	
	public void addSentMeasure(int aid, String measure) {
		List<String> measures; 
		if (agentSentMeasures.containsKey(aid)) {
			measures = agentSentMeasures.get(aid);
		} else {
			measures = new ArrayList<String>();			
		}
		measures.add(measure);
		agentSentMeasures.put(aid, measures);		

	}
	
	public void addReceivedMeasure(int aid, String measure){
		List<String> measures; 
		if (agentReceivedMeasures.containsKey(aid)) {
			measures = agentReceivedMeasures.get(aid);
		} else {
			measures = new ArrayList<String>();			
		}
		measures.add(measure);
		agentReceivedMeasures.put(aid, measures);		
	}
	
	public void addFramesReceived(int nFrames){
		totalFramesReceived += nFrames;
	}
	
	public void addFramesSent(int nFrames){
		totalFramesSent += nFrames;
	}
	
	public void addMaliciousAgent(int source){
		malicious.add(source);
	}
	
	public void addCorruptedRoute(int source, List<Integer> route, int sender){
		numCorruptedRouteEvents++;
	}
	
	public void addUnauthorizedMessage(int source, String msgType, int msgSource, int msgSender){
		unauthorizedMessages++;
	}
	
	private int getTotalMeasuresSent(){
		int totalMeasuresSent = 0;
		
		Set<Entry<Integer, List<String>>> entries = agentSentMeasures.entrySet();
		for(Entry<Integer, List<String>> e : entries){
			totalMeasuresSent += e.getValue().size();
		}
		
		return totalMeasuresSent;
	}

	private int getTotalMeasuresReceived() {
		int totalMeasuresReceived = 0;

		Set<Entry<Integer, List<String>>> entries = agentReceivedMeasures.entrySet();
		for (Entry<Integer, List<String>> e : entries) {
			totalMeasuresReceived += e.getValue().size();
		}
		return totalMeasuresReceived;
	}
	
	public void close(){
		if (out != null) {
			// write results
			out.println("<html>");
	
			out.println("There are " + malicious.size() + " malicious agents: " + malicious);
			String table = "<table> " +
					
					"<tr> " + "<td> Sent measures </td>" + "<td>" + getTotalMeasuresSent() + "</td></tr>" +
					"<tr> " + "<td> Received measures </td>" + "<td>" + getTotalMeasuresReceived() + "</td></tr>" +
					"<tr> " + "<td> Lost percentage </td>" + "<td>" + computeLostPercentage() + " %&nbsp</td></tr>" +
					"<tr> " + "<td>  </td> <td> </td></tr>" +
					"<tr> " + "<td> Sent frames </td>" + "<td>" + totalFramesSent + " </td></tr>" +
					"<tr> " + "<td> Received frames </td>" + "<td>" + totalFramesReceived + "</td></tr>" +
					"</table>";
			
			out.println(table);
			
			out.println("</br>");
			out.println("Distrust map </ br>" + distrustMap);
			out.println("</html>");
			out.close();
		}
	}

	public void addDistrustNode(int source, int neighbour) {
		List<Integer> nbIds; 
		if (distrustMap.containsKey(source)) {
			nbIds = distrustMap.get(source);
		} else {
			nbIds = new ArrayList<Integer>();			
		}
		nbIds.add(neighbour);
		distrustMap.put(source, nbIds);	
	}
}
