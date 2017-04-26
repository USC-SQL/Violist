package usc.sql.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Unit;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cdg.Dominator;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class LayerRegion {

	private Map<NodeInterface, Boolean> marked = new HashMap<>();
	private Map<NodeInterface, Boolean> markedforpre = new HashMap<>();

	private Map<RegionNode, Boolean> markedforRTree = new HashMap<>();
	private List<RegionNode> allRegionNodeList = new ArrayList<>();

	private List<EdgeInterface> backedge = new ArrayList<>();
	private Map<EdgeInterface, ArrayList<NodeInterface>> loopSet = new HashMap<EdgeInterface, ArrayList<NodeInterface>>();
	private CFGInterface cfg;
	private RegionNode root;
	public LayerRegion(CFGInterface cfg)
	{	
		this.cfg = cfg;
		
	}
	public RegionNode getRoot()
	{	
		removeExceptionBlock(cfg);
		root = buildRegionTree(cfg,identifyLoopSet(cfg.getAllNodes(),identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(),cfg.getEntryNode())));
		return root;
	}
	public List<RegionNode> getAllRegionNode()
	{
		return allRegionNodeList;
	}
	public boolean isLoopNode(NodeInterface n,EdgeInterface backedge)
	{
		if(loopSet.get(backedge).contains(n))
			return true;
		else
			return false;
	}
	public void run() {	
		identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(),cfg.getEntryNode());
				
		// Perform reverse dfs on cfg to get the loop nodes for each backedge
		identifyLoopSet(cfg.getAllNodes(),backedge);
		
		
		RegionNode root = buildRegionTree(cfg,loopSet);
		


	}
	public void tranverseRTree(RegionNode root,ReachingDefinition rd)
	{
		// initialize
		for (RegionNode rn : allRegionNodeList) {
			markedforRTree.put(rn, false);
		}		
		//System.out.println("Region tree:"+root.getChildren().size());
		dfsRegionTree(root,rd);
	}
	
	public List<EdgeInterface> identifyBackEdges(Set<NodeInterface> allNodes,Set<EdgeInterface> allEdges,NodeInterface entry)
	{

		
		for (NodeInterface node : allNodes) 
		{
			
			marked.put(node, false);		
		}
		//System.out.println(marked);
		// Perform dfs on cfg to get the back edges
		dfs(entry);

		
		// Perform Domination Analysis
		Dominator d = new Dominator(allNodes, allEdges,entry);
		
		List<EdgeInterface> outputBackedge = new ArrayList<>();
		for(EdgeInterface bg:backedge)
		{
			// Check if the destination of the back edge dominates the source of
			// the back edge

			if (d.isDominate(bg.getDestination(), bg.getSource()))
				outputBackedge.add(bg);
		}
		backedge.clear();
		for(EdgeInterface bg:outputBackedge)
		{
			backedge.add(bg);
		}
		
		return backedge;
	}
	
	
	public Map<EdgeInterface, ArrayList<NodeInterface>> identifyLoopSet(Set<NodeInterface> allNodes,List<EdgeInterface> backedge)
	{		
		
		for (EdgeInterface bg : backedge) {
			// initialization
			for (NodeInterface node : allNodes)
				markedforpre.put(node, false);
			ArrayList<NodeInterface> loopnodes = new ArrayList<NodeInterface>();

				
				dfsReverse(bg.getSource(), bg.getDestination());

				for (Entry<NodeInterface, Boolean> e : markedforpre.entrySet()) {
					if (e.getValue()) {
						//System.out.print(e.getKey().getName() + " ");
						loopnodes.add(e.getKey());
					}
				}
				//System.out.println("");

				loopSet.put(bg, loopnodes);		
		}
		
		return loopSet;
	}
	
	private void dfs(NodeInterface node) {
		marked.put(node, true);
		for (EdgeInterface successorEdge : node.getOutEdges()) {

				if (marked.get(successorEdge.getDestination()))
					backedge.add(successorEdge);
			

		}

		for(EdgeInterface e:node.getOutEdges())
		{

				if(!marked.get(e.getDestination()))
					dfs(e.getDestination());

		}
	}

	private void dfsReverse(NodeInterface node, NodeInterface backedgeDest) {
		markedforpre.put(node, true);

		if (!node.getOffset().toString().equals(backedgeDest.getOffset().toString())) {
			for(EdgeInterface e:node.getInEdges())
			{
				if(!markedforpre.get(e.getSource()))
					dfsReverse(e.getSource(),backedgeDest);
			}
		}
	}

	private void dfsRegionTree(RegionNode root,ReachingDefinition rd) {
		markedforRTree.put(root, true);
		
		for (RegionNode child : root.getChildren()) {
			if (!markedforRTree.get(child))
				dfsRegionTree(child,rd);
		}
		System.out.println("Region tree:"+root.getChildren().size()+ " "+root.getRegionNumber());
		/*
		if(root.getBackEdge()!=null)
		{
			
		NodeInterface entryOfRegion = root.getBackEdge().getDestination();
		
		System.out.println(entryOfRegion.getName()+": "+((Node<Unit>)entryOfRegion).getActualNode());
		NodeInterface temp = entryOfRegion.getOutEdges().iterator().next().getDestination();
		if(entryOfRegion.getName().equals("15"))
		{System.out.println("sdf");
			while(!temp.equals(entryOfRegion))
			{	System.out.println(temp.getName()+": "+((Node<Unit>)temp).getActualNode());
			    temp = temp.getOutEdges().iterator().next().getDestination();
			}
			
		}
		}*/
		
		for (NodeInterface n : root.getNodeList())
			System.out.println(n.getOffset().toString() + ": " + ((Node<Unit>)n).getActualNode());
		System.out.println("");
	}

	public RegionNode buildRegionTree(CFGInterface cfg,Map<EdgeInterface, ArrayList<NodeInterface>> loopSet) {
		List<ArrayList> allTheLists = new ArrayList<ArrayList>();


		Map<ArrayList<NodeInterface>, EdgeInterface> tempMap = new HashMap<>();
		// find the list with smallest size
		for (Entry<EdgeInterface, ArrayList<NodeInterface>> et : loopSet
				.entrySet()) {
			allTheLists.add(et.getValue());
			tempMap.put(et.getValue(), et.getKey());

		}
		Collections.sort(allTheLists, new Comparator<ArrayList>() {
			public int compare(ArrayList a1, ArrayList a2) {
				return a1.size() - a2.size(); // assumes you want biggest to
												// smallest
			}
		});
		List<RegionNode> tempList = new ArrayList<>();
		int count = 1;
		for (ArrayList<NodeInterface> nodeList : allTheLists) {
			// Get the back edge
			EdgeInterface e = tempMap.get(nodeList);
			RegionNode rn = new RegionNode(count, e);
			count++;

			List<RegionNode> temptempList = new ArrayList<>();
			
			
			for (NodeInterface n : nodeList)
				rn.addToNodeList(n);
			
			//if rn contains one of the tempList, replace it with rn,else add rn to tempList
			boolean contain = false;
			List<RegionNode> notContainNodes = new ArrayList<>();
			for (RegionNode temp : tempList) {
				//System.out.println(rn.getNodeList().size()+"size"+temp.getNodeList().size());
				if (rn.contain(temp)) {	
						temp.setParent(rn);
						rn.addToChildrenList(temp);
						if(!temptempList.contains(rn))
							temptempList.add(rn);
						contain = true;
					
				}
				else
				{
					if(!temptempList.contains(temp))
						temptempList.add(temp);
				}

			}
			if(!contain)
			{
				for(RegionNode temp: tempList)
					temptempList.add(temp);
						
				temptempList.add(rn);	
			}			
			allRegionNodeList.add(rn);
			tempList.clear();
			for(RegionNode r:temptempList)
				tempList.add(r);
		}

	
		// root node
		RegionNode root = new RegionNode(0, null);
		for (NodeInterface n : cfg.getAllNodes()) {
			root.addToNodeList(n);
		}
		for (RegionNode temp : allRegionNodeList) {
			if (temp.getParent() == null) {
				root.addToChildrenList(temp);
				temp.setParent(root);
			}

		}

		return root;
	}
	private void removeExceptionBlock(CFGInterface cfg)
	{
		//remove redundant catch and finally block
		Map<NodeInterface, Boolean> marked = new HashMap<>();
		NodeInterface entry = cfg.getEntryNode();
		Set<EdgeInterface> realEdge = new HashSet<>();
		for(EdgeInterface e: cfg.getEntryNode().getOutEdges())
		{
			if(e.getLabel().equals("real"))
				realEdge.add(e);
		}
		entry.getOutEdges().retainAll(realEdge);
		for(NodeInterface n:cfg.getAllNodes())
		{
			marked.put(n, false);
		}
		Set<NodeInterface> dfsNode = new HashSet<>();
		dfs(entry,marked,dfsNode);
		cfg.getAllNodes().retainAll(dfsNode);
		
		for(NodeInterface n: dfsNode)
		{
			Set<EdgeInterface> remain = new HashSet<EdgeInterface>();
			for(EdgeInterface e:n.getInEdges())
			{
				if(dfsNode.contains(e.getSource()))
					remain.add(e);
			}
			
			n.getInEdges().retainAll(remain);
		}
		
	}
	private void dfs(NodeInterface node,Map<NodeInterface, Boolean> marked, Set<NodeInterface> dfsNode) {
		marked.put(node, true);
		dfsNode.add(node);

		for(EdgeInterface e:node.getOutEdges())
		{

				if(!marked.get(e.getDestination()))
					dfs(e.getDestination(),marked,dfsNode);

		}
	}
}
