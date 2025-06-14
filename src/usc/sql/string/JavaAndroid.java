package usc.sql.string;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import CallGraph.NewNode;
import CallGraph.StringCallGraph;
import SootEvironment.AndroidApp;
import SootEvironment.JavaApp;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.internal.ImmediateBox;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;
import soot.util.Chain;
import usc.sql.ir.ConstantInt;
import usc.sql.ir.ConstantString;
import usc.sql.ir.Expression;
import usc.sql.ir.ExternalPara;
import usc.sql.ir.InterRe;
import usc.sql.ir.InternalVar;
import usc.sql.ir.T;
import usc.sql.ir.Variable;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cdg.Dominator;
import edu.usc.sql.graphs.cdg.PostDominator;
import edu.usc.sql.graphs.cfg.BuildCFGs;
import edu.usc.sql.graphs.cfg.CFGInterface;
import edu.usc.sql.graphs.cfg.SootCFG;

public class JavaAndroid {
	
	private Map<String,List<Integer>> targetSignature;
	private int maxloop;
	private Map<String, Set<Variable>> output = new HashMap<>();
	private Map<String, Set<String>> analysisResult = new HashMap<>();
	private Map<String, Map<String, Integer>> irOpStatistics = new HashMap<>();
 	private Map<String,ReachingDefinition> rds = new HashMap();
	private Map<String,CFGInterface> cfgs = new HashMap();
	private StringCallGraph callGraph;
	public JavaAndroid(String rtjar,String appfolder,String classlist,String apk,Map<String,List<Integer>> targetSignature, int maxloop)
	{		
		this.targetSignature = targetSignature;
		this.maxloop = maxloop;
		AndroidApp App=new AndroidApp(rtjar,appfolder+apk,appfolder+classlist);
		this.callGraph = App.getCallgraph();
		InterpretCheckerAndroid(appfolder);
		
	}
	public JavaAndroid(String rtjar,String appfolder,String classlist,Map<String,List<Integer>> targetSignature, int maxloop)
	{
		this.targetSignature = targetSignature;
		this.maxloop = maxloop;
		InterpretCheckerJava(rtjar,appfolder,appfolder+classlist,
				appfolder+"/MethodSummary/",appfolder+"/Output/");
	}
	public JavaAndroid(Map<String,List<Integer>> targetSignature, int maxloop, String outputPath)
	{
		this.targetSignature = targetSignature;
		this.callGraph = createCallGraph();
		this.maxloop = maxloop;
		InterpretCheckerAndroid(outputPath);
	}
	public JavaAndroid(StringCallGraph callGraph, Map<String,List<Integer>> targetSignature, int maxloop, String outputPath)
	{
		this.callGraph = callGraph;
		this.targetSignature = targetSignature;
		this.maxloop = maxloop;
		InterpretCheckerAndroid(outputPath);
	}
	private StringCallGraph createCallGraph()
	{
		Chain<SootClass> classes =  Scene.v().getApplicationClasses();
		List<SootMethod> entryPoints = new ArrayList<>();
		Set<SootMethod> allMethods = new HashSet<>();
		for(Iterator<SootClass> iter = classes.iterator(); iter.hasNext();)
		{
			SootClass sc = iter.next();
			if(!sc.getName().startsWith("android.support.v4"))
            {
                sc.setApplicationClass();
                allMethods.addAll(sc.getMethods());
            }
			for(SootMethod sm: sc.getMethods()){
				if(sm.isConcrete())
					entryPoints.add(sm);
			}
		}
		Scene.v().loadNecessaryClasses();
		Scene.v().setEntryPoints(entryPoints);
		CHATransformer.v().transform();
		CallGraph cg = Scene.v().getCallGraph();
		StringCallGraph callgraph = new StringCallGraph(cg, allMethods);
		return callgraph;
	}
	private void InterpretCheckerAndroid(String outputPath)
	{
		Map<String,Map<String,Set<Variable>>> targetMap = new HashMap<>();
    	Map<String,Set<NodeInterface>> paraMap = new HashMap<>();
    	Map<String,Set<String>> fieldMap = new HashMap<>();
		Map<String,Translator> tMap = new HashMap<>();


		String summaryFolder = "MethodSummary/";
		File sFolder = createSummaryFolder(summaryFolder);

		//System.out.println("Target Signatures and parameters: "+targetSignature);
		

		//prune the call graph and only include callers and callees of target APIs
		Set<String> relevantMethods = identifyRelevant(targetSignature.keySet());
		callGraph.setPotentialAPI(relevantMethods);
		
		//visit the methods in reverser topological order, get the IRs for each target string variable,
		//store in targetMap, which maps the method signature to the IRs it contains
		List<CFGInterface> rtoList = callGraph.getRTOInterface();
		Map<String, CFGInterface> sigToCFG = new HashMap<>();
    	getIRsInReverseTopoOrder(targetMap, paraMap, fieldMap, tMap,
				summaryFolder, rtoList, sigToCFG);

    	
    	Map<String, Set<Variable>> callPathIdToIR = new HashMap<>();
    	Map<String, Map<NodeInterface, String>> callPathIdToCallPath = new HashMap<>();
    	//visit the method in topological order, duplicate the IRs in different call chains
    	for(NewNode cgEntry : callGraph.getHeads())
    	{
    		if(cgEntry.getMethod() == null)
    			continue;
    		String sig = cgEntry.getMethod().getSignature();
    		if(!relevantMethods.contains(sig))
    			continue;
    		

    		CFGInterface cfg = sigToCFG.get(sig);

			Map<NodeInterface, String> callChain = new LinkedHashMap<>();
			addToTargetIRs(cfg, sigToCFG, callChain, targetMap, callPathIdToIR, callPathIdToCallPath);
    	}
    	
    	
    	
    	for(Entry<String, Set<Variable>> callPathIdToIREntry : callPathIdToIR.entrySet())
    	{
    		
    		String callPathId = callPathIdToIREntry.getKey();
    		Set<Variable> IRs = replaceExternalInCallChain(callPathIdToIREntry.getValue(),
    				callPathIdToCallPath.get(callPathId), tMap, fieldMap);
    		

			irOpStatistics.put(callPathId, getOperations(IRs));
    		Interpreter intp = new Interpreter(IRs,fieldMap,maxloop);
			//InterpreterPath intp = new InterpreterPath(newIR,fieldMap,maxloop);

			Set<String> possibleValues = new LinkedHashSet<>();
			for(Variable targetIR : IRs)
			{
				//System.out.println("IR:"+targetIR);
				for(String intpValue: targetIR.getInterpretedValue())
				{
					String formatAdjustment = intpValue.trim().replaceAll("\\\\'","'");
					possibleValues.add(formatAdjustment);
				}
			}
			analysisResult.put(callPathId, possibleValues);

    	}
    	
		removeSummaryFolder(sFolder);
	}

	
	private void addToTargetIRs(CFGInterface cfg,
			Map<String, CFGInterface> sigToCFG,
			Map<NodeInterface, String> callChain,
			Map<String, Map<String, Set<Variable>>> methodToIR,
			Map<String, Set<Variable>> callPathIdToIR,
			Map<String, Map<NodeInterface, String>> callPathIdToCallPath) {
		
		String currentMethodSig = cfg.getSignature();
		if(methodToIR.containsKey(cfg.getSignature()))
		{
			Map<String, Set<Variable>> lineIdToIR = methodToIR.get(currentMethodSig);
			
			for(Entry<String, Set<Variable>> lineIdToIREntry : lineIdToIR.entrySet())
			{
				String[] hotspot = lineIdToIREntry.getKey().split("@");
				String sourceLineNum = hotspot[1];
				String bytecodeOffset = hotspot[2];
				String apiSig = hotspot[3];
				String paraIndex = hotspot[4];
				
				String pathId = printCallChain(callChain) 
				+ currentMethodSig + "@" + sourceLineNum + "@" + bytecodeOffset +"@" + apiSig + "@"+ paraIndex;
				if(callPathIdToIR.containsKey(pathId))
					System.err.println("Depulicate string analysis result:" + pathId);
				callPathIdToIR.put(pathId, copyVarSet(lineIdToIREntry.getValue()));
				callPathIdToCallPath.put(pathId, callChain);
			}
			
		}
		
		for(NodeInterface n: cfg.getAllNodes())
		{
			Unit actualNode = (Unit) ((Node)n).getActualNode();
			if(actualNode!=null)
			{
				if(((Stmt)actualNode).containsInvokeExpr())
				{
					SootMethod sm = ((Stmt)actualNode).getInvokeExpr().getMethod();
					String sig= sm.getSignature();
					if(sigToCFG.containsKey(sig))
					{
						CFGInterface newCfg = sigToCFG.get(sig);
						
						Map<NodeInterface, String> copyCallChain = new LinkedHashMap<>();
						for(Entry<NodeInterface, String> entry : callChain.entrySet())
							copyCallChain.put(entry.getKey(), entry.getValue());
						copyCallChain.put(n, currentMethodSig);
						addToTargetIRs(newCfg, sigToCFG, copyCallChain, methodToIR, callPathIdToIR, callPathIdToCallPath);
					}
				}
			}
		}
	}

	
	private void getIRsInReverseTopoOrder(
			Map<String, Map<String, Set<Variable>>> targetMap,
			Map<String, Set<NodeInterface>> paraMap,
			Map<String, Set<String>> fieldMap, Map<String, Translator> tMap,
			String summaryFolder, List<CFGInterface> rtoList,
			Map<String, CFGInterface> sigToCFG) {

		for(CFGInterface cfg : rtoList)
    	{
    		String signature=cfg.getSignature();
    		sigToCFG.put(signature, cfg);
    		
    		LayerRegion lll = new LayerRegion(null);
    		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));	   		
    		rds.put(signature, rd);
    		cfgs.put(signature, cfg);
    		
