package usc.sql.string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;

public class Reachability {

	private Set<NodeInterface> allNode;
	private Set<EdgeInterface> allEdge;
	private Set<RNode> allRNode = new HashSet<RNode>();

	Reachability(Set<NodeInterface> allNode, Set<EdgeInterface> allEdge) {
		this.allNode = allNode;
		this.allEdge = allEdge;
		// this.allNode=allNode;
		for (NodeInterface n : allNode) {
			allRNode.add(new RNode(n));
		}
	}

	private void initialize() {
		for (RNode rn : allRNode) {
			rn.getGenSet().add(rn.getNode());
			rn.getOutSet().add(rn.getNode());
		}
	}

	private boolean compareTwoSet(Set<NodeInterface> oldset,
			Set<NodeInterface> newset) {/*
										 * Set<String> s1 = new HashSet<>();
										 * Set<String> s2 = new HashSet<>();
										 * for(NodeInterface o:oldset)
										 * System.out.println(o.getName());
										 * for(NodeInterface n:newset)
										 * System.out.println(n.getName());
										 * System.out.println("");
										 */

		if (oldset.containsAll(newset) && newset.containsAll(oldset))
			return true;
		else
			return false;
	}

	void computeReachability() {
		initialize();
		Map<RNode, ArrayList<RNode>> preList = new HashMap<>();
		// Create predecessor list for each node
		for (RNode node : allRNode) {
			ArrayList<RNode> nodePre = new ArrayList<>();
			for (EdgeInterface e : node.getNode().getInEdges()) {
				for (RNode n : allRNode)
					if (n.getNode().equals(e.getSource()))
						nodePre.add(n);

			}

			preList.put(node, nodePre);

		}
		Map<String, Set<NodeInterface>> old = new HashMap<String, Set<NodeInterface>>();
		for (RNode rn : allRNode) {
			Set<NodeInterface> s = new HashSet<>(rn.getOutSet());
			old.put(rn.getNode().getOffset().toString(), s);
			
		}
		boolean needtoloop = true;

		while (needtoloop) {
			needtoloop = false;
			for (RNode rn : allRNode) {
				// Union the out set of all the nodes in the predecessor
				// list，add to the in set of node rn
				for (RNode node : preList.get(rn)) {
					for (NodeInterface outnode : node.getOutSet())
						if (!rn.getInSet().contains(outnode))
							rn.getInSet().add(outnode);
				}
				// Union the gen set and in set to the out set of node rn
				for (NodeInterface gennode : rn.getGenSet())
					if (!rn.getOutSet().contains(gennode))
						rn.getOutSet().add(gennode);
				for (NodeInterface innode : rn.getInSet())
					if (!rn.getOutSet().contains(innode))
						rn.getOutSet().add(innode);
				if (!compareTwoSet(old.get(rn.getNode().getOffset().toString()),
						rn.getOutSet())) {
					needtoloop = true;
					Set<NodeInterface> s = new HashSet<>(rn.getOutSet());
					old.put(rn.getNode().getOffset().toString(), s);

				}
			}
		}

	}

	public Set<RNode> getRNode() {
		return allRNode;
	}
}

class RNode {
	private NodeInterface node;
	private Set<NodeInterface> inSet = new HashSet<>();
	private Set<NodeInterface> outSet = new HashSet<>();
	private Set<NodeInterface> genSet = new HashSet<>();

	public RNode(NodeInterface node) {
		this.node = node;
	}

	public NodeInterface getNode() {
		return node;
	}

	public void setNode(NodeInterface node) {
		this.node = node;
	}

	public Set<NodeInterface> getInSet() {
		return inSet;
	}

	public void setInSet(Set<NodeInterface> inSet) {
		this.inSet = inSet;
	}

	public Set<NodeInterface> getOutSet() {
		return outSet;
	}

	public void setOutSet(Set<NodeInterface> outSet) {
		this.outSet = outSet;
	}

	public Set<NodeInterface> getGenSet() {
		return genSet;
	}

	public void setGenSet(Set<NodeInterface> genSet) {
		this.genSet = genSet;
	}

}