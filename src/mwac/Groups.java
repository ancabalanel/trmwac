package mwac;
/**
 * 
 */


import java.util.ArrayList;
import java.util.List;

/**
 * @author Anca
 * 
 */
public class Groups {

	List<Integer> groups;
	
	public Groups(){
		groups = new ArrayList<Integer>();	
	}	

	public Groups(List<Integer> groups) {
		this.groups = groups;
	}

	public void setGroups(List<Integer> groups) {
		this.groups = groups;
	}
	
	public List<Integer> getGroups(){
		return groups;
	}

	public boolean addGroup(int gId) {
		return groups.add(gId);
	}

	@Override
	public String toString() {
		return "Groups [groups=" + groups + "]";
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Groups){
			Groups other = (Groups)obj;
			return groups.containsAll(other.groups)
					&& other.groups.containsAll(groups);
		}else
			return false;
	}

	public boolean contains(int groupId) {
		return groups.contains(groupId);
	}
	
}
