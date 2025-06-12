package usc.sql.string.testfinal;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import soot.Unit;
import soot.ValueBox;
import usc.sql.ir.ConstantInt;
import usc.sql.ir.ConstantString;
import usc.sql.ir.Expression;
import usc.sql.ir.ExternalPara;
import usc.sql.ir.Init;
import usc.sql.ir.InternalVar;
import usc.sql.ir.T;
import usc.sql.ir.Variable;
import usc.sql.string.Interpreter;
import usc.sql.string.LayerRegion;
import usc.sql.string.ReachingDefinition;
import usc.sql.string.Translator;
import SootEvironment.JavaApp;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class JSABenchmarkTest {


	
	
	
	private void InterpretChecker(String arg0,String arg1,String arg2,String summaryFolder, String gtfolder,String wfolder)
	{
		//"/home/yingjun/Documents/StringAnalysis/MethodSummary/"
		//"Usage: rt.jar app_folder classlist.txt"
		JavaApp App=new JavaApp(arg0,arg1,arg2,"void main(java.lang.String[])");
		
		Map<String,Set<Variable>> targetMap = new HashMap<>();
    	Map<String,Set<NodeInterface>> paraMap = new HashMap<>();
    	Map<String,Set<String>> fieldMap = new HashMap<>();
		Map<String,Translator> tMap = new HashMap<>();
		long totalTranslate = 0,totalInterpret = 0;
		
		
		long t1,t2;
    	for(CFGInterface cfg:App.getCallgraph().getRTOInterface())
    	{
    		System.out.println(cfg.getSignature());
    		String signature=cfg.getSignature();
    		
    		//if(!signature.contains("testcases.standalone.MethodCall:"))
    		///	continue;
    		
    		
    		if(signature.equals("<LoggerLib.Logger: void <clinit>()>")||signature.equals("<LoggerLib.Logger: void reportString(java.lang.String,java.lang.String)>"))
    			continue;
    		
    		//field																	def missing						
    		if(signature.contains("OverriddenToString"))
    			continue;
    		int loopCount = 0;
    		
    		
    		/*
    		String tempSig = signature.replaceAll("TestCases.", "");
    		int dot = tempSig.indexOf(".");
    		String tt = tempSig.substring(dot+1);
    		
    		if(tt.contains("Mix")||tt.contains("NestedLoop"))
    			loopCount = 2;
    		else
    			loopCount = 3;
    		*/
    		
      		//for(int i=1;i<=loopCount;i++)
    		//{  		
    		
    		
    		t1 = System.currentTimeMillis();
    		LayerRegion lll = new LayerRegion(null);
    		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));	   		
    
    		
    		LayerRegion lr = new LayerRegion(cfg);
    	
    		//System.out.println(signature);
    		Translator t = new Translator(rd, lr,signature,summaryFolder,App);
    	
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
    	
    		
    		int i1 = signature.indexOf("<"),i2 = signature.indexOf(":");
    		String label = "\""+signature.substring(i1+1,i2).replaceAll("testcases.standalone.","")+"\"";
    		//System.out.println(label);
    		
    		//Interpreter intp = new Interpreter(t,loopCount);
    		
    		Set<Variable> targetIR = new HashSet<>();
    		
    		for(String line: t.getTargetLines().get(label))
    		{
    			if(t.getTranslatedIR(line)!=null)
    				targetIR.addAll(t.getTranslatedIR(line));
    			//value.addAll(intp.getValue(line));
    				
    		}
    		
    		if(!targetMap.containsKey(signature))
    			targetMap.put(signature, targetIR);
    	 		
    		t2 = System.currentTimeMillis();
    		totalTranslate += t2-t1;
    	
    	}
    	int count = 0;
    	for(Entry<String,Set<Variable>> en: targetMap.entrySet())
    	{
    		String signature = en.getKey();
    		int i1 = signature.indexOf("<"),i2 = signature.indexOf(":");
    		String label = "\""+signature.substring(i1+1,i2).replaceAll("testcases.standalone.","")+"\"";
    		
    		//System.out.println("\n"+signature);
    		
    	
    		t1 = System.currentTimeMillis();
    		Set<Variable> newIR = replaceExternal(en.getValue(),signature,paraMap,tMap,App);
    		t2 = System.currentTimeMillis();
    		totalTranslate += t2-t1;
    		
    		
    		t1 = System.currentTimeMillis();
			Interpreter intp = new Interpreter(newIR,fieldMap,3);
			Set<String> value = new HashSet<>();
			value.addAll(intp.getValueForIR());

    		t2 = System.currentTimeMillis();
    		totalInterpret += t2-t1;
    		
    		List<String> gt = new ArrayList<>();
    		try
    		{
    		
    		BufferedReader br = new BufferedReader(new FileReader(gtfolder+signature.substring(i1+1,i2).replaceAll("testcases.standalone.","")+".gt"));
    		 
    		String line = null;
    		while ((line = br.readLine()) != null) {
    			gt.add(line);
    		}
    	 
    		br.close();
    		}
    		catch(IOException e)
    		{
    			e.printStackTrace();
    		}
    		
    		if(!signature.equals("<testcases.standalone.FieldUninitialized: void foo()>")&&!signature.equals("<testcases.standalone.SubstringOfNull: void foo(boolean,boolean)>")&&!signature.equals("<testcases.standalone.ArrayNewContainsNull: void foo()>"))
    			value.remove("null");
    		//System.out.println("Output: "+value);
    		
    		double pre=0;
    		int nomi=0,deno=value.size();
    		if(deno==0)
    			System.out.println("Empty output");
    		else
    		{
	    		for(String s:gt)
	    		{
	    			if(value.contains(s))
	    				nomi++;
	    		}
	    		pre = nomi*1.0/deno;
	    		System.out.println("Presion: "+pre*100+"%");
    		}
    		double rec=0;
    		nomi = 0;
    		deno = gt.size();
    		if(deno==0)
    			System.out.println("Empty ground truth");
    		else
    		{
	    		for(String s:gt)
	    		{
	    			if(value.contains(s))
	    				nomi++;
	    		}
	    		rec = nomi*1.0/deno;
	    		System.out.println("Recall: "+rec*100+"%");
	    		if(rec==1)
	    			count++;
    		}
    		
    		try
    		{
    			BufferedWriter bw = new BufferedWriter(new FileWriter(wfolder+"output.txt",true));
    			bw.write(label);
    			bw.newLine();
    			bw.write("Precision:"+pre*100+"%");
    			bw.newLine();
    			bw.write("Recall:"+rec*100+"%");
    			bw.newLine();
    			bw.newLine();
    			bw.flush();
    			bw.close();
    		}
    		catch(IOException e)
    		{
    			e.printStackTrace();
    		}
    		
    	}


    	System.out.println(count);
    	assertTrue(true); 
    	System.out.println("Total Trans: "+ totalTranslate);
    	System.out.println("Total Interp: "+ totalInterpret);
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
					tempOp.add(copyVar(operand));
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

		else
			return v;
	}
	
	private Set<Variable> replaceExternal(Set<Variable> IRs,String signature,Map<String,Set<NodeInterface>> paraMap,Map<String,Translator> tMap,JavaApp App)
	{
		Set<Variable> vSet = new HashSet<>();
		for(Variable v: IRs)
		{
			if(paraMap.get(signature)==null)
				vSet.add(v);
			else
			{
				for(NodeInterface n:paraMap.get(signature))
				{
	    			Set<Variable> newIR = new HashSet<>();
	    				if(App.getCallgraph().getParents(signature).isEmpty())
	    					newIR.add(copyVar(v));
	    				else
	    				{
	    				String parentSig = App.getCallgraph().getParents(signature).iterator().next();
	    				newIR.addAll(replaceExternal(copyVar(v),n,tMap.get(parentSig)));
	    				}
	    			vSet.addAll(newIR);
	    		}				
			}			
		}
		boolean existPara = false;
		for(Variable v:vSet)
		{
			if(containPara(v))
				existPara = true;
		}
		if(!existPara)
			return vSet;
		else
		{
			if(App.getCallgraph().getParents(signature).isEmpty())
				return vSet;
			else
			{
				String parentSig = App.getCallgraph().getParents(signature).iterator().next();
				if(paraMap.get(parentSig)==null)
					return vSet;
				else
				{
					Set<Variable> copy = new HashSet<>();
					for(Variable vv:vSet)
						copy.add(copyVar(vv));
					Set<Variable> newIR = new HashSet<>();
					newIR.addAll(replaceExternal(copy,parentSig, paraMap, tMap, App));
					return newIR;
				}
			}
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
				
				String para = valueBox.get(index).getValue().toString();
				
				
				if(para.contains("\""))
					returnSet.add( new ConstantString(para));
				else if(valueBox.get(index).getValue().getType().toString().equals("int"))
				{
					//System.out.println("YOYOYO"+para);
					returnSet.add(new ConstantInt(para));
					//returnSet.add( new ConstantString(""+(char)Integer.parseInt(para)));
				}

				else
				{
					System.out.println("Type"+valueBox.get(index).getValue().getType().toString());
					System.out.println(para);
					Set<Variable> newIR = new HashSet<>();

					for(String line:t.getRD().getLineNumForUse(n, para))
						newIR.addAll(t.getTranslatedIR(line));
					
					System.out.println("newIR:"+newIR);
					Interpreter ip = new Interpreter(newIR,3);
					System.out.println("Outcome: "+ip.getValueForIR());
					
					
					
					returnSet.addAll(newIR);
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
	
	
	
	
	
	
	String rtjar = "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar";
	String appfolder = "/home/yingjun/Documents/StringAnalysis/JSATesting";	
	String gt = "/home/yingjun/Documents/eclipse/workspace/StringTestCases/JSAcase/groundtruth/";
	
	@Test
	public void test() {	
		//String name = "Concat";
		InterpretChecker(rtjar,appfolder,appfolder+"/classlist.txt",
			appfolder+"/MethodSummary/",gt,appfolder+"/Output/");
	
		
	}

}