    		LayerRegion lr = new LayerRegion(cfg);

    		Translator t = new Translator(rd, lr,signature,summaryFolder,targetSignature);
    		
    		tMap.put(signature, t);
    		paraMap.putAll(t.getParaMap());
    		
    		for(Entry<String,Set<String>> en: t.getFieldMap().entrySet())
    		{
    			if(fieldMap.containsKey(en.getKey()))
    				fieldMap.get(en.getKey()).addAll(en.getValue());
    			else
    				fieldMap.put(en.getKey(), en.getValue());
    		}
    		
    	
    		 		
    		if(t.getTargetLines().isEmpty())
    			continue;
    		
    		//Set<String> value = new HashSet<>();
    	
    	
    		
    		//Interpreter intp = new Interpreter(t,loopCount);
    		
    		//label set<IR>
    		Map<String,Set<Variable>> labelIR = new HashMap<>();
    		
    		for(String labelwithnum:t.getTargetLines().keySet())
    		{
    			Set<Variable> targetIR = new LinkedHashSet<>();
    			for(String line: t.getTargetLines().get(labelwithnum))
    			{
    				//if target IR is a constant string
    				if(line.equals("-1"))
    				{

    		    		//add const label
    		    		
    		    		if(tMap.get(signature).getLabelConstant().get(labelwithnum)!=null)
    		    		{
    		    			String value = tMap.get(signature).getLabelConstant().get(labelwithnum);
    		    			
    		    			targetIR.add(new ConstantString(value.substring(1,value.length()-1)));
    		    		}
    					
    				}
    				if(t.getTranslatedIR(line)!=null)
    				{

    					targetIR.addAll(t.getTranslatedIR(line));
    				}
    			}

    			labelIR.put(labelwithnum, targetIR);
    		}
    		
    		
    		
