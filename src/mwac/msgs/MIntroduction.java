/**
 * 
 */
package mwac.msgs;

/**
 * @author Anca
 *
 */
@SuppressWarnings("serial")
public class MIntroduction extends Message{
	
	public MIntroduction(int source){
		super(source, Frame.BROADCAST);
	}
	

	@Override
	public String toString() {
		return "MIntroduction [source=" + source + ", destination=" + Frame.idToStr(destination) + "]";
	}
	
	
}
