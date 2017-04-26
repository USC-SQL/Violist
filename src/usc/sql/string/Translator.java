package usc.sql.string;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import SootEvironment.JavaApp;
import soot.ValueBox;
import usc.sql.ir.*;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.NodeInterface;

public class Translator {
	//private List<NodeInterface> allNode;
	private EdgeInterface backedge;
	private ReachingDefinition rd;
	//private InterRe ir;
	private LayerRegion lr;
	private Map<String,ArrayList<Variable>> newUseMap = new HashMap<>();
	private Map<String,ArrayList<Variable>> newUseMapLoopPrevious = new HashMap<>();
	private Map<String,ArrayList<Variable>> newUseMapLoop = new HashMap<>();
	//private int lineRegionNum;
	private Map<RegionNode, Boolean> markedforRTree = new HashMap<>();
	private Map<String,NodeInterface> lineNodeMap = new HashMap<>();
	private List<EdgeInterface> backedges = new ArrayList<>();
	private List<String> visited;
	private String methodName;
	private String folderName;
	private Map<String,List<Integer>> targetSignature;
	private int targetParaOffset;
	private Map<NodeInterface,List<String>> targetVarNodeAndName = new HashMap<>();
	private Map<String,Set<NodeInterface>> paraMap = new HashMap<>();
	private Map<String,Set<String>> fieldMap = new HashMap<>();
	private Map<String,String> labelConst = new HashMap<>();
	private int targetCount = 0;
	private JavaApp App;
	public Translator(ReachingDefinition rd, LayerRegion lr, String methodName, String folderName,Map<String,List<Integer>> targetSignature)
	{
		//allNode = rn.getNodeList();
		//backedge = rn.getBackEdge();
		this.rd = rd;
		//this.ir = ir;
		this.lr = lr;
		this.methodName = methodName;
		this.folderName = folderName;
		this.targetSignature = targetSignature;

		
		RegionNode root = lr.getRoot();
		for (RegionNode rn : lr.getAllRegionNode()) {
			markedforRTree.put(rn, false);
			backedges.add(rn.getBackEdge());
		}	
		dfsRegionTree(root);
		
	}
	
