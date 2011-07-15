/**
 * 
 */
package sim.scn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mwac.Groups;
import mwac.Role;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sim.scn.act.ActionDescription;
import sim.scn.act.AttackDescription;
import sim.scn.act.FabricationDescription;
import sim.scn.act.ModificationDescription;
import sim.scn.act.NoForwardDescription;
import sim.scn.act.SendMeasuresDescription;
import sim.scn.instr.FabricateMessageInstruction;
import sim.scn.instr.Instruction;
import sim.scn.instr.ModifyMessageInstruction;
import sim.scn.instr.NoForwardInstruction;
import sim.scn.instr.SendMeasuresInstruction;

/**
 * @author Anca
 * 
 */
public class Scenario {

	/**
	 * @return The first child element of the root node that has the given tag,
	 *         or null if there is no such element.
	 */
	private static Element getElementByTagName(Element root, String tag) {
		NodeList l = root.getElementsByTagName(tag);
		if (l != null) {
			return (Element) l.item(0);
		}

		return null;
	}
	
	private static String getFinalConfigFilename(String fileName){
		String tmp  = String.copyValueOf(fileName.toCharArray());
		return "output/" + tmp.substring(tmp.indexOf("/") + 1, tmp.indexOf(".")) + "_f.xml";		
	}
	
	Organization organization;
	
	int workstationId;
	
	List<ActionDescription> actions;

	String finalConfigFilename = "output/default.xml";

	public Scenario() {
		organization = new Organization();
		actions = new ArrayList<ActionDescription>();
	}

	public Scenario(String filePath) {

		this();
		finalConfigFilename = getFinalConfigFilename(filePath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(filePath));

			Element root = document.getDocumentElement();

			Element org = getElementByTagName(root, "organization");
			if(org != null)
				parseOrganization(org);
			
			Element ws = getElementByTagName(root, "workstation");
			if(ws != null)
				workstationId = Integer.parseInt(ws.getAttribute("id"));
			
			Element measureEl = getElementByTagName(root, "measures");
			if(measureEl != null){
				SendMeasuresDescription measure = parseSendMeasure(measureEl);
				actions.add(measure);	
			}
			
			NodeList attacks = root.getElementsByTagName("attack");
			if(attacks != null){
				for(int i=0;i<attacks.getLength();i++){
					AttackDescription attack = parseAttack((Element) attacks.item(i));
					actions.add(attack);
				}
			}
			
			// actions are sorted in increasing order of the date they were issued on
			// relative to the beginning of the simulation.
			Collections.sort(actions); 
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public List<Instruction> buildInstructionList(ActionDescription ad){
		List<Instruction> instructions = new ArrayList<Instruction>();
		
		if(ad instanceof SendMeasuresDescription){
			SendMeasuresDescription sm = (SendMeasuresDescription)ad;
			for(Integer agent : ((SendMeasuresDescription) ad).getIds()){
				instructions.add(new SendMeasuresInstruction(agent, sm.getInterval(), sm.getTotalActions(), workstationId));
			}
		} else if (ad instanceof FabricationDescription) {
			FabricationDescription fab = (FabricationDescription) ad;

			List<Integer> attackers = getRandomIds(fab.getNumAttackers(), fab.getRoleAttackers());

			for(Integer a : attackers){
				instructions.add(new FabricateMessageInstruction(a, fab.getMessageType(), fab.getInterval(), fab.getTotalMsgs()));
			}
		} else if (ad instanceof NoForwardDescription) {
			NoForwardDescription nfwd = (NoForwardDescription) ad;
			
			List<Integer> attackers = getRandomIds(nfwd.getNumAttackers(), nfwd.getRoleAttackers());
			
			for(Integer a : attackers){
				instructions.add(new NoForwardInstruction(a, nfwd.getDropPercentage()));
			}
		} else if (ad instanceof ModificationDescription){
			ModificationDescription md = (ModificationDescription) ad;
			
			List<Integer> attackers = getRandomIds(md.getNumAttackers(), md.getRoleAttackers());
			
			for(Integer a : attackers){
				instructions.add(new ModifyMessageInstruction(a, md.getMsgType(), md.getModProb()));
			}
		}
		return instructions;			
	}
	
	public List<ActionDescription> getActions() {
		return actions;
	}
	
	public List<Integer> getNodeIds() {
		return organization.getNodeIds();
	}

	public Organization getOrganization() {
		return organization;
	}

	/**
	 * 
	 * @param num
	 * @param role
	 * @return num agents with the specified role, to be used as attackers
	 */
	public List<Integer> getRandomIds(int num, Role role){
		List<Integer> ids = new ArrayList<Integer>();

		//  TODO TESTING .. 
		ids.add(9);  return ids;
		 
		
		/*ids = organization.getIdsWithRole(role, workstationId, true);

		if (ids.size() < num){
			for(Integer i : ids)
				organization.setMalicious(i, true);
			return ids;
		}
		else {
			List<Integer> randNum = new ArrayList<Integer>();
			Collections.shuffle(ids);
			for (int i = 0; i < num; i++)
				randNum.add(ids.get(i));
			for(Integer r : randNum)
				organization.setMalicious(r, true);
			return randNum;
		}*/
	
	}
	
	public int getWorkstationId() {
		return workstationId;
	}

	private AttackDescription parseAttack(Element attack){
	 	String type = attack.getAttribute("type"); 
		int numAttackers = Integer.parseInt(attack.getAttribute("num"));
		Role role = Role.valueOf(attack.getAttribute("role"));
		Long issued = Long.parseLong(attack.getAttribute("issued"));
		
		if(type.equals("fabricate")){
			String msgType = attack.getAttribute("msgType");
			int totalMsgs = Integer.parseInt(attack.getAttribute("total"));
			long interval = Long.parseLong(attack.getAttribute("interval"));
			return new FabricationDescription(issued, numAttackers, role, msgType, interval, totalMsgs);
		} else if (type.equals("nofwd")){
			float drop  = Float.parseFloat(attack.getAttribute("probDrop"));
			return new NoForwardDescription(issued, numAttackers, role, drop);
		} else if (type.equals("modify")){
			String msgType = "mwac.msgs.M" + attack.getAttribute("msgType");
			float probMod = Float.parseFloat(attack.getAttribute("probMod"));
			return new ModificationDescription(issued, numAttackers, role, msgType, probMod);
		} else
			return null;
	}

	private void parseOrganization(Element element) {
		
		boolean stable = Boolean.getBoolean(element.getAttribute("stable"));
		organization.setStable(stable);
		
		NodeList nodes = element.getElementsByTagName("node");

		List<NodeDescription> nodeList = new ArrayList<NodeDescription>();

		for (int i = 0; i < nodes.getLength(); i++) {

			Element node = (Element) nodes.item(i);

			int id = Integer.parseInt(node.getAttribute("id").trim());
			int x = Integer.parseInt(node.getAttribute("x").trim());
			int y = Integer.parseInt(node.getAttribute("y").trim());
			int range = Integer.parseInt(node.getAttribute("range").trim());
			Role role = Role.valueOf(node.getAttribute("role").trim());
			
			Groups groups = new Groups();
			NodeList groupList = node.getElementsByTagName("group");

			for (int g = 0; g < groupList.getLength(); g++) {
				Element e = (Element) groupList.item(g);
				groups.addGroup(Integer.parseInt(e.getAttribute("id").trim()));
			}
			
			if(role == Role.Representative)
				groups.addGroup(id);

			nodeList.add(new NodeDescription(id, x, y, range, role, groups));
		}

		organization.setNodeDescriptions(nodeList);
	}

	private SendMeasuresDescription parseSendMeasure(Element element){

		List<Integer> ids = new ArrayList<Integer>();
		String id = element.getAttribute("id");
		
		if(id.equals("*")){ // all nodes
			for(NodeDescription nd : organization.nodeDescriptions)
				if(nd.id != workstationId)
					ids.add(nd.id);
		} else { // the specified id
			ids.add(new Integer(id));
		}		
		long issued = Long.parseLong(element.getAttribute("issued"));
		int total = Integer.parseInt(element.getAttribute("total"));
		long interval = Long.parseLong(element.getAttribute("interval"));
		
		return new SendMeasuresDescription(issued, ids, total, interval);
	}
	

	public long sleepTime(int i){
		if(actions.get(i+1) != null && actions.get(i) != null)
			return actions.get(i+1).getIssued() - actions.get(i).getIssued();
		return 0;
	}
	@Override
	public String toString() {
		return "Scenario [org:\t" + organization + "]";
	}

}
