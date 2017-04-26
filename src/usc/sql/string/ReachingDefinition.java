package usc.sql.string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocalBox;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class ReachingDefinition {

	private Set<NodeInterface> allNode;
	private List<NodeInterface> topoNode;
	private Set<EdgeInterface> allEdge;
//	private Set<ReachingDefSet> allRNode = new HashSet<ReachingDefSet>();
	private Map<NodeInterface,ReachingDefSet> reachingDefMap = new HashMap<>();
	private Map<String,String> allDef = new HashMap<>();
	public ReachingDefinition(Set<NodeInterface> allNode, Set<EdgeInterface> allEdge,List<EdgeInterface> backedge) {
		this.allNode = allNode;
		this.allEdge = allEdge;
		// this.allNode=allNode;
		for (NodeInterface n : allNode) {
			ReachingDefSet rds = new ReachingDefSet();
			reachingDefMap.put(n, rds);
		//	allRNode.add(new ReachingDefSet(n));
		}
		topoNode = topoSort(allNode,backedge);
		computeReachingDefinition();
	}

	private void initialize() {
		for (NodeInterface n : allNode)
		{
			Def v = new Def(n);
			if(v.getVarName()!=null)
			{
				reachingDefMap.get(n).getGenSet().add(v);
				allDef.put(n.getOffset().toString(), v.getVarName());
			//System.out.println("olalala "+v.getPosition()+v.getVarName());
			}
		//	rn.getGenSet().add(new Variable(rn.getNode()));
		//	rn.getOutSet().add(rn.getNode());
		}
	}
	public List<NodeInterface> topoSort(Set<NodeInterface> allNode,List<EdgeInterface> backedge)
	{
		NodeInterface entry = null;
		Map<NodeInterface,List<EdgeInterface>> inEdgeMap = new HashMap<>();
		for(NodeInterface n:allNode)
		{
			if(n.getInEdges().isEmpty())
				entry = n;
			else
			{
				List<EdgeInterface> inEdge = new ArrayList<>();
				for(EdgeInterface e:n.getInEdges())
				{
					if(!backedge.contains(e))
					inEdge.add(e);
				}
				inEdgeMap.put(n, inEdge);
			}
		}
		//L ← Empty list that will contain the sorted elements
		List<NodeInterface> L = new ArrayList<>();
		//S ← Set of all nodes with no incoming edges
		Queue<NodeInterface> S = new LinkedList<>();
		
		S.add(entry);
		while(!S.isEmpty())
		{
			NodeInterface n = S.poll();
			L.add(n);
			for(EdgeInterface e:n.getOutEdges())
			{
				if(!backedge.contains(e))
				{
					inEdgeMap.get(e.getDestination()).remove(e);
				
				if(inEdgeMap.get(e.getDestination()).isEmpty())
					S.add(e.getDestination());
				}
			}
		}
		return L;
	}
	public void computeReachingDefinition() {
		
		initialize();

		int count = 0;
		
		boolean change = true;

		while (change) {
			change = false;
			count++;
			for (NodeInterface n : topoNode) {
				// Union the out set of all the nodes in the predecessor
				// list，add to the in set of node n
				for(EdgeInterface e:n.getInEdges())
				{
					for (Def outnode : reachingDefMap.get(e.getSource()).getOutSet())
						if (!reachingDefMap.get(n).getInSet().contains(outnode))
							reachingDefMap.get(n).getInSet().add(outnode);
				}
				// Union the gen set with (in set - kill set) to the out set of node rn
				
				if(!reachingDefMap.get(n).getGenSet().isEmpty())
					for (Def gennode : reachingDefMap.get(n).getGenSet())
						if (!reachingDefMap.get(n).getOutSet().contains(gennode))
						{
							change = true;
							reachingDefMap.get(n).getOutSet().add(gennode);
						}
						
				
				if(reachingDefMap.get(n).getGenSet().isEmpty())
				{	for(Def innode : reachingDefMap.get(n).getInSet())
						if (!reachingDefMap.get(n).getOutSet().contains(innode))
						{
							change = true;
							reachingDefMap.get(n).getOutSet().add(innode);
						}
				}
				else
				{
					String name = reachingDefMap.get(n).getGenSet().iterator().next().getVarName();
					for (Def innode : reachingDefMap.get(n).getInSet())
						if (!reachingDefMap.get(n).getOutSet().contains(innode)&&!innode.getVarName().equals(name))
						{
							change = true;
							reachingDefMap.get(n).getOutSet().add(innode);
						}
				}

			}
		}
		//System.out.println(count);
	}

	
	public void outputToConsole()
	{
		for(NodeInterface n:allNode)
		{
			System.out.println(n.getOffset()+" "+((Node)n).getActualNode());
			System.out.print("In Set:");
			for(Def v:reachingDefMap.get(n).getInSet())
				System.out.print(v.getPosition()+ " " +v.getVarName()+"   ");
			System.out.println("");
			System.out.print("Out Set: ");
			for(Def v:reachingDefMap.get(n).getOutSet())
				System.out.print(v.getPosition()+ " " +v.getVarName()+"   ");
			System.out.println("");
			
			
		}
	}
	
	public Map<String,String> getAllDef()
	{
		return allDef;
	}
	
	public Map<String,String> getInSet(NodeInterface n)
	{
		Map<String,String> lineVarName = new HashMap<>();
		for(Def v:reachingDefMap.get(n).getInSet()) 
		{
			lineVarName.put(v.getPosition(), v.getVarName());			
		}
		return lineVarName;
	}
	
	//No array
	//return the line number where the variable is defined
	
	/*
	public List<String> getLineNumForUse(NodeInterface n,String varName)
	{
		List<String> line = new ArrayList<>();
	    if(reachingDefMap.get(n)==null)
	    {
	    	return line;
	    }
	    
		
		
		for(Def v:reachingDefMap.get(n).getInSet())
			if(v.getVarName().equals(varName))
				line.add(v.getPosition());
		return line;
				
	}
	*/
	
	
	//With array
	
	public List<String> getLineNumForUse(NodeInterface n,String varName)
	{
		if(varName.contains("["))
			varName = varName.substring(0,varName.indexOf("["));
		List<String> line = new ArrayList<>();
		
		if(reachingDefMap.get(n)==null)
			return line;
		for(Def v:reachingDefMap.get(n).getInSet())
		{
			String defVarName;
			if(v.getVarName().contains("["))
				defVarName = v.getVarName().substring(0,v.getVarName().indexOf("["));
			else
				defVarName = v.getVarName();
				
			if(defVarName.equals(varName))
				line.add(v.getPosition());
		}
				
		return line;
			
	}
	
	public Map<String,String> getOutSet(NodeInterface n)
	{
		Map<String,String> lineVarName = new HashMap<>();
		for(Def v:reachingDefMap.get(n).getOutSet()) 
		{
			lineVarName.put(v.getPosition(), v.getVarName());			
		}
		return lineVarName;
	}
	
	public Set<String> getUltimateLineNumForUse(CFGInterface cfg,Map<NodeInterface,String> NodeToVarName)
	{
		
		Set<String> line = new HashSet<>();
		Map<NodeInterface,String> varUse = new HashMap<>();
		for(Entry<NodeInterface,String> p:NodeToVarName.entrySet())
		{
			NodeInterface n = p.getKey();
			String varName = p.getValue();
		
		    if(reachingDefMap.get(n)==null)
		    {
		    	System.err.println("Node is not found.");
		    	
		    }
		  
	
		    for(Def v:reachingDefMap.get(n).getInSet())
		    {
		    	if(v.getVarName().equals(varName))
		    	{
		
		    		NodeInterface newNode = cfg.getNodeFromOffset(Integer.parseInt(v.getPosition()));
		    		Stmt u = (Stmt) ((Node)newNode).getActualNode();
		    	
		    		
		    		if(u instanceof AssignStmt)
		    		{
		    			//r1 = r2, etc
		    			if(u.toString().matches("(.r|r|i|b|z|d|l)([0-9]*)(.*) = (.r|r|i|b|z|d|l)([0-9]*)(.*)"))
		    			{	
		    				System.out.println("Pattern match: "+ u.toString());
		    				if(u.containsInvokeExpr())
		    					line.add(v.getPosition());
		    				else
		    					varUse.put(newNode,((AssignStmt)u).getRightOp().toString());
		    			}
		    			/*
		    			if(u.toString().matches("(.r|r)([0-9]*)(.*) = (.r|r)([0-9]*)(.*)"))
		    			{		
		    				//System.out.println("Pattern match: "+ u.toString());
		    				varUse.put(newNode,((AssignStmt)u).getRightOp().toString());
		    			}
		    			*/
		    			else
		    			{
				    		if(u.containsInvokeExpr())
				    		{
				    			String sig = u.getInvokeExpr().getMethod().getSignature();
				    			//i1 = java.lang.Integer.valueOf(i2) is transformed to i1 = i2
				    			if(sig.contains("java.lang.")&&sig.contains("valueOf("))
				    			{
				    				Value arg = u.getInvokeExpr().getArg(0);
				    				if(arg instanceof Constant)
				    					line.add(v.getPosition());
				    				else
				    					varUse.put(newNode,arg.toString());
				    			}
				    			else
				    				line.add(v.getPosition());
				    		}
				    		else
				    			line.add(v.getPosition());
		    			}
		    		}
		    		else if(u instanceof IdentityStmt)
		    		{
		    			
		    			if(((IdentityStmt)u).getRightOp().toString().contains("@parameter"))
		    				line.add("-"+((IdentityStmt)u).getRightOp().toString().split(":")[0].replaceAll("@parameter", "")+":"+cfg.getSignature());
		    		}

		    		
		      			
		    	}
		    }

		   
		}
		if(!varUse.isEmpty())
		    line.addAll(getUltimateLineNumForUse(cfg, varUse));
		return line;
	}
	public String toDot() {
		StringBuilder dotGraph = new StringBuilder();
		dotGraph.append("digraph directed_graph {\n\tlabel=\"" + "DU Chain" + "\";\n");
		dotGraph.append("\tlabelloc=t;\n");
		
		for(NodeInterface n:allNode)
			dotGraph.append("\t"+n.getName()+" [];\n");
		for(NodeInterface n:allNode)
		{
			Unit jimple = ((Node<Unit>)n).getActualNode();
			if(jimple!=null&&jimple.getUseBoxes()!=null)
			{
				//System.out.println(jimple);
				//boolean isReported = false;
				for(ValueBox vb:jimple.getUseBoxes())
				{
						String use = vb.getValue().toString();
						System.out.println(n.getName()+" "+use+getLineNumForUse(n,use));
					for(String lineDef:getLineNumForUse(n,use))
					{
						dotGraph.append("\t"+lineDef+" -> "+n.getName()+"[label=\""+use+"\"];\n");
					}
				
				}
			}
		}
		dotGraph.append("}\n");
		return dotGraph.toString();
	}
}
class Def{
	public String getVarName() {
		return varName;
	}
	public String getPosition() {
		return position;
	}
	private String varName;
	private String position;
	public Def(NodeInterface n)
	{
		//System.out.println(n.toString());
		position = n.getOffset().toString();
		varName = interpret(n);
	} 
	private String interpret(NodeInterface n)
	{
		Unit temp = ((Node<Unit>)n).getActualNode();
		if(temp==null)
			return null;
		else
		{			
			if(temp.toString().contains("specialinvoke"))
			{
				if(temp.toString().contains(".<java.lang.StringBuffer: void <init>(java.lang.String)")
				||temp.toString().contains(".<java.lang.StringBuilder: void <init>(java.lang.String)>")||temp.toString().contains(".<java.lang.StringBuilder: void <init>(java.lang.CharSequence)")
				||temp.toString().contains(".<java.lang.String: void <init>(java.lang.String)>")||temp.toString().contains(".<java.lang.String: void <init>(java.lang.StringBuilder)>")||temp.toString().contains(".<java.lang.String: void <init>(java.lang.StringBuffer)>"))
				{
					if(temp.getUseBoxes().size()>1)
					return temp.getUseBoxes().get(1).getValue().toString();
					else
						return null;
				}
					//return null;
				else if(temp.toString().contains(".<java.lang.StringBuilder: void <init>()>")||temp.toString().contains(".<java.lang.String: void <init>()>")||temp.toString().contains(".<java.lang.StringBuffer: void <init>()>"))
				{
					return temp.getUseBoxes().get(0).getValue().toString();
				}
				else
				{
					if(temp.getDefBoxes().isEmpty())
					{
					
					//System.out.println("rd: "+ temp);
					return null;
					}
					else
						return temp.getDefBoxes().get(0).getValue().toString();
				}
				
			}
			else
			{
				if(temp.toString().contains("virtualinvoke")&&temp.getDefBoxes().isEmpty()&&(temp.toString().contains("java.lang.StringBuffer: java.lang.StringBuffer")||temp.toString().contains("java.lang.StringBuilder: java.lang.StringBuilder")))
				{
					String defname = null;
					for(ValueBox vb:temp.getUseBoxes())
					{
						if(vb instanceof JimpleLocalBox)
						{
							defname = vb.getValue().toString();
							break;
						}
					}
					return defname;
					
				}
				else
				{
					if(temp.getDefBoxes().isEmpty())
						return null;
					else
					{
												
						if(temp.getDefBoxes().get(0).getValue() instanceof FieldRef)
							return ((FieldRef)temp.getDefBoxes().get(0).getValue()).getField().getSignature();
						else
							return temp.getDefBoxes().get(0).getValue().toString();
					}
				}
			}
				
		}
	}
}

 class ReachingDefSet {
	private Set<Def> inSet = new HashSet<>();
	private Set<Def> outSet = new HashSet<>();
	private Set<Def> genSet = new HashSet<>();

	public Set<Def> getInSet() {
		return inSet;
	}

	public void setInSet(Set<Def> inSet) {
		this.inSet = inSet;
	}

	public Set<Def> getOutSet() {
		return outSet;
	}

	public void setOutSet(Set<Def> outSet) {
		this.outSet = outSet;
	}

	public Set<Def> getGenSet() {
		return genSet;
	}

	public void setGenSet(Set<Def> genSet) {
		this.genSet = genSet;
	}

}