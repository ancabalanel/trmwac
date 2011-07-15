/**
 * 
 */
package sim.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import mwac.Neighbour;

/**
 * @author Anca
 *
 */
public class Parameters {

	
	
	/** After this delay from the last received message the simulation ends */
	public static final long DELAY_BEFORE_SIMULATION_END = 5000;
	
	/** Maximum simulation time */
	public static final long MAX_SIMULATION_TIME = 300000;	
	
	
	/** All honest agents use/don't use authorization */
	public static boolean USE_AUTHORIZATION = false;	
	
	/** All honest agents use/don't use trust based decisions */
	public static boolean USE_TRUST = false;
	
		
	/** Trust threshold */
	public final static float TRUST_THRESHOLD = 0.9f;
	/** Trust recovery parameter */
	public static final float LAMBDA = 0.01f;	
	/** Default time (ms) to listen to a message */
	public static final long WATCH_TIME = 5000;
	/** Trust decrease step */
	public static final float TRUST_DECREASE_UNIT = 0.11f;	
	/** Penalty given to a neighbour upon receiving a warning about it */	
	public static final float TRUST_PENALTY = 0.11f;
	
	
	// other
	/** Monitor the forwarding of Route Requests */
	public static boolean LISTEN_TO_RREQ = false;
	
	/** used by malicious agents to generate fake ids and routes */
	private static int UNKNOWN_ID = 1000;
	
	public static int SIM_NUMBER = 2;
	
	public static Random random = new Random();

	
	public static int generateFakeId(){
		return UNKNOWN_ID + random.nextInt(100);
	}	
	public static List<Integer> generateFakeRoute(){
		List<Integer> fRoute = new ArrayList<Integer>();
		int length = random.nextInt(10);
		for (int i = 0; i < length; i++){
			fRoute.add(generateFakeId());
		}
		return fRoute;
	}
	
	public static Neighbour chooseTrustedNeighbour(List<Neighbour> neighbours){
		if(neighbours.isEmpty())
			return null;		
		else{
			if (neighbours.size() == 1)
				return neighbours.get(0);
			else {
				Collections.sort(neighbours);
				float sum = 0.0f;
				for(Neighbour n : neighbours)
					sum += n.getTrust();
				
				List<Float> intervals = new ArrayList<Float>();
				intervals.add(0.0f);
				
				float sumI = 0.0f;
				for(Neighbour n : neighbours){
					sumI +=  n.getTrust()/sum;
					intervals.add(sumI);
				}
								
				double db = random.nextDouble();
				for (int i = 0; i < intervals.size() - 1; i++) {
					float f1 = intervals.get(i);
					float f2 = intervals.get(i+1);
					if (f1 < db && f2 > db)
						return neighbours.get(i);
				}
			}
			return null;
		}
	}
	
/*	public static void main(String args[]){
		List<Neighbour> nb = new ArrayList<Neighbour>();
		nb.add(new Neighbour(1, 0.5f, Role.None, new Groups()));
		nb.add(new Neighbour(2, 0.5f, Role.None, new Groups()));
		nb.add(new Neighbour(3, 0.5f, Role.None, new Groups()));
		
		for(int i=0;i<10;i++)
			System.out.println(Parameters.chooseTrustedNeighbour(nb));
		
	} */
}
