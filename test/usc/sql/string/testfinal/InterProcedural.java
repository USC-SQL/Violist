package usc.sql.string.testfinal;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class InterProcedural {

	private void InterpretChecker(String arg0,String arg1,String arg2,String summaryFolder, int loopCount, String label, String gtfolder)
	{
		//"/home/yingjun/Documents/StringAnalysis/MethodSummary/"
		//"Usage: rt.jar app_folder classlist.txt"
		JavaApp App=new JavaApp(arg0,arg1,arg2,"void main(java.lang.String[])");

    	
    	for(CFGInterface cfg:App.getCallgraph().getRTOInterface())
    	{
    		String signature=cfg.getSignature();
    		if(signature.equals("<LoggerLib.Logger: void <clinit>()>")||signature.equals("<LoggerLib.Logger: void reportString(java.lang.String,java.lang.String)>"))
    		continue;
    		LayerRegion lll = new LayerRegion(null);
    		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));	   		
    		
    		LayerRegion lr = new LayerRegion(cfg);
    	
    		System.out.println(signature);
    		Translator t = new Translator(rd, lr,signature,summaryFolder);
    		
    		Interpreter intp = new Interpreter(t,loopCount);
    		
    		if(t.getTargetLines().isEmpty())
    			continue;
    		
    		List<String> value = new ArrayList<>();
    		for(String line: t.getTargetLines().get(label))
    		{
    			
    			value.addAll(intp.getValue(line));
    				
    		}
    		List<String> gt = new ArrayList<>();
    		try
    		{
    		
    		BufferedReader br = new BufferedReader(new FileReader(gtfolder));
    		 
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
    		
    		assertTrue(rec==1);
    	}
    	
    	
    	
	}

	String rtjar = "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar";
	String appfolder = "/home/yingjun/Documents/StringAnalysis/Testing/";	
	String gt = "/home/yingjun/Documents/eclipse/workspace/StringTestCases/groundtruth/";
	
		
	String concatfolder = appfolder+"Concat/";
	String concatgt = gt + "Concat/";


	@Test
	public void testInterPro()
	{
		String name = "InterProcedural";
		InterpretChecker(rtjar,concatfolder+name,concatfolder+name+"/classlist.txt",
				concatfolder+"/MethodSummary/",3,"\""+name+"\"",gt+"/Concat."+name+".gt");
		
	}

}
