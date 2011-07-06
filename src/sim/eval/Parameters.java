/**
 * 
 */
package sim.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Anca
 *
 */
public class Parameters {
	/** used by malicious agents to generate fake ids and routes */
	private static int UNKNOWN_ID = 1000;
	
	
	/** After this delay from the last received message the simulation ends */
	public static final long DELAY_END = 5000;
	
	/** Maximum simulation time */
	public static final long MAXIMUM_TIME = 300000;
	
	/** All honest agents use/don't use authorization */
	public static boolean USE_AUTHORIZATION = false;
	
	/** All hones agents use/don't use trust based decisions */
	public static boolean USE_TRUST = true;
	
	// For trust management 
	
	/** Trust threshold */
	public final static float TRUST_THRESHOLD = 0.7f;

	/** Trust recovery parameter */
	public static final float LAMBDA = 0.1f;
	
	/** Default time (ms) to listen to a message */
	public static final long WATCH_TIME = 30000;

	/** decrease step */
	public static final float DECREASE_STEP = 0.1f;
	
	public static Random random = new Random();

	
	public static int generateFakeId(){
		return UNKNOWN_ID + random.nextInt(100);
	}	
	public static List<Integer> generateFakeRoute(){
		List<Integer> fRoute = new ArrayList<Integer>();
		int length = random.nextInt(10);
		for (int i = 0; i < length; i++){
			fRoute.add(random.nextInt(UNKNOWN_ID));
		}
		return fRoute;
	}
}
