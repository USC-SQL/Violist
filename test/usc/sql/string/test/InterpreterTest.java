package usc.sql.string.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import usc.sql.ir.Variable;
import usc.sql.string.Interpreter;
import usc.sql.string.LayerRegion;
import usc.sql.string.ReachingDefinition;
import usc.sql.string.Translator;
import edu.usc.sql.graphs.cfg.BuildCFGs;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class InterpreterTest {
	
	private void InterpreterChecker(String p1,String p2, String p3,String folder,int loopcount,Map<String,List<String>> expected)
	{
		
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						p1,
						p2);
		CFGInterface cfg = result
				.get(p3);
		LayerRegion lll = new LayerRegion(null);
		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));	
		LayerRegion lr = new LayerRegion(cfg);
		Translator t = new Translator(rd, lr,p3,folder);
		Interpreter intp = new Interpreter(t,loopcount);
		
		Map<String,List<String>> output = new HashMap<>();
		
		for(Entry<String,List<String>> en:t.getTargetLines().entrySet())
		{
			//System.out.println("Label = "+en.getKey());
			List<String> value = new ArrayList<>();
			for(String line: en.getValue())
			{
			
				value.addAll(intp.getValue(line));
				
			}
			
			output.put(en.getKey(), value);
		}
		assertTrue(checkContain(output,expected));
		
	}
	private boolean checkContain(Map<String, List<String>> output,
			Map<String, List<String>> expected) {
		for(Entry<String,List<String>> en:expected.entrySet())
		{
		
			if(output.get(en.getKey())==null)
			{
				System.out.println("Label miss");
				return false;
			}
			else
			{
				if(!output.get(en.getKey()).containsAll(en.getValue()))
				{
					System.out.println("Not 100% recall");
					return false;
				}
			}
		}
		return true;
	}

	@Test
	public void testCircleLoop() {
		List<String> o = new ArrayList<>();
		o.add("cbaacbc");
		o.add("cbaacbcbaacbaacaaccbaacbcbaacbcbaacbc");
		o.add("cbaacbcbaacbaacaaccbaacbcbaacbcbaacbcbaacbaacaaccbaacbcbaacbaacaaccbaacbcaacaaccbaacbccbaacbcbaacbaacaaccbaacbcbaacbcbaacbcbaacbaacaaccbaacbcbaacbcbaacbcbaacbaacaaccbaacbcbaacbcbaacbc");
		Map<String,List<String>> map = new HashMap<>();
		map.put("\"CircleLoop\"", o);
		InterpreterChecker("./target/classes","usc/sql/string/testcase/CircleLoop.class","<usc.sql.string.testcase.CircleLoop: void main(java.lang.String[])>", "", 3, map );

	}
	@Test
	public void testNestLoop()
	{
		List<String> o = new ArrayList<>();
		o.add("a");
		o.add("aba");
		o.add("ababba");
		Map<String,List<String>> map = new HashMap<>();
		map.put("\"NestLoop\"", o);
		InterpreterChecker("./target/classes","usc/sql/string/testcase/NestLoop.class","<usc.sql.string.testcase.NestLoop: void main(java.lang.String[])>", "", 3, map );

	}

}
