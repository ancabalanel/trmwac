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
	
	int totalFramesSent = 0;
	int totalFramesReceived = 0;
	
	private PrintWriter out;
	
	public Report (String inputName){
		
		agentSentMeasures = new HashMap<Integer, List<String>>();
		agentReceivedMeasures = new HashMap<Integer, List<String>>();
		
		String outputName = "output/orgscenario.out";
		
		try {
			out = new PrintWriter(new File(outputName));		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public double computeLostPercentage(){
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
			out.println("Total measures sent: " + getTotalMeasuresSent());
			out.println("Total measures received: " + getTotalMeasuresReceived());
			out.println();
			out.println("Total frames sent: " + totalFramesSent);
			out.println("Total frames received: " + totalFramesReceived);
			// close file
			out.close();
		}
	}
}
