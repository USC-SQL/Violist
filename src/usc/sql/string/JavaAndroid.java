package usc.sql.string;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

import CallGraph.NewNode;
import CallGraph.StringCallGraph;
import SootEvironment.AndroidApp;
import SootEvironment.JavaApp;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.internal.ImmediateBox;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
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
		long totalTranslate = 0,totalInterpret = 0;

		String summaryFolder = "MethodSummary/";
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

		//System.out.println("Target Signatures and parameters: "+targetSignature);
		
		long t1,t2;
		//prune the call graph and only include callers and callees of target APIs
		callGraph.setPotentialAPI(identifyRelevant(targetSignature.keySet()));
    	for(CFGInterface cfg:callGraph.getRTOInterface())
    	{
    		
    		String signature=cfg.getSignature();
    		//field																	def missing
    		//if(cfg.getAllNodes().size()>3000)
    			//continue;

    		//for(int i=1;i<=loopCount;i++)
    		//{
    		t1 = System.currentTimeMillis();
    		
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
    			
    			Set<Variable> targetIR = new HashSet<>();
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
    					targetIR.addAll(t.getTranslatedIR(line));
    			}
    			labelIR.put(labelwithnum, targetIR);
    		}
    		
    		
    		
    		if(!targetMap.containsKey(signature))
    			targetMap.put(signature, labelIR);
    	 		

    		t2 = System.currentTimeMillis();
    		totalTranslate += t2-t1;
    	}
    	int count = 0;
    	
    	List<String> statistic = new ArrayList<>();
    	for(Entry<String,Map<String,Set<Variable>>> enout: targetMap.entrySet())
    	{
    		String signature = enout.getKey();
    		int i1 = signature.indexOf("<"),i2 = signature.indexOf(":");
    		for(Entry<String,Set<Variable>> irSignature:enout.getValue().entrySet())
    		{
	    		t1 = System.currentTimeMillis();
	    		Set<Variable> newIR = replaceExternal(irSignature.getValue(),signature,paraMap,tMap);
	    		
	    		//statistic.add(en.getKey()+":"+getWidth(newIR)+" "+getHeight(newIR)+" "+getLoopDepth(newIR)+" "+getLoopCount(newIR)+" "+getExternalCount(newIR));
	    		
	    		t2 = System.currentTimeMillis();
	    		totalTranslate += t2-t1;
	    
	    		
	    		t1 = System.currentTimeMillis();

	    		Interpreter intp = new Interpreter(newIR,fieldMap,maxloop);
				//InterpreterPath intp = new InterpreterPath(newIR,fieldMap,maxloop);
				Set<String> value = new HashSet<>();
				value.addAll(intp.getValueForIR());
	    		
				if(value.isEmpty())
					value.add("Unknown@INTERPRET");
	    		
    			t2 = System.currentTimeMillis();
    			
    			totalInterpret += t2-t1;

    			//if(!value.isEmpty())
	    		//if(!emptyOrContainUnknown(value))  			
	    		{
        			output.put(irSignature.getKey(), newIR);
	    			String[] hotspot = irSignature.getKey().split("@");
	    			
	    			StringBuilder result = new StringBuilder();

    				//System.out.println("Method Name: "+ hotspot[0]);
    				result.append("Method Name: "+ hotspot[0]+"\n");
    				//System.out.println("Source Line Number: "+ hotspot[1]);
    				result.append("Source Line Number: "+ hotspot[1]+"\n");
    				//System.out.println("Bytecode Offset: "+hotspot[2]);
    				result.append("Bytecode Offset: "+hotspot[2]+"\n");
    				//System.out.println("Nth String Parameter: " + hotspot[3]);
    				result.append("Nth String Parameter: " + hotspot[3]+"\n");
    				//System.out.println("Jimple: "+ hotspot[4]);
    				result.append("Jimple: "+ hotspot[4]+"\n");

    				Set<String> possibleValues = new HashSet<>();
    				for(Variable targetIR : newIR)
    				{
    					//System.out.println("IR:"+targetIR);
    					result.append("IR:"+targetIR+"\n");
    					for(String intpValue: targetIR.getInterpretedValue())
    					{
    						String replace = intpValue.trim().replaceAll("\\\\'","'");
    						//System.out.println("Value:"+replace);
    						result.append("Value:"+replace+"\n");
    						possibleValues.add(replace);
    					}
    				}
    				//System.out.println();
    				//Method Signature@Bytecode Offset@Parameter Index
	    			analysisResult.put(hotspot[0]+"@"+hotspot[2]+"@"+hotspot[3],possibleValues);
	    			
		    		try
		    		{
		    			if(outputPath != null) {
                            PrintWriter bw = new PrintWriter(new FileWriter(outputPath + "result.txt", true));
                            //PrintWriter bw = new PrintWriter(new FileWriter(wfolder+en.getKey().replaceAll("\"", "")+".txt",true));
                            bw.println(result);

                            bw.flush();
                            bw.close();
                        }
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
    			Set<Variable> targetIR = new HashSet<>();
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
    	
	
    	
		
    	
   
		
		
    	for(Entry<String,Map<String,Set<Variable>>> enout: targetMap.entrySet())
    	{
    		String signature = enout.getKey();
    		
    		//System.out.println("\n"+signature);
    		

    		
    		for(Entry<String,Set<Variable>> en:enout.getValue().entrySet())
    		{
    		
    			
    				
	    		t1 = System.currentTimeMillis();
	    		Set<Variable> newIR = replaceExternal(en.getValue(),signature,paraMap,tMap);
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
	    		
	    		Set<String> value = new HashSet<>();
	    		
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
        final File[] files = sFolder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                f.delete();
            }
        }
        sFolder.delete();
	}
	public Map<String, Set<Variable>> getIRs()
	{
		return output;
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

	private Set<Variable> replaceExternal(Set<Variable> IRs,String signature,Map<String,Set<NodeInterface>> paraMap,Map<String,Translator> tMap)
	{

		boolean existPara = false;
		for(Variable v:IRs)
		{
			if(containPara(v))
				existPara = true;
		}
		if(!existPara)
		{	
			return IRs;
		}
		else
		{
			Set<Variable> vSet = new HashSet<>();
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
							Set<Variable> newIR = new HashSet<>();
							
							if(tMap.get(parentSig)!=null&&tMap.get(parentSig).getParaMap()!=null)
								if(tMap.get(parentSig).getParaMap().get(signature)!=null)
								{
									for(NodeInterface n:tMap.get(parentSig).getParaMap().get(signature))
										newIR.addAll(replaceExternal(copyVar(v),n,tMap.get(parentSig)));
									
									Set<Variable> copy = new HashSet<>();
									for(Variable vv:newIR)
										copy.add(copyVar(vv));
									
								//	System.out.println(signature);
									vSet.addAll(replaceExternal(copy,parentSig, paraMap, tMap));
									
								}

						}					
					}
					
				}			
			}
			return vSet;
		}

	}
	
	boolean containPara(Variable v)
	{

			if(v instanceof ExternalPara)
			{
				if(((ExternalPara) v).getName().contains("@parameter"))
					return true;
				else
					return false;
			}
			else if(v instanceof Expression)
			{
				for(List<Variable> operandList:((Expression) v).getOperands())
				{
					for(Variable operand: operandList)
					if(containPara(operand))
						return true;
				}
				return false;
			}
			else if(v instanceof T)
			{
			
				return containPara(((T) v).getVariable());
			}
			else
			return false;
	}
	
	
	private Set<Variable> replaceExternal(Variable v,NodeInterface n,Translator t)
	{
		Set<Variable> returnSet = new HashSet<>();
		if(v instanceof ExternalPara)
		{
			if(((ExternalPara) v).getName().contains("@parameter"))
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
			
				
				
					
					if(para.contains("\""))
						returnSet.add( new ConstantString(para));
					else if(valueBox.get(index).getValue().getType().toString().equals("int"))
					{
					
						if(!para.contains("i")&&!para.contains("b"))
						returnSet.add( new ConstantString(""+(char)Integer.parseInt(para)));
					}
	
					else
					{
						
						Set<Variable> newIR = new HashSet<>();
	
						//System.out.println(valueBox.toString()+para+t.getRD().getAllDef());
						for(String line:t.getRD().getLineNumForUse(n, para))
						{
							if(t.getTranslatedIR(line)!=null)
							newIR.addAll(t.getTranslatedIR(line));
						}
						
						
						
						returnSet.addAll(newIR);
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
					tempOperand.addAll( replaceExternal(operand,n,t));
				}
				newOperandList.add(tempOperand);
			}
 			((Expression) v).setOperands(newOperandList);
 			returnSet.add(v);
		}
		else if(v instanceof T)
		{
			((T) v).setVariable(replaceExternal(((T) v).getVariable(),n,t).iterator().next());
			returnSet.add(v);
		}
		else
			returnSet.add(v);
		
		return returnSet;
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
		System.out.println("Methods contain target APIs: "+ targetMethod.size());
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
		System.out.println("Target methods and callees: "+ targetMethod.size());
		
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
		System.out.println("Target methods and callers and callees: "+ targetMethod.size());
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
		System.out.println("Total Target Method including constructor: "+ targetMethod.size());
		return targetMethod;
	}
	
	public Map<String,Integer> getOperations(Set<Variable> vars)
	{		

		Map<String,Integer> opFreq = new HashMap<>();
		for(Variable v : vars)
			getOperations(v,opFreq);
		return opFreq;
	
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
	
}