    		if(!targetMap.containsKey(signature))
    			targetMap.put(signature, labelIR);
    	 		

    	}
	}
	


	private Set<Variable> replaceExternalInCallChain(Set<Variable> IRs, 
			Map<NodeInterface, String> callChainNodeToContainingMethod, 
			Map<String,Translator> tMap, Map<String, Set<String>> fieldMap)
	{
		boolean existParaOrField = false;
		for(Variable v:IRs)
		{
			if(containParaOrField(v))
				existParaOrField = true;
		}
		if(!existParaOrField)
		{	
			return IRs;
		}
		else
		{
			//replace parameters
			Set<Variable> newIRs = new LinkedHashSet<>();
			newIRs.addAll(IRs);
			if(!callChainNodeToContainingMethod.isEmpty())
			{
				//reverse the order so that it is from callee to caller
				List<NodeInterface> reverseOrderedKeys = new ArrayList<>(callChainNodeToContainingMethod.keySet());
				Collections.reverse(reverseOrderedKeys);
	

				for (NodeInterface n : reverseOrderedKeys) {
					
					Stmt actualNode = ((Node<Stmt>) n).getActualNode();
					
					
					if(actualNode.getInvokeExpr().getArgCount() == 0)
						break;
					
				    String parentSig = callChainNodeToContainingMethod.get(n);
				    Set<Variable> externalReplacedIRs = new LinkedHashSet<>();
				    for(Variable v : newIRs)
				    {
				    	Set<Variable> newV = replaceExternal(v, n, parentSig, tMap, fieldMap);
				    	externalReplacedIRs.addAll(copyVarSet(newV));
				    }
				    newIRs = externalReplacedIRs;
				}
			
			}
			//replace fields
			Set<Variable> externalReplacedIRs = new LinkedHashSet<>();
			for(Variable v : newIRs)
		    {
		    	Set<Variable> newV = replaceExternal(v, null, null, tMap, fieldMap);
		    	externalReplacedIRs.addAll(copyVarSet(newV));
		    }
			return externalReplacedIRs;
			
		}

	}

	
	boolean containParaOrField(Variable v)
	{

			if(v instanceof ExternalPara)
			{
				String name = ((ExternalPara) v).getName();
				if(name.contains("@parameter") || name.matches("<.*>"))
					return true;
				else
					return false;
			}
			else if(v instanceof Expression)
			{
				for(List<Variable> operandList:((Expression) v).getOperands())
				{
					for(Variable operand: operandList)
					if(containParaOrField(operand))
						return true;
				}
				return false;
			}
			else if(v instanceof T)
			{
			
				return containParaOrField(((T) v).getVariable());
			}
			else
			return false;
	}
	
	
	private Set<Variable> replaceExternal(Variable v,NodeInterface n, String parentSig, 
			Map<String,Translator> tMap,
			Map<String,Set<String>> fieldMap)
	{
		Set<Variable> returnSet = new LinkedHashSet<>();
		if(v instanceof ExternalPara)
		{
			
			Translator t = tMap.get(parentSig);
			//replace the parameter with the IR value at the call site
			if(((ExternalPara) v).getName().contains("@parameter") && n != null)
			{
				String tmp = ((ExternalPara) v).getName().split(":")[0].replaceAll("@parameter", "");
				int index = Integer.parseInt(tmp);
				
			//	System.out.println(index +" "+valueBox);
			
				List<ValueBox> valueBox = ((Unit)((Node)n).getActualNode()).getUseBoxes();

				if(!(valueBox.get(0) instanceof ImmediateBox))
					index = index+1;
				
			//	System.out.println(index+"->index-> "+valueBox);
				
				if(index >= valueBox.size())
					returnSet.add(v);
				else 
				{
					String para = valueBox.get(index).getValue().toString();
					String type = valueBox.get(index).getValue().getType().toString();
					if(para.contains("\""))
						returnSet.add( new ConstantString(para));
					else if(type.equals("int") || type.equals("long"))
					{
						if(!para.contains("i")&&!para.contains("b")&&!para.contains("l"))
							returnSet.add( new ConstantString(""+Integer.parseInt(para.replace("L", ""))));
						else
							returnSet.add(v);
					}
					else
					{
						
				
	
						//System.out.println(valueBox.toString()+para+t.getRD().getAllDef());
						List<String> defineLines = t.getRD().getLineNumForUse(n, para);
						if(defineLines.isEmpty())
							returnSet.add(v);
						else
						{
							Set<Variable> newIR = new LinkedHashSet<>();
							for(String line: defineLines)
							{
								if(t.getTranslatedIR(line)!=null)
									newIR.addAll(t.getTranslatedIR(line));
							}
							returnSet.addAll(newIR);
						}
					}		
				}
			}
			//replace the field with all the field assignments, this is flow & context insensitive
			else if(((ExternalPara) v).getName().matches("<.*>"))
			{
				String fieldName = ((ExternalPara) v).getName();
				if(fieldMap.get(fieldName) == null)
					returnSet.add(v);
				else
				{
					//System.out.println(fieldName);
					//System.out.println(fieldMap.get(fieldName).size());
					for(String fieldDefLocation : fieldMap.get(fieldName))
					{
						String methodSig = fieldDefLocation.split("@")[0];
						String line = fieldDefLocation.split("@")[1];
						if(tMap.get(methodSig) != null && tMap.get(methodSig).getTranslatedIR(line) != null)
						{
							List<Variable> irs = tMap.get(methodSig).getTranslatedIR(line);
							returnSet.addAll(irs);
						}
						//System.out.println(returnSet.size() + returnSet.toString());
					}
				}
			}
			else
				returnSet.add(v);
			
		}

		else if(v instanceof Expression)
		{
			List<List<Variable>> newOperandList = new ArrayList<>();
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
				List<Variable> tempOperand = new ArrayList<>();
				for(Variable operand:operandList)
				{
					tempOperand.addAll( replaceExternal(operand,n,parentSig,tMap,fieldMap));
				}
				newOperandList.add(tempOperand);
			}
 			((Expression) v).setOperands(newOperandList);
 			returnSet.add(v);
		}
		else if(v instanceof T)
		{
			((T) v).setVariable(replaceExternal(((T) v).getVariable(),n,parentSig,tMap,fieldMap).iterator().next());
			returnSet.add(v);
		}
		else
			returnSet.add(v);
		
		return returnSet;
	}
	
	public Map<String, Set<Variable>> getIRs()
	{
		return output;
	}
	public Map<String, Map<String, Integer>> getIROpStatistics()
	{
		return irOpStatistics;
	}
	public Map<String, Set<String>> getInterpretedValues()
	{
		return analysisResult;
	}
	
	public Map<String,ReachingDefinition> getReachingDefinition()
	{
		return rds;
	}
	public Map<String,CFGInterface> getCFG()
	{
		return cfgs;
	}
	boolean notEmptyAndContainNotUnknown(Set<String> value)
	{
		if(value.isEmpty())
			return false;
		else
		{
			for(String s:value)
				if(!s.contains("(.*)"))
						return true;
			
			return false;
		}
	}
	boolean emptyOrContainUnknown(Set<String> value)
	{
		if(value.isEmpty())
			return true;
		else
		{
			for(String s:value)
				if(s.contains("Unknown"))
					return true;
			return false;
		}
	}
	private Set<Variable> copyVarSet(Set<Variable> vSet)
	{
		Set<Variable> copyVSet = new LinkedHashSet<>();
		for(Variable v : vSet)
			copyVSet.add(copyVar(v));
		return copyVSet;
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
				else if(operand instanceof Expression)
				{
					
					//System.out.println(operand.getSize());
					tempOp.add(copyVar(operand));
				}
				else if(operand instanceof T)
				{
					
					tempOp.add(new T(copyVar(((T) operand).getVariable()),((T) operand).getTVarName(),((T) operand).getRegionNumber(),((T) operand).getK(),((T) operand).isFi(),((T) operand).getLine()));
				}
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

		else
			return v;
	}
	
	
	public Set<String> identifyRelevant(Set<String> targetScanList)
	{
		Set<String> targetMethod = new HashSet<>();
		List<NewNode> rto = callGraph.getRTOdering();
		Map<String,NewNode> rtoMap = callGraph.getRTOMap();
		Set<String> rtoSig = new HashSet<>();
		for(NewNode n: rto)
		{

			if(n.getMethod().isConcrete()&&!n.getMethod().getDeclaringClass().isAbstract())
			{
				rtoSig.add(n.getMethod().getSignature());
				boolean isRelevant = false;
				for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
				{
					
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						SootMethod sm = null;
						try{
							sm =((Stmt)actualNode).getInvokeExpr().getMethod();
						}
						catch(ResolutionFailedException ex)
						{
							System.out.println("Soot fails to get the method:"+((Stmt)actualNode).getInvokeExpr());
						}	
						if(sm==null)
							continue;
						
						String sig= sm.getSignature();
						
						if(targetScanList.contains(sig)||targetMethod.contains(sig))
						{
							isRelevant = true;
						}
						
					}
				}
				
				if(isRelevant)
				{
					targetMethod.add(n.getMethod().getSignature());
					
				}


			}
			
		}
		//System.out.println("Methods contain target APIs: "+ targetMethod.size());
		Stack<String> processMethod = new Stack<>();
		processMethod.addAll(targetMethod);
		//topo
		while(!processMethod.isEmpty())
		{
			String currentMethod = processMethod.pop();
			NewNode n = rtoMap.get(currentMethod);
			SootMethod sm = n.getMethod();
			if(sm.isConcrete()&&!sm.getDeclaringClass().isAbstract())
			{
				
				for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
				{
					
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						
						SootMethod method = ((Stmt)actualNode).getInvokeExpr().getMethod();
						String type = method.getReturnType().toString();
						
						if(type.equals("java.lang.String")||type.equals("java.lang.StringBuilder")
								||type.equals("java.lang.StringBuffer"))
						{
							if(rtoSig.contains(method.getSignature()))
							{
								if(!targetMethod.contains(method.getSignature()))
								{
									processMethod.push(method.getSignature());
									targetMethod.add(method.getSignature());
								}
							}
						}
	
						
					}
				}
			}

		}
		//System.out.println("Target methods and callees: "+ targetMethod.size());
		
		for(NewNode n: rto)
		{

			if(n.getMethod().isConcrete()&&!n.getMethod().getDeclaringClass().isAbstract())
			{

				for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
				{
					
					if(((Stmt)actualNode).containsInvokeExpr())
					{
						SootMethod sm = null;
						try{
							sm =((Stmt)actualNode).getInvokeExpr().getMethod();
						}
						catch(ResolutionFailedException ex)
						{
							System.out.println("Soot fails to get the method:"+((Stmt)actualNode).getInvokeExpr());
						}	
						if(sm==null)
							continue;
						
						String sig= sm.getSignature();
						
						if(targetMethod.contains(sig))
						{
							targetMethod.add(n.getMethod().getSignature());
						}
						
					}
				}
				

			}
			
		}
		//System.out.println("Target methods and callers and callees: "+ targetMethod.size());
		Set<String> targetClass = new HashSet<>();
		for(String sig: targetMethod)
		{
			targetClass.add(sig.substring(0,sig.indexOf(":")+1));
		}
		for(NewNode n: rto)
		{
			String sig = n.getMethod().getSignature();
			String className = sig.substring(0,sig.indexOf(":")+1);
			if(sig.contains("<init>")||sig.contains("<clinit>"))
			{
				if(targetClass.contains(className))
				{
					targetMethod.add(sig);
				}
				
			}
				
		}
		//System.out.println("Total Target Method including constructor: "+ targetMethod.size());
		return targetMethod;
	}
	
	
	private File createSummaryFolder(String summaryFolder) {
		File sFolder = new File(summaryFolder);
		 // if the directory does not exist, create it
		 if (!sFolder.exists()) {
		     try{
		    	 sFolder.mkdir();
		     } 
		     catch(SecurityException se){
		    	 System.out.println("Create a folder named : \"MethodSummary\" under the app folder");
		     }
		 }
		 else
		 {
			 final File[] files = sFolder.listFiles();
			 if(files!=null) { //some JVMs return null for empty dirs
			        for(File f: files) {
			                f.delete();
			        }
			    }
		 }
		return sFolder;
	}
	
	private void removeSummaryFolder(File sFolder) {
		final File[] files = sFolder.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				f.delete();
			}
		}
		sFolder.delete();
	}
	
	public Map<String,Integer> getOperations(Set<Variable> vars)
	{		

		Map<String,Integer> opFreq = new HashMap<>();
		for(Variable v : vars)
			getOperations(v,opFreq);
		return opFreq;
	
	}
	
	private String printCallChain(Map<NodeInterface, String> callChain)
	{
		StringBuilder output = new StringBuilder("");
		//int length = silica.getCallChain().size();
		//int count = 0;
		for(Entry<NodeInterface, String> nodeToCallerSig : callChain.entrySet())
		{
			Unit actualNode = ((Node<Unit>)nodeToCallerSig.getKey()).getActualNode();
			String sig = nodeToCallerSig.getValue();
			output.append(sig);
			output.append("@");
			output.append(actualNode.getJavaSourceStartLineNumber());
			output.append("@");
			output.append(InterRe.getBytecodeOffset(actualNode));
			output.append("->\n");
		}
		return output.toString();
	}
	public void getOperations(Variable v,Map<String,Integer> opFreq)
	{		

		if(v instanceof Expression)
		{
			String op = ((Expression) v).getOperation().getName();
			if(opFreq.containsKey(op))
				opFreq.put(op,opFreq.get(op)+1);
			else
				opFreq.put(op,1);
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					getOperations(var,opFreq);
				}
			}
		}
	}
	public int getWidth(Set<Variable> vars)
	{
		int max = 0;
		for(Variable v: vars)
		{
			int width = getWidth(v);
			if(width > max)
				max = width;
		}
		return max;
			
	}
	public int getWidth(Variable v)
	{
		if(v instanceof T)
			return getWidth(((T)v).getVariable());
		else if(v instanceof Expression)
		{
			int sumWidth = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				int max = 0;
				for(Variable var: varList)
				{
					int temp = getWidth(var);
					if(temp > max)
						max = temp;		
				}
				sumWidth+=max;
			}
			return sumWidth;
		}
		else return 1;
	}
	
	public int getHeight(Set<Variable> vars)
	{
		int max = 0;
		for(Variable v: vars)
		{
			int height = getHeight(v);
			if(height > max)
				max = height;
		}
		return max;
			
	}
	public int getHeight(Variable v)
	{
		if(v instanceof T)
			return getHeight(((T)v).getVariable());
		else if(v instanceof Expression)
		{
			int maxHeight = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					int temp = getHeight(var);
					if(temp > maxHeight)
						maxHeight = temp;		
				}
			}
			return maxHeight+1;
		}
		else
			return 1;
	}
	
	public int getLoopDepth(Set<Variable> vars)
	{
		int max = 0;
		for(Variable v: vars)
		{
			int depth = getLoopDepth(v);
			if(depth > max)
				max = depth;
		}
		return max;
	}
	public int getLoopDepth(Variable v)
	{
		if(v instanceof T)
			return getLoopDepth(((T)v).getVariable())+1;
		else if(v instanceof Expression)
		{
			int maxLoopDepth = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					int temp = getLoopDepth(var);
					if(temp > maxLoopDepth)
						maxLoopDepth = temp;		
				}
			}
			return maxLoopDepth;
		}
		else 
			return 0;
	}
	public int getLoopCount(Set<Variable> vars)
	{
		int count = 0;
		for(Variable v: vars)
			count+= getLoopCount(v);
		return count;
	}
	public int getLoopCount(Variable v)
	{
		if(v instanceof T)
			return getLoopCount(((T)v).getVariable())+1;
		else if(v instanceof Expression)
		{
			int loopCount = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					loopCount+=getLoopCount(var);		
				}
			}
			return loopCount;
		}
		else 
			return 0;
	}
	public int getExternalCount(Set<Variable> vars)
	{
		int count = 0;
		for(Variable v: vars)
			count+= getExternalCount(v);
		return count;
	}
	public int getExternalCount(Variable v)
	{
		if(v instanceof ExternalPara)
			return 1;
		else if(v instanceof Expression)
		{
			int externalCount = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					externalCount+=getExternalCount(var);		
				}
			}
			return externalCount;
		}
		else 
			return 0;
	}
	
	

	
	private void InterpretCheckerJava(String arg0,String arg1,String arg2,String summaryFolder,String wfolder)
	{
		//"/home/yingjun/Documents/StringAnalysis/MethodSummary/"
		//"Usage: rt.jar app_folder classlist.txt"

		JavaApp App;
		if(arg1.contains("bookstore"))
		{

			App=new JavaApp(arg0,arg1,arg2,"void _jspService(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)");
		}
		else{
			App=new JavaApp(arg0,arg1,arg2,"void main(java.lang.String[])");
		}
		Map<String,Map<String,Set<Variable>>> targetMap = new HashMap<>();
    	Map<String,Set<NodeInterface>> paraMap = new HashMap<>();
    	Map<String,Set<String>> fieldMap = new HashMap<>();
		Map<String,Translator> tMap = new HashMap<>();
		long totalTranslate = 0,totalInterpret = 0;
		
		
		long t1,t2;
		

		 File sFolder = new File(summaryFolder);
		 File wFolder = new File(wfolder);
		 // if the directory does not exist, create it
		 if (!sFolder.exists()) {
		     System.out.println("creating directory: " + sFolder);
		     boolean result = false;
		     try{
		    	 sFolder.mkdir();
		         result = true;
		     } 
		     catch(SecurityException se){
		    	 System.out.println("Create a folder named : \"MethodSummary\" under the app folder");
		     }        
		     if(result) {    
		         System.out.println("DIR created");  
		     }
		 }
		 if (!wFolder.exists()) {
		     System.out.println("creating directory: " + wFolder);
		     boolean result = false;
		     try{
		    	 wFolder.mkdir();
		         result = true;
		     } 
		     catch(SecurityException se){
		    	 System.out.println("Create a folder named : \"Output\" under the app folder");
		     }        
		     if(result) {    
		         System.out.println("DIR created");  
		     }
		 }


    	for(CFGInterface cfg:callGraph.getRTOInterface())
    	{
    		//System.out.println(cfg.getSignature());
    		String signature=cfg.getSignature();
    		
    	//	if(!signature.contains("SubstringOfNull"))
    	//		continue;
    		
    		
    		if(signature.equals("<LoggerLib.Logger: void <clinit>()>")||signature.equals("<LoggerLib.Logger: void reportString(java.lang.String,java.lang.String)>"))
    		continue;
    		
    		//field																	def missing						

    		

    		
    		
      		//for(int i=1;i<=loopCount;i++)
    		//{  		
    		
    		

    		t1 = 	System.currentTimeMillis();
    		LayerRegion lll = new LayerRegion(null);
    		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));	   		
    		rds.put(signature, rd);
    		
    		LayerRegion lr = new LayerRegion(cfg);
    	
    		//System.out.println(signature);
    		Translator t = new Translator(rd, lr,signature,summaryFolder,targetSignature);
    	
    		tMap.put(signature, t);
    		paraMap.putAll(t.getParaMap());
    		
    		for(Entry<String,Set<String>> en: t.getFieldMap().entrySet())
    		{
    			if(fieldMap.containsKey(en.getKey()))
    				fieldMap.get(en.getKey()).addAll(en.getValue());
    			else
    				fieldMap.put(en.getKey(), en.getValue());
    		}
    		//fieldMap.putAll(t.getFieldMap());
    		 		
    		if(t.getTargetLines().isEmpty())
    			continue;
    		
    		//Set<String> value = new HashSet<>();
    	
    		
    		//label set<IR>
    		Map<String,Set<Variable>> labelIR = new HashMap<>();
    		
    		for(String labelwithnum:t.getTargetLines().keySet())
    		{
    			Set<Variable> targetIR = new LinkedHashSet<>();
    			for(String line: t.getTargetLines().get(labelwithnum))
    				if(t.getTranslatedIR(line)!=null)
    					targetIR.addAll(t.getTranslatedIR(line));
    			
    	
    			//if(targetIR.isEmpty())
    			//	targetIR.add(new ExternalPara("Unknown"));
    			labelIR.put(labelwithnum, targetIR);
    		}
    		

    	
    		
    		if(!targetMap.containsKey(signature))
    			targetMap.put(signature, labelIR);
    	 		
    		t2 = 	System.currentTimeMillis();
    		totalTranslate+=t2-t1;
    	
    	}
		
    	//Key : signature of method that contains the targets
    	//Value: a map mapping the line id of the target string to its IR
    	for(Entry<String,Map<String,Set<Variable>>> enout: targetMap.entrySet())
    	{
    		String signature = enout.getKey();
    		
    		//System.out.println("\n"+signature);
    		

    		
    		for(Entry<String,Set<Variable>> en:enout.getValue().entrySet())
    		{
    		
    			
    				
	    		t1 = System.currentTimeMillis();
	    		
	    		
	    		Set<Variable> newIR = replaceExternal(en.getValue(),signature,paraMap,tMap,fieldMap);
	    		t2 = System.currentTimeMillis();
	    		totalTranslate += t2-t1;
	    		
	    		
	    		int loopCount = 3;	    			    		
	    		String tempSig = signature.replaceAll("TestCases.", "");
	    		int dot = tempSig.indexOf(".");
	    		String tt = tempSig.substring(dot+1);
	    		
	    		if(tt.contains("Mix")||tt.contains("NestedLoop"))
	    			loopCount = 2;
	    		else
	    			loopCount = 3;
	    		
	    		Set<String> value = new LinkedHashSet<>();
	    		
	    		t1 = System.currentTimeMillis();
    			Interpreter intp = new Interpreter(newIR,fieldMap,loopCount);
    			
    			value.addAll(intp.getValueForIR());

	    		
	    		//add const label
	    		
	    		if(tMap.get(signature).getLabelConstant().get(en.getKey())!=null)
	    		{
	    		//	value.add(tMap.get(signature).getLabelConstant().get(en.getKey()).replaceAll("\"", ""));
	    		}
	    		
    			t2 = System.currentTimeMillis();
    			
    			totalInterpret += t2-t1;
	    		
	    		//System.out.println("Label: "+en.getKey());
	    		//System.out.println("Output: "+value);
	    		
	    		if(!emptyOrContainUnknown(value))
	    		{
	    			//System.out.println(en.getKey()+":"+value+";"+value.isEmpty()+value.iterator().next().equals(""));
	    			
		    		try
		    		{
		    			BufferedWriter bw = new BufferedWriter(new FileWriter(wfolder+en.getKey().replaceAll("\"", "")+".txt",true));
		    			//BufferedWriter bw = new BufferedWriter(new FileWriter(wfolder+"output.txt",true));
		    			//bw.write(en.getKey().replaceAll("\"", ""));
		    			//bw.newLine();
		    			for(String s:value)
		    			{
		    				
		    				bw.write(s);
		    				bw.newLine();
		    			}
		    			
		    			bw.flush();
		    			bw.close();
		    		}
		    		catch(IOException e)
		    		{
		    			e.printStackTrace();
		    		}
	    		}

	    
	    	}
    		
    	    

    	}
    	
    	System.out.println("Total Trans: "+ totalTranslate);
    	System.out.println("Total Interp: "+ totalInterpret);
        removeSummaryFolder(sFolder);
	}
	
	private Set<Variable> replaceExternal(Set<Variable> IRs,String signature,
			Map<String,Set<NodeInterface>> paraMap,Map<String,Translator> tMap, Map<String, Set<String>> fieldMap)
	{

		boolean existPara = false;
		for(Variable v:IRs)
		{
			if(containParaOrField(v))
				existPara = true;
		}
		if(!existPara)
		{	
			return IRs;
		}
		else
		{
			Set<Variable> vSet = new LinkedHashSet<>();
			for(Variable v: IRs)
			{
				if(paraMap.get(signature)==null)
					vSet.add(v);
				else
				{
					
					if(callGraph.getParents(signature).isEmpty())
						vSet.add(copyVar(v));
					else
					{
						for(String parentSig:callGraph.getParents(signature))
						{
							Set<Variable> newIR = new LinkedHashSet<>();
							
							if(tMap.get(parentSig)!=null&&tMap.get(parentSig).getParaMap()!=null)
								if(tMap.get(parentSig).getParaMap().get(signature)!=null)
								{
									for(NodeInterface n:tMap.get(parentSig).getParaMap().get(signature))
										newIR.addAll(replaceExternal(copyVar(v),n, parentSig, tMap, fieldMap));
									
									Set<Variable> copy = new LinkedHashSet<>();
									for(Variable vv:newIR)
										copy.add(copyVar(vv));
									
								//	System.out.println(signature);
									vSet.addAll(replaceExternal(copy,parentSig, paraMap, tMap, fieldMap));
									
								}

						}					
					}
					
				}			
			}
			return vSet;
		}

	}
}
