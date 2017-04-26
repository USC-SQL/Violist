package usc.sql.string;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import usc.sql.ir.Variable;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.NodeInterface;

public class RegionNode {

	private List<NodeInterface> nodeList = new ArrayList<>();
	private RegionNode parent;
	private List<RegionNode> children = new ArrayList<>();
	private EdgeInterface backedge;
	private int regionNumber;
	private Map<String,ArrayList<Variable>> useMap;
	


	public RegionNode(int i, EdgeInterface b) {
		backedge = b;
		regionNumber = i;
	}

	public Map<String, ArrayList<Variable>> getUseMap() {
		return useMap;
	}


	public void setUseMap(
			Map<String, ArrayList<Variable>> useMap) {
		this.useMap = useMap;
	}

	public int getRegionNumber() {
		return regionNumber;
	}

	public EdgeInterface getBackEdge() {
		return backedge;
	}

	public void addToNodeList(NodeInterface n) {
		nodeList.add(n);
	}

	public void addToChildrenList(RegionNode n) {
		children.add(n);
	}

	public List<NodeInterface> getNodeList() {
		return nodeList;
	}

	public RegionNode getParent() {
		return parent;
	}

	public void setParent(RegionNode parent) {
		this.parent = parent;
	}

	public List<RegionNode> getChildren() {
		return children;
	}

	public void setChildren(List<RegionNode> children) {
		this.children = children;
	}

	public boolean contain(RegionNode rn) {
		if (nodeList.containsAll(rn.getNodeList()))
			return true;
		else
			return false;
	}
}
