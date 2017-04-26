package usc.sql.string.testfinal;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import usc.sql.string.Interpreter;
import usc.sql.string.LayerRegion;
import usc.sql.string.ReachingDefinition;
import usc.sql.string.Translator;
import SootEvironment.JavaApp;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class ToyAppTest {

	private void InterpretChecker(String arg0,String arg1,String arg2,String summaryFolder, String gtfolder,String wfolder,String test)
	{
		//"/home/yingjun/Documents/StringAnalysis/MethodSummary/"
		//"Usage: rt.jar app_folder classlist.txt"
		JavaApp App=new JavaApp(arg0,arg1,arg2,"void main(java.lang.String[])");

    	
		long totalTranslate = 0,totalInterpret = 0;
	
    	for(CFGInterface cfg:App.getCallgraph().getRTOInterface())
    	{
    		
    		
    		
    		//System.out.println(cfg.getSignature());
    		String signature=cfg.getSignature();
    		if(signature.contains("void <init>()")||signature.equals("<LoggerLib.Logger: void <clinit>()>")||signature.equals("<LoggerLib.Logger: void reportString(java.lang.String,java.lang.String)>"))
    		continue;
    		
    		int loopCount = 0;
    		
    		
    		String tempSig = signature.replaceAll("TestCases.", "");
    		int dot = tempSig.indexOf(".");
    		String tt = tempSig.substring(dot+1);
    		
    		if(tt.contains("Mix")||tt.contains("NestedLoop"))
    			loopCount = 2;
    		else
    			loopCount = 3;
    		
      		//for(int i=1;i<=loopCount;i++)
    		//{  		
    	
    		
    		long t1 = 	System.currentTimeMillis();
    		
    		LayerRegion lll = new LayerRegion(null);
    		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));	   		
    		
    		LayerRegion lr = new LayerRegion(cfg);
    	
    		//System.out.println(signature);
    		Translator t = new Translator(rd, lr,signature,summaryFolder);
    		
    		long t2 = System.currentTimeMillis();
    		
    		totalTranslate += t2-t1;
    		
    		/*

    			Interpreter intp = new Interpreter(t,i);
    			if(t.getTargetLines().isEmpty())
        			continue;
    			
    			Set<String> value = new HashSet<>();
    			int i1 = signature.indexOf("<"),i2 = signature.indexOf(":");
        		String label = "\""+signature.substring(i1+1,i2).replaceAll("TestCases.","")+"\"";
        		System.out.println(label);
        		for(String line: t.getTargetLines().get(label))
        		{
        			
        			value.addAll(intp.getValue(line));
        				
        		}
        		try
        		{
        		BufferedWriter bw = new BufferedWriter(new FileWriter(wfolder+label.replaceAll("\"","")+".txt",true));

        		for(String v:value)
        		{
        			bw.write(v+" ");
        			
        		}
        		bw.newLine();
        		
        		bw.close();
        		}
        		catch(IOException e)
        		{
        			e.printStackTrace();
        		}
        		
    		}
    		*/
    		
    		long t3 = System.currentTimeMillis();
    		
    		Interpreter intp = new Interpreter(t,loopCount);
    		
    		if(t.getTargetLines().isEmpty())
    			continue;
    		
    		Set<String> value = new HashSet<>();
    	
    		
    		int i1 = signature.indexOf("<"),i2 = signature.indexOf(":");
    		String label = "\""+signature.substring(i1+1,i2).replaceAll("TestCases.","")+"\"";
    		//System.out.println(label);
    		
    		
    		for(String line: t.getTargetLines().get(label))
    		{
    			
    			value.addAll(intp.getValue(line));
    				
    		}
    		long t4 = System.currentTimeMillis();
    		
    		totalInterpret += t4-t3;
    		
    		
    		List<String> gt = new ArrayList<>();
    		try
    		{
    		
    		BufferedReader br = new BufferedReader(new FileReader(gtfolder+signature.substring(i1+1,i2).replaceAll("TestCases.","")+".gt"));
    		 
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
    		assertTrue(rec==1);
    	
    	}
    	
    
    	System.out.println("Total Trans: "+ totalTranslate);
    	System.out.println("Total Interp: "+ totalInterpret);
	}
	String rtjar = "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar";
	String appfolder = "/home/yingjun/Documents/StringAnalysis/Testing";	
	String gt = "/home/yingjun/Documents/eclipse/workspace/StringTestCases/groundtruth/";
	
	@Test
	public void test() {	
		//String name = "Concat";
		InterpretChecker(rtjar,appfolder,appfolder+"/classlist.txt",
			appfolder+"/MethodSummary/",gt,appfolder+"/TestCases/"+"/Output/","Concat");		
	}

}
