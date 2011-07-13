/**
 * 
 */
package sim.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mwac.NeighbourTrust;

/**
 * @author Anca
 *
 */
public class Report {
	
	Map<Integer, List<String>> agentSentMeasures;
	Map<Integer, List<String>> agentReceivedMeasures;
	Map<Integer, List<Integer>> distrustMap;
	
	Map<Integer, Map<Integer, Float>> neighbourTrust;
	
	List<Integer> maliciousAgents;
		
	int totalFramesSent;
	int totalFramesReceived;
	
	
	int totalFramesSentVolume; // bytes
	int totalFramesReceivedVolume; // bytes
	
	int numCorruptedRouteEvents;
	int unauthorizedMessages;
	
	private String imageFilename;
	private String outputFilename;
	private String crtFolder;
	
	private PrintWriter out;
	int simCount;

	public Report (String baseFilename, int simCount){
		
		this.simCount = simCount;
		agentSentMeasures = new HashMap<Integer, List<String>>();
		agentReceivedMeasures = new HashMap<Integer, List<String>>();
		distrustMap = new HashMap<Integer, List<Integer>>();
		neighbourTrust = new HashMap<Integer, Map<Integer,Float>>();
		maliciousAgents = new ArrayList<Integer>();
		
		totalFramesReceived = 0;
		totalFramesSent = 0;
		
		numCorruptedRouteEvents = 0;
		unauthorizedMessages = 0;
		
		imageFilename = "images" + File.separator + baseFilename + ".png";
		outputFilename = "output" + File.separator + baseFilename + "-" + simCount + ".html";
		crtFolder = System.getProperty("user.dir");
		
		try {
			out = new PrintWriter(new File(outputFilename));		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public double computeDeliveredPercentage(){
		if (getTotalMeasuresSent() == 0)
			return 0.0;
		return ((double) getTotalMeasuresReceived() / (double) getTotalMeasuresSent()) * 100.0;
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
	
	public void addFramesSentVolume(int volFrame){
		totalFramesSentVolume += volFrame;
	}
	
	public void addFramesReceivedVolume(int volFrame){
		totalFramesReceivedVolume += volFrame;
	}
	
	public void addMaliciousAgent(int source){
		maliciousAgents.add(source);
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
			
			out.println("<html>");
	
			String imageFilepath = crtFolder + File.separator + imageFilename;
			
			out.println("<h1>Results for simulation #" + simCount + "</h1>");
			
			out.println("<h2>Topology</h2>");			
			out.println("<img src=\"" + imageFilepath + "\" alt=\"35agents\"/>");
			
			out.println("</br></br>");
			
			out.println("<h2>Parameters</h2> ");
			
			out.println("Using authorization: " + Parameters.USE_AUTHORIZATION + " </br>");
			out.println("Using trust: " + Parameters.USE_TRUST + " </br>");
			
			if(Parameters.USE_TRUST){
				out.println("Watch time = " + Parameters.WATCH_TIME + " ms + </br>");
				out.println("Trust threshold = " + Parameters.TRUST_THRESHOLD + "</br>");
				out.println("Trust decrease unit = " + Parameters.TRUST_DECREASE_UNIT + "</br>");
				out.println("Trust penalty = " + Parameters.TRUST_PENALTY + "</br>");
				out.println("Trust recovery parameter = " + Parameters.LAMBDA + "</br>");
			}	
			
			out.println("</br>");
			
			int mSize = maliciousAgents.size();
			String message = mSize == 0 ? "There are no malicious agents. "
					: (mSize == 1) ? "There is 1 malicious agent: " : "There are " + mSize + " malicious agents: ";
			
			out.println("<font color=\"red\">" + message + (mSize > 0 ? maliciousAgents : "") +"</font>" + "</br></br>");
			
			out.println("<h2>Statistics</h2> </br></br>");
			
			StringBuilder deliveredPc = new StringBuilder();
			Formatter formatter = new Formatter(deliveredPc);			
			formatter.format("%.2f", computeDeliveredPercentage());
			
			StringBuilder fsVol = new StringBuilder();
			formatter  = new Formatter(fsVol, Locale.US);
			formatter.format("%,d", totalFramesSentVolume);
			
			StringBuilder frVol = new StringBuilder();
			formatter  = new Formatter(frVol, Locale.US);
			formatter.format("%,d", totalFramesReceivedVolume);
			
			String table = "<table> " +					
					"<tr> <td> Sent measures </td>" + "<td>" + getTotalMeasuresSent() + "</td></tr>" +
					"<tr> <td> Received measures </td>" + "<td>" + getTotalMeasuresReceived() + "</td></tr>" +
					"<tr> <td> Delivered percentage </td>" + "<td>" + deliveredPc + " %&nbsp</td></tr>" +
					"<tr> <td> Sent frames </td>" + "<td>" + totalFramesSent + " </td></tr>" +
					"<tr> <td> Received frames </td>" + "<td>" + totalFramesReceived + "</td></tr>" +
					"<tr> <td> Volume of sent frames </td>" + "<td>" + fsVol + " </td></tr>" +
					"<tr> <td> Volume of received frames </td>" + "<td>" + frVol + "</td></tr>" +
					"<tr> <td> Corrupted route events </td>" + "<td>" + numCorruptedRouteEvents + "</td></tr>" +
					"<tr> <td> Unauthorized message events </td>" + "<td>" + unauthorizedMessages + "</td></tr>" +
					"</table>";			
			out.println(table);
			out.println("</br>");
			
			
			
			if (Parameters.USE_TRUST) {
				String trustTable = "<table> ";			
				Set<Entry<Integer,Map<Integer,Float>>> entries = neighbourTrust.entrySet();
				for(Entry<Integer,Map<Integer,Float>> e : entries ){
					trustTable += "<tr> " + "<td> " + e.getKey() + " </td>" + "<td>" + e.getValue().toString() + "</td></tr>" ;
				} 
				
				trustTable += "</table>";
				
				out.println("</br> Modified trust values: </br> ");
				out.println(trustTable);

				out.println("</br>");
				out.println("Distrust map </ br>" + distrustMap);
			}
			
			
			
			out.println("</html>");
			out.close();
			System.out.print("Report file written [ " + outputFilename + " ]. ");
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

	public void addNeighbourTrust(int source, NeighbourTrust nbTrust) {
		
		Map<Integer, Float> nbTrustMap;
		if(neighbourTrust.containsKey(source))
			nbTrustMap = neighbourTrust.get(source);
		else 
			nbTrustMap = new HashMap<Integer, Float>();
		nbTrustMap.put(nbTrust.getNeighbour(), nbTrust.getCurrentTrust());
		neighbourTrust.put(source, nbTrustMap);		
	}
}