	public Translator(ReachingDefinition rd, LayerRegion lr, String methodName, String folderName,JavaApp App)
	{
		//allNode = rn.getNodeList();
		//backedge = rn.getBackEdge();
		this.rd = rd;
		//this.ir = ir;
		this.lr = lr;
		this.methodName = methodName;
		this.folderName = folderName;
		this.App = App;
		RegionNode root = lr.getRoot();
		for (RegionNode rn : lr.getAllRegionNode()) {
			markedforRTree.put(rn, false);
			backedges.add(rn.getBackEdge());
		}	

		dfsRegionTree(root);
		
	}
	public Translator(ReachingDefinition rd, LayerRegion lr, String methodName, String folderName)
	{
		//allNode = rn.getNodeList();
		//backedge = rn.getBackEdge();
		this.rd = rd;
		//this.ir = ir;
		this.lr = lr;
		this.methodName = methodName;
		this.folderName = folderName;
		this.App = App;
		RegionNode root = lr.getRoot();
		for (RegionNode rn : lr.getAllRegionNode()) {
			markedforRTree.put(rn, false);
			backedges.add(rn.getBackEdge());
		}	
	
		dfsRegionTree(root);
		
	}
	private void dfsRegionTree(RegionNode root)
	{
		markedforRTree.put(root, true);
		
		
		for (RegionNode child : root.getChildren()) {
			if (!markedforRTree.get(child))
			{
				
				dfsRegionTree(child);
				//if(child.getChildren().isEmpty())
				//mergedMapFromChildRegionNode.clear();
				//merge 

			}					
	
		}	
	//	System.out.println("");
	//	System.out.println("Region Number: "+root.getRegionNumber()+" number of children: "+ root.getChildren().size());
		Map<String,ArrayList<Variable>> mergedMapFromChildRegionNode = new HashMap<>();
			for(RegionNode childrenNode:root.getChildren())
			{			

				//if(childrenNode!=null)
					for(Entry<String,ArrayList<Variable>> en:childrenNode.getUseMap().entrySet())
							mergedMapFromChildRegionNode.put(en.getKey(), en.getValue());
			}
		
			run(root,mergedMapFromChildRegionNode);		
	
		
		
	}
	private void run(RegionNode rn,Map<String,ArrayList<Variable>> mapFromChildRegionNode)
	{		
		
		List<NodeInterface> allNode = rn.getNodeList();
		EdgeInterface backedge = rn.getBackEdge();
		
		NodeInterface entry = null;
		if(backedge==null)
		{
			
	
			for(NodeInterface n:allNode)
			{	if(n.getInEdges().isEmpty())
				{
					entry = n;
					break;
				}
			}
			//return;
		}
		else
			entry = backedge.getDestination();
		
		
	//	System.out.println("previous map size: "+mapFromChildRegionNode.keySet().size());
		newUseMap = new HashMap<>() ;
		newUseMapLoopPrevious = new HashMap<>();
		newUseMapLoop = new HashMap<>();

		int lineRegionNum = rn.getRegionNumber();
		
		//the name of the variables that are define in the region
		Map<String,Boolean> regionDef = new HashMap<>();
		for(String s:rd.getAllDef().values())
		{
	
			regionDef.put(s, false);
		}
		
		//the lines that are defining variables
		List<String> lineDef = new ArrayList<>();
		
		
		
		for(NodeInterface n:rn.getNodeList())
		{
			if(rd.getAllDef().keySet().contains(n.getOffset().toString()))
			{
				regionDef.put(rd.getAllDef().get(n.getOffset().toString()), true);
				lineDef.add(n.getOffset().toString());
			}
				
		}
		//the lines that are defining variables in the current region instead of is sub region 
		List<String> specialLineDef = new ArrayList<>(lineDef);
		for(RegionNode children: rn.getChildren())
		{
			for(NodeInterface cn: children.getNodeList())
				if(rd.getAllDef().keySet().contains(cn.getOffset().toString()))
					specialLineDef.remove(cn.getOffset().toString());
		}
		
		InterRe ir = new InterRe(rn.getNodeList(),regionDef,lineDef,folderName,targetVarNodeAndName,paraMap,fieldMap,targetCount, methodName, App, targetSignature);
		

		
		if(entry==null)
			System.out.println("Entry Null");
	
		List<NodeInterface> tp = topoSort(allNode, entry, backedges);

		visited = new ArrayList<>();
	
		//before
		//Update information from children node, setExternalToInternal, and substitute Init
	    Map<String,ArrayList<Variable>> modifiedUseMap = new HashMap<>();
	    for(Entry<String,Variable> en: ir.getUseMap().entrySet())
	    {
	    	ArrayList<Variable> temp = new ArrayList<>();
	    	temp.add(en.getValue());
	    	modifiedUseMap.put(en.getKey(), temp);
	    }
	    for(Entry<String,ArrayList<Variable>> en: ir.getUseMapFromOtherMethod().entrySet())
	    {
	    	modifiedUseMap.put(en.getKey(),en.getValue());
	    }

	    if(!mapFromChildRegionNode.isEmpty())
	    	for(Entry<String,ArrayList<Variable>> en:mapFromChildRegionNode.entrySet())
	    	{
	    	
	    		modifiedUseMap.put(en.getKey(), setExternalToInternal(regionDef, en.getValue()));
	    	}


		int k = 1;
		for(NodeInterface n: tp)
		{
			
			if(modifiedUseMap.keySet().contains(n.getOffset().toString()))		
			{
				
				//System.out.println("old: "+n.getOffset().toString()+" "+rd.getAllDef().get(n.getOffset().toString())+" = "+modifiedUseMap.get(n.getOffset().toString()).toString());
		
				modifiedUseMap.put(n.getOffset().toString(), expandBeforeBefore(n,modifiedUseMap.get(n.getOffset().toString()), lineDef,specialLineDef));
				//System.out.println("new : "+n.getOffset().toString()+" "+rd.getAllDef().get(n.getOffset().toString())+" = "+modifiedUseMap.get(n.getOffset().toString()).toString());

				if(specialLineDef.contains(n.getOffset().toString()))
					newUseMap.put(n.getOffset().toString(), expandBefore(n,modifiedUseMap.get(n.getOffset().toString()),specialLineDef,modifiedUseMap));
				else
					newUseMap.put(n.getOffset().toString(), modifiedUseMap.get(n.getOffset().toString()));
				
			}
			visited.add(n.getOffset().toString());
		}
		

		for(Entry<String,ArrayList<Variable>> en:newUseMap.entrySet())
		{
			
			newUseMapLoopPrevious.put(en.getKey(), copyVar(en.getValue()));	
			for(Variable tempV:newUseMapLoopPrevious.get(en.getKey()))
			{
				buildParentRelationship(tempV);
			}
		}

				//after
		//System.out.println();
		for(NodeInterface n: tp)		
			if(newUseMapLoopPrevious.keySet().contains(n.getOffset().toString()))					
			{
			
				ArrayList<Variable> vlist = new ArrayList<>();
				for(Variable v:newUseMapLoopPrevious.get(n.getOffset().toString()))
				{
					
					if(shouldAddT(n.getOffset().toString(),v,rn.getRegionNumber()))
					{
					
						Variable t = new T(v,rd.getAllDef().get(n.getOffset().toString()),lineRegionNum,n.getOffset().toString());
						v.setParent(t);
						//newUseMapLoopPrevious.put(n.getName(), t);
						vlist.add(t);					
					}
					else
						vlist.add(v);
				}
				newUseMapLoopPrevious.put(n.getOffset().toString(), vlist);	
				
			//	System.out.println(n.getOffset().toString()+" "+rd.getAllDef().get(n.getOffset().toString())+" = "+newUseMapLoopPrevious.get(n.getOffset().toString()).toString());
			}

		boolean changed = true;
		//k = k*2;
		while(changed)
		{
			changed = false;
			//Map<Variable,String> tempMap = new HashMap<>();

			for(NodeInterface n: tp)
			{				
				    //!! seems that after the first step, we no longer need to analyze the temporary variables  
					if(newUseMapLoopPrevious.keySet().contains(n.getOffset().toString()))  //&&!rd.getAllDef().get(n.getName()).contains("$")
					{   
						//	continue;
						ArrayList<Variable> vlist = new ArrayList<>();
						for(Variable v :newUseMapLoopPrevious.get(n.getOffset().toString()))
						{
							
							if(rd.getAllDef().get(n.getOffset().toString()).startsWith("$"))
							{
								vlist.add(v);
								continue;
							}
							for(Variable exp : expand(n,v,k,rn.getRegionNumber()))
							{
								//Variable exp = copyVar(exptmp);
								buildParentRelationship(exp);

								if(shouldAddT(n.getOffset().toString(),exp,rn.getRegionNumber()))
								{
									Variable t = new T(exp,rd.getAllDef().get(n.getOffset().toString()),lineRegionNum,n.getOffset().toString());
									exp.setParent(t);
									vlist.add(t);			
								}
								else
									vlist.add(exp);
							}
						}
						for(Variable v: vlist)
						{
							changeTtoF(v);
						}
						newUseMapLoop.put(n.getOffset().toString(), vlist);	
					}				
			}

		//	System.out.println();
			for(NodeInterface n: tp)		
			{	if(newUseMapLoop.keySet().contains(n.getOffset().toString()))					
				{
					//System.out.println(n.getOffset().toString()+" "+rd.getAllDef().get(n.getOffset().toString())+" = "+newUseMapLoop.get(n.getOffset().toString()).toString());
					
					int lengthPrevious = newUseMapLoopPrevious.get(n.getOffset().toString()).toString().length();
					int lengthNew = newUseMapLoop.get(n.getOffset().toString()).toString().length();
					if(lengthPrevious!=lengthNew)
					{
						newUseMapLoopPrevious.put(n.getOffset().toString(),newUseMapLoop.get(n.getOffset().toString()));
						changed = true;
					}
					
					
				}
			}
			
			k = k*2;
			
			
			//if it is stable, we expand the init variable 
			if(!changed)
			{
				
				for(NodeInterface n: tp)		
					
				{	
					
					if(newUseMapLoop.keySet().contains(n.getOffset().toString()))					
					{
						newUseMapLoop.put(n.getOffset().toString(), copyVar(newUseMapLoop.get(n.getOffset().toString())));
					}
				}
				List<String> newUseMapLoopOutputPrevious = new ArrayList<>();
				for(NodeInterface n: tp)							
				{						
					if(newUseMapLoop.keySet().contains(n.getOffset().toString()))					
					{
					
						for(Variable var:newUseMapLoop.get(n.getOffset().toString()))
						{
					
								
							expandInit(var,lineDef,n);
						}
							newUseMapLoopOutputPrevious.add(newUseMapLoop.get(n.getOffset().toString()).toString());
					}
				}
				boolean initchanged = true;
				while(initchanged)
				{
					initchanged = false;
					List<String> newUseMapLoopOutput = new ArrayList<>();
				
					for(NodeInterface n: tp)							
					{						
						if(newUseMapLoop.keySet().contains(n.getOffset().toString()))					
						{
						
								for(Variable var:newUseMapLoop.get(n.getOffset().toString()))
							{
							
									
		
								expandInit(var,lineDef,n);
							}
						newUseMapLoopOutput.add(newUseMapLoop.get(n.getOffset().toString()).toString());
						}
						

					}
					
					//compare output, if same, break, else, copy value of output to previous
					for(int i = 0; i < newUseMapLoopOutput.size();i++)
					{
						if(!newUseMapLoopOutput.get(i).equals(newUseMapLoopOutputPrevious.get(i)))
							initchanged = true;
					}
					if(initchanged)
					{
						newUseMapLoopOutputPrevious.clear();
						newUseMapLoopOutputPrevious.addAll(newUseMapLoopOutput);
					}
					
				
				}
		    // we dump the translated IR of the return variable to a file
		
				
				List<Variable> returnVar = new ArrayList<>();
			
				for(Entry<NodeInterface,Variable> returnNodeAndName:ir.getReturnVar().entrySet())
				{
					if(returnNodeAndName.getValue() instanceof InternalVar)
					{
						for(String line:rd.getLineNumForUse(returnNodeAndName.getKey(), ((InternalVar)returnNodeAndName.getValue()).getName()))
						{
							if(newUseMapLoop.get(line)==null)
								returnVar.add(new ExternalPara("Unknown@USENULL"));
							else
								returnVar.addAll(newUseMapLoop.get(line));
						}
					}
					else
						returnVar.add(returnNodeAndName.getValue());
					
				}
				
				try {
					int id=Encoder.add(methodName);
					
					FileOutputStream fout = new FileOutputStream(folderName+id+".ser");
					ObjectOutputStream oos = new ObjectOutputStream(fout);
					for(Variable v: returnVar)
					{
						
						oos.writeObject(v);
						oos.reset();
					
					}
					oos.close();
				}
				catch (IOException e) {				
					e.printStackTrace();
				}

			}
		}
		
		
		
		
		//System.out.println("");
		for(NodeInterface n: tp)		
		{	if(newUseMapLoop.keySet().contains(n.getOffset().toString()))					
			{
			
				//if(n.getOffset().toString().equals("17"))
				//	continue;
				//System.out.println("final:"+n.getOffset().toString()+" "+rd.getAllDef().get(n.getOffset().toString())+" = "+newUseMapLoop.get(n.getOffset().toString()).toString());
				
			}
		}
		
		for(ArrayList<Variable> list: newUseMapLoop.values())
		{
			for(Variable tempV: list)
				buildParentRelationship(tempV);
		}
		rn.setUseMap(newUseMapLoop);
		
		
	}
	
	
	private void expandInit(Variable v,List<String> lineDef, NodeInterface n)
	{
		//!!!Note: There are two situations about Init, 1. the Init is in the IR summary of another method, so the initVar is not empty at the first place, and line number is not the line number of current method
		// 2. the Init is defined in the current method
		if(v instanceof Init)
		{
			 
			if(!((Init) v).getInitVar().isEmpty())
			{
				
				List<Variable> initVar = new ArrayList<>();
				for(Variable init: ((Init) v).getInitVar())
				{
					
					if(init instanceof InternalVar)
					{
						/*
						if(((InternalVar) init).getSigma())
							continue;
						else
						{
							initVar.addAll(newUseMapLoopPrevious.get(((InternalVar) init).getLine()));
							
						}*/
						
						for(String line:rd.getLineNumForUse(n, ((InternalVar)init).getName()))
							if(newUseMapLoopPrevious.containsKey(line))
							{
								initVar.addAll(newUseMapLoopPrevious.get(line));
							}
						
						
					}
					else if(init instanceof T)
					{
						expandInit(init,lineDef,n);
					
						initVar.add(init);
					}
					else
						initVar.add(init);
				}
				((Init) v).setInitVar(initVar);
			//	((Init) v).setInitVar(expandBefore(n,((Init) v).getInitVar(),lineDef,modifiedUseMap));
				
			}
			else
			{
				
				if(lineDef.contains(((Init) v).getLine()))
				{
					///!!!!!!!!!!!!!!!! if line and varname is the same, dont do it
					if(rd.getAllDef().get(n.getOffset().toString()).equals(((Init) v).getName())&&n.getOffset().toString().equals(((Init) v).getLine()))
					{
					//	System.out.println("BBBA"+n.getOffset().toString() +((Init) v).getLine());
						
						((Init) v).setName("σσ "+((Init) v).getName());
						
					}
					else
					{//((Init) v).setInitVar(copyVar( newUseMapLoopPrevious.get(((Init) v).getLine())));
					}
				}	
			}
			
		}
		else if(v instanceof Expression)
		{
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
		
				for(Variable operand:operandList)
				{
					expandInit(operand,lineDef,n);
				}
		
			}

		}
		else if(v instanceof T)
		{
			expandInit(((T) v).getVariable(),lineDef,n);
			
		}

	}
	private ArrayList<Variable> expandBeforeBefore(NodeInterface n,List<Variable> varListInUseMap,List<String> lineDefAll,List<String> lineDef)
	{
		ArrayList<Variable> varList = new ArrayList<>();
		for(Variable v:varListInUseMap)
		{
			if(v instanceof InternalVar)
			{
				if(((InternalVar) v).getSigma()==false)
				{
					
					if(((InternalVar) v).getLine()==null)
					{

						
						for(String line:rd.getLineNumForUse(n, ((InternalVar)v).getName()))
						{
						/*
						if(lineDef.contains(line))
							varList.add(new InternalVar(((InternalVar)v).getName(),line));
						else if(lineDefAll.contains(line))
						{
							varList.add(new InternalVar(((InternalVar) v).getName(),line));
						}*/
							varList.add(new InternalVar(((InternalVar) v).getName(),line));
							
						}
					}

					else
						varList.add(v);
				}
				else
					varList.add(v);
			}
			else if(v instanceof Expression)
			{				
				List<List<Variable>> newOperandList = new ArrayList<>();
				for(List<Variable> operandList: ((Expression) v).getOperands())
					newOperandList.add(expandBeforeBefore(n, operandList,lineDefAll,lineDef));
				((Expression) v).setOperands(newOperandList);
				varList.add(v);
			}
			else if(v instanceof T)
			{
				List<Variable> l = new ArrayList<>();
				l.add(((T) v).getVariable());
				//there should be only one item in expandBefore(n,l)
				((T) v).setVariable(expandBeforeBefore(n,l,lineDefAll,lineDef).get(0));
				varList.add(v);
			}
			else
				varList.add(v);
		}
		return varList;
		
	}
	private ArrayList<Variable> expandBefore(NodeInterface n,List<Variable> varListInUseMap,List<String> lineDef, Map<String, ArrayList<Variable>> modifiedUseMap )
	{
		ArrayList<Variable> varList = new ArrayList<>();
		for(Variable v:varListInUseMap)
		{
			if(v instanceof InternalVar)
			{
				if(((InternalVar) v).getSigma())
					varList.add(v);
				else
				{
					boolean newlyDefine = false;
					
				//	for(String line:rd.getLineNumForUse(n, ((InternalVar)v).getName()))
					//{
						String line = ((InternalVar) v).getLine();
						if(visited.contains(line))
						{	//if(n.getOffset().toString().equals(line))
							
							
							if(newUseMap.get(line)!=null)
							{
								for(Variable tempVar:newUseMap.get(line))
								{
									//!!!! There may be duplicate terms 
									varList.add(tempVar);
									
									if(tempVar instanceof ExternalPara)
									{	
										ExternalPara field = (ExternalPara) tempVar;
										if(field.getName().matches("<.*>"))
										{
											if(fieldMap.get(rd.getAllDef().get(n.getOffset().toString()))==null)
											{
												Set<String> s = new HashSet<>();
												s.add(field.getName());
												fieldMap.put(rd.getAllDef().get(n.getOffset().toString()), s);
											}
											else
											fieldMap.get(rd.getAllDef().get(n.getOffset().toString())).add(field.getName());
										}
										
									}
								}
							}
							else
								varList.add(new ExternalPara("Unknown@USENULL"));
							
							newlyDefine = true;
						}
						
						else
						{ 
							if(!lineDef.contains(line))
							{	varList.add(v);
							
							newlyDefine = true;
							}
							
							//if(!lineDef.contains(line))
							//	varList.add(new Init(((InternalVar)v).getName(),line));
						
						
						}
					//}
					if(!newlyDefine)
					{						
						((InternalVar)v).setK(1);
						varList.add(v);					
					}
				}
			}
			else if(v instanceof Expression)
			{				
				List<List<Variable>> newOperandList = new ArrayList<>();
				for(List<Variable> operandList: ((Expression) v).getOperands())
					newOperandList.add(expandBefore(n, operandList,lineDef, modifiedUseMap));
				((Expression) v).setOperands(newOperandList);
				varList.add(v);
			}
			else if(v instanceof T)
			{
				List<Variable> l = new ArrayList<>();
				l.add(((T) v).getVariable());
				//there should be only one item in expandBefore(n,l)
				((T) v).setVariable(expandBefore(n,l,lineDef, modifiedUseMap).get(0));
				varList.add(v);
			}
			
			else
			{
				varList.add(v);
			}
		}
		return varList;
	}
	
	//Expand the right hand side of the statement
	//Warning: there may be an error here.I assume getLineNumForUse will return the same result with parameters (n,varName) and with parameters(successor of n, varName)
	private ArrayList<Variable> expand(NodeInterface n,Variable v,int k,int regionNum)
	{
		if(v instanceof InternalVar)
		{
			if(((InternalVar) v).getSigma())
			{
				Variable current = copyVar(v);
				//addK(current,k);
				ArrayList<Variable> c = new ArrayList<>();
				c.add(current);
				return c;
			}
			else
			{			
				ArrayList<Variable> tempList = new ArrayList<>();
				if(newUseMapLoopPrevious.keySet().contains(((InternalVar) v).getLine()))
				{
					for(Variable temp : newUseMapLoopPrevious.get(((InternalVar) v).getLine()))
					{
						
						Variable current = copyVar(temp);

						if(current instanceof T)
						{
							((T) current).setK(((T) current).getK()-((InternalVar) v).getK());
							modifyChildrenFiK(((T) current).getVariable(),((InternalVar) v).getK());
						}
				    
						buildParentRelationship(current);
						addK(current,k,regionNum);
						tempList.add(current);
					}
				}
				else
					tempList.add(copyVar(v));
				/*
				for(String line:rd.getLineNumForUse(n, ((InternalVar)v).getName()))
				{					
					if(newUseMapLoopPrevious.keySet().contains(line))
					{
						
						for(Variable temp : newUseMapLoopPrevious.get(line))
						{
							Variable current = copyVar(temp);
							if(current instanceof T)
							{
								((T) current).setK(((T) current).getK()-((InternalVar) v).getK());
							}
					    
							buildParentRelationship(current);
							addK(current,k,regionNum);
							tempList.add(current);
					
						}
										
					}

				}
				*/
				return tempList;	
			}
			//return null;
		}
		else if(v instanceof Expression)
		{
			String defVarName = rd.getAllDef().get(n.getOffset().toString());
			
			List<List<Variable>> newOperandList = new ArrayList<>();
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
				List<Variable> tempOp = new ArrayList<>();
				for(Variable operand: operandList)
				{
					if(operand instanceof InternalVar)
					{
						if(((InternalVar) operand).getName().equals(defVarName)&&((InternalVar) operand).getLine().equals(n.getOffset().toString()))
						{
						
							tempOp.add(operand);
						}
						else
						{
							for(Variable exp:expand(n,operand,k,regionNum))
								tempOp.add(exp);
						}
					}
					else
					{
						for(Variable exp:expand(n,operand,k,regionNum))
							tempOp.add(exp);
					}
				}
				newOperandList.add(tempOp);
			}
			Expression e = new Expression(newOperandList,((Expression) v).getOperation());
			//((Expression) v).setOperands(tempOp);
			
			ArrayList<Variable> elist = new ArrayList<>();
			elist.add(e);
			return elist;			
		}
		else if(v instanceof T)
		{	

			ArrayList<Variable> tlist = new ArrayList<>();
			for(Variable exp: expand(n,((T) v).getVariable(),k,regionNum))
				tlist.add(new T(exp,((T) v).getTVarName(),((T) v).getRegionNumber(),((T) v).getK(),((T) v).isFi(),((T) v).getLine()));
			
			//there should be only one item in the list
			
				try {if(tlist.size()>1)
					throw new Exception();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return tlist;
		}

		else
		{
			ArrayList<Variable> list = new ArrayList<>();
			list.add(v);
			return list;
		}
	}
	
	private void modifyChildrenFiK(Variable ir,int k)
	{
		if(ir instanceof T)
		{
			((T) ir).setK(((T) ir).getK()-k);
			modifyChildrenFiK(((T) ir).getVariable(),k);
		}
		else if(ir instanceof Expression)
		{
			for(List<Variable> operandList:((Expression) ir).getOperands())
				for(Variable operand:operandList)
					modifyChildrenFiK(operand,k);
		}
		//else if(ir instanceof InternalVar)
		//{
		//	((InternalVar) ir).setK(((InternalVar) ir).getK()+k);
		//}
			 
	}
	
	//set the parent for each variable 
	private void buildParentRelationship(Variable exp)
	{
		if(exp instanceof T)
		{
			((T) exp).getVariable().setParent(exp);
			buildParentRelationship(((T) exp).getVariable());
		}
		else if(exp instanceof Expression)
		{
			for(List<Variable> operandList:((Expression) exp).getOperands())
				for(Variable operand:operandList)
				{
					operand.setParent(exp);
					buildParentRelationship(operand);
				}
		}
			
		
	}
	
	//edit the superscript
	private void addK(Variable v, int k,int regionNum)
	{
		if(v instanceof InternalVar)
		{
			//if the variable is sigma and the corresponding regionNum is not the current regionNum, we don't perform addK
		//	if(((InternalVar) v).getSigma()==true&&((InternalVar) v).getRegionNum()!=regionNum)
			//	;//do nothing
			//else
				((InternalVar) v).setK(((InternalVar) v).getK()+k);
		}
		else if(v instanceof Expression)
		{
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
				for(Variable operand: operandList)
				{
					if(operand instanceof InternalVar)
					{
						boolean shouldAddK = true;
						//if the operand is sigma and there exist a parent that is a instance of T and its TVarName = operand.getName
						if(((InternalVar) operand).getSigma())
						{
							/*
							Variable parent = operand.getParent();
							while(parent!=null)
							{
								if(parent instanceof T)
								{	if(((T) parent).getTVarName().equals(((InternalVar) operand).getName()))
								{
									if(!((T) parent).getLine().equals(((InternalVar) operand).getLine()))
									
										shouldAddK = false;
										break;
									}
								}
								parent = parent.getParent();
							}*/
							shouldAddK = false;
						}
					
					
						if(shouldAddK)
							((InternalVar) operand).setK(((InternalVar) operand).getK()+k);
					}
					else if(operand instanceof Expression)
						addK(operand,k,regionNum);
					}
			}
		}
		else if(v instanceof T)
		{
			addK(((T) v).getVariable(),k,regionNum);
		}
	}

	//check if we should add T and set corresponding sigma of internal variables
	//after put to the newUseMapLoop, call this method
	
	/*
	private boolean shouldAddT(String line,Map<String, Variable> newUseMapLoop,int regionNum)
	{
		Variable v = newUseMapLoop.get(line);
		if(!(v instanceof InternalVar)&&!(v instanceof Expression)&&!(v instanceof T))
			return false;
		else
			return sameVarName(rd.getAllDef().get(line),v, regionNum);
	}*/
	
	private boolean shouldAddT(String line,Variable v,int regionNum)
	{
		if(!(v instanceof InternalVar)&&!(v instanceof Expression)&&!(v instanceof T))
			return false;
		else
		{
			return sameVarName(line,v, regionNum);
		}
	}
	
	//change sigma to true if necessary
	private boolean sameVarName(String line,Variable v,int regionNum)
	{
		
		boolean same = false;
		String defVarName = rd.getAllDef().get(line);
		if(v instanceof InternalVar)
		{
			//if there exist a parent that is instanceof T and its TVarName = v.getName() and its RegionNum = current regionNum
			boolean existSpecialParent = false;
			if(((InternalVar) v).getSigma()==false)
			{	
				Variable parent = v.getParent();
				while(parent!=null)
				{
					//warning: regionNum?
					//if(((InternalVar) v).getName().equals("r1"))
					
					if((parent instanceof T)&&((T)parent).getTVarName().equals(((InternalVar) v).getName())&&/*((T)parent).getRegionNumber()==regionNum&&*/((InternalVar) v).getLine().equals(((T)parent).getLine()))
					{
													
						 // System.out.println("yoyo1"+v.toString()+"aaaa "+parent.toString());
						((InternalVar) v).setSigma(true);
						
						((InternalVar) v).setRegionNum(((T)parent).getRegionNumber());
						existSpecialParent = true;
						break;
					}
					else
						parent = parent.getParent();
				}
			}
			if(existSpecialParent == false)
			{
				if(((InternalVar) v).getSigma()==false)
				{
					if(((InternalVar) v).getName().equals(defVarName)&&((InternalVar) v).getLine().equals(line))
					{

						
						((InternalVar) v).setRegionNum(regionNum);
				
						((InternalVar) v).setSigma(true);
						
						same = true;			
					}
				}
			}
		}
		else if(v instanceof Expression)
		{
			for(List<Variable> varList:((Expression) v).getOperands())
			for(Variable operand: varList)
			{
				if(sameVarName(line,operand,regionNum))
					same = true;
			}
		}
		
		else if(v instanceof T)
		{
			if(((T) v).getRegionNumber()==regionNum&&((T)v).getTVarName().equals(defVarName))
				sameVarName(line,((T) v).getVariable(),regionNum);
			else
				same = sameVarName(line,((T) v).getVariable(),regionNum);
			//setKtoZero(defVarName,v);
		}
		
		
		return same;
		
	}
	
	private void changeTtoF(Variable v)
	{
		if(v instanceof T)
			changeTtoF(((T) v).getVariable(),((T) v).getRegionNumber());
		else if(v instanceof Expression)
		{
			for(List<Variable> operandList: ((Expression) v).getOperands())
				for(Variable op: operandList)
					changeTtoF(op);
		}
			
	}
	private void changeTtoF(Variable v, int regionNumber)
	{
		if(v instanceof T)
		{
			if(((T) v).getRegionNumber()==regionNumber)
				((T) v).setFi(true);
			
			changeTtoF(((T) v).getVariable(),regionNumber);
			
		}
		else if(v instanceof Expression)
		{
			for(List<Variable> operandList: ((Expression) v).getOperands())
				for(Variable op: operandList)
					changeTtoF(op,regionNumber);
		}
	}
	private ArrayList<Variable> setExternalToInternal(Map<String,Boolean> regionDef, ArrayList<Variable> varList)
	{
		ArrayList<Variable> tempList = new ArrayList<>();
		for(Variable v:varList)
			tempList.add(setExternalToInternal(regionDef,v));
		return tempList;
	}
	
	//after knowing 
	private Variable setExternalToInternal(Map<String,Boolean> regionDef,Variable v)
	{
		if(v instanceof ExternalPara)
		{
			if(regionDef.get(((ExternalPara) v).getName())==null)
				return v;
			else
			{
				if(regionDef.get(((ExternalPara) v).getName()))
				{
					return  new InternalVar(((ExternalPara) v).getName());
				}
				else
					return v;
			}
		}
		else if(v instanceof Expression)
		{
			List<List<Variable>> newOperandList = new ArrayList<>();
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
				List<Variable> tempOperand = new ArrayList<>();
				for(Variable operand:operandList)
				{
					tempOperand.add( setExternalToInternal(regionDef,operand));
				}
				newOperandList.add(tempOperand);
			}
 			((Expression) v).setOperands(newOperandList);
			return v;
		}
		else if(v instanceof T)
		{
			((T) v).setVariable(setExternalToInternal(regionDef,((T) v).getVariable()));
			return v;
		}
		else
			return v;
	
	}
	private ArrayList<Variable> copyVar(ArrayList<Variable> varList)
	{
		ArrayList<Variable> vl = new ArrayList<>();
		if(varList!=null)
			for(Variable v: varList)
			{
				//!!!Stop analyzing if the size of the IR grows too big
				
				if(v.getSize()<2000)
					vl.add(copyVar(v));
				else
					vl.add(new ExternalPara("Unknown@IRSIZE"));
					
			}
		return vl;
	}
	private Variable copyVar(Variable v)
	{
		
		
		if(v instanceof InternalVar)
			return new InternalVar(((InternalVar) v).getName(),((InternalVar) v).getK(),((InternalVar) v).getSigma(),((InternalVar) v).getRegionNum(),((InternalVar) v).getLine());
		else if(v instanceof Expression)
		{
			List<List<Variable>> newOperandList = new ArrayList<>();
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
			List<Variable> tempOp = new ArrayList<>();
			for(Variable operand:operandList)
			{
				if(operand instanceof InternalVar)
					tempOp.add(new InternalVar(((InternalVar) operand).getName(),((InternalVar) operand).getK(),((InternalVar) operand).getSigma(),((InternalVar) operand).getRegionNum(),((InternalVar) operand).getLine()));
				else if(operand instanceof Init)
					tempOp.add(new Init(((Init) operand).getName(),((Init) operand).getLine(),copyVar((ArrayList<Variable>) ((Init) operand).getInitVar())));
				else if(operand instanceof Expression)
				{
					//System.out.println(operand.getSize());
					tempOp.add(copyVar(operand));
				}
				else if(operand instanceof T)
					tempOp.add(new T(copyVar(((T) operand).getVariable()),((T) operand).getTVarName(),((T) operand).getRegionNumber(),((T) operand).getK(),((T) operand).isFi(),((T) operand).getLine()));
				else
					tempOp.add(operand);
			}
			newOperandList.add(tempOp);
			}
			return new Expression(newOperandList,((Expression) v).getOperation());
		}
		else if(v instanceof T)
		{
			return new T(copyVar(((T) v).getVariable()),((T) v).getTVarName(),((T) v).getRegionNumber(),((T) v).getK(),((T) v).isFi(),((T) v).getLine());
		}
		else if(v instanceof Init)
		{
			return new Init(((Init) v).getName(),((Init) v).getLine(),copyVar((ArrayList<Variable>) ((Init) v).getInitVar()));
		}
		else
			return v;
	}
	public List<NodeInterface> topoSort(List<NodeInterface> allNode,NodeInterface entry,List<EdgeInterface> backedges)
	{

		Map<NodeInterface,List<EdgeInterface>> inEdgeMap = new HashMap<>();
		for(NodeInterface n:allNode)
		{

			if(!n.equals(entry))
			{
				List<EdgeInterface> inEdge = new ArrayList<>();
				for(EdgeInterface e:n.getInEdges())
				{
					if(!backedges.contains(e))
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
			
			
			
			//if one of its inedge is a backedge
			
			EdgeInterface back = null;
			for(EdgeInterface ine:n.getInEdges())
			{	if(backedges.contains(ine))
				{
					back = ine;
				}
			}

				for(EdgeInterface e:n.getOutEdges())
				{
					if(!backedges.contains(e)&&allNode.contains(e.getDestination()))
					{
						if(inEdgeMap.get(e.getDestination())==null)
						{
							//S.add(e.getDestination());
						}
						else
						{	
							inEdgeMap.get(e.getDestination()).remove(e);
						
							if(inEdgeMap.get(e.getDestination()).isEmpty())
								S.add(e.getDestination());
						}
					}
				}
			
		}
		return L;
	}
	/*
	public String toHtml(String line)
	{
		StringBuilder html = new StringBuilder();
		html.append("var treeData = [  {\n");		
		html.append("\"name\": \""+rd.getAllDef().get(line)+"\",");
		html.append("\"parent\": \"null\"");
		html.append(",\n \"children\": [");
		for(int i=0;i<newUseMapLoop.get(line).size();i++)
		{
			if(i==newUseMapLoop.get(line).size()-1)
				html.append("{"+toHtml(newUseMapLoop.get(line).get(i),rd.getAllDef().get(line))+"}");
			else
				html.append("{"+toHtml(newUseMapLoop.get(line).get(i),rd.getAllDef().get(line))+"},");
		}
		html.append("]");
		html.append("}];");
		return html.toString();
	}

	public String toHtml(Variable v,String parent)
	{
		StringBuilder html = new StringBuilder();
		//html.append("\"name\": "+v.toString()+",");
		//html.append("\"parent\": \""+parent+"\"");
		if(v instanceof T)
		{
			html.append("\"name\": "+"\""+((T)v).toTString()+"\""+",");
			html.append("\"parent\": \""+parent+"\"");
			html.append(",\n \"children\": [");
			html.append("{"+toHtml(((T) v).getVariable(),((T)v).toTString())+"}");
			html.append("]");
		}
		else if(v instanceof Expression)
		{
			
			Expression e = (Expression) v;
			html.append("\"name\": "+ e.getOperation().toString()+",");
			html.append("\"parent\": \""+parent+"\"");
			html.append(",\n \"children\": [");
			int size = e.getOperands().size();
			for(int i=0;i<size;i++)
			{
				Variable operand = e.getOperands().get(i);
				html.append("{"+toHtml(operand,e.getOperation().getName()));
				if(i!=size-1)
					html.append("},");
				else
					html.append("}");
			}
			html.append("]");
		}
		else
		{
			
			html.append("\"name\": "+v.toString()+",");
			html.append("\"parent\": \""+parent+"\"");
		}
		return html.toString();
	}*/
	public Map<String,List<String>> getTargetLines()
	{	
		Map<String,List<String>> labelLines = new HashMap<>();
		for(Entry<NodeInterface,List<String>> en:targetVarNodeAndName.entrySet())
		{		
			
			List<String> lines = new ArrayList<>();
			NodeInterface n = en.getKey();
			String varname = en.getValue().get(0); 
			String label = en.getValue().get(1);
			if(varname.contains("\""))
			{
				labelConst.put(label, varname);
				lines.add("-1");
				labelLines.put(label, lines);
				
			}
			
			else				
			for(String line: rd.getLineNumForUse(n, varname))
			{
				lines.add(line);
	
			}
			labelLines.put(label, lines);
		}
		
		return labelLines;
	}
	public ReachingDefinition getRD()
	{
		return rd;
	}
	public Map<String,String> getLabelConstant()
	{
		return labelConst;
	}
	public ArrayList<Variable> getTranslatedIR(String line)
	{
		return newUseMapLoop.get(line);
	}
	public Map<String, Set<NodeInterface>> getParaMap()
	{
		return paraMap;
	}
	public Map<String,Set<String>> getFieldMap()
	{
		return fieldMap;
	}
	public int getTargetCount()
	{
		return targetCount;
	}
}
