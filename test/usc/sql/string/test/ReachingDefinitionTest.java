package usc.sql.string.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.BuildCFGs;
import edu.usc.sql.graphs.cfg.CFGInterface;
import usc.sql.string.LayerRegion;
import usc.sql.string.ReachingDefinition;

public class ReachingDefinitionTest {

	private static CFGInterface cfg;
	private static List<NodeInterface> testNodes = new ArrayList<>();
	private static Map<String,Map<String,String>> lineVarName = new HashMap<>();
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						"./target/classes",
						"usc/sql/string/testcase/TestCFG.class");
		cfg = result
				.get("<usc.sql.string.testcase.TestCFG: void main(java.lang.String[])>");
		
		List<String> testNodeName = new ArrayList<>();
		testNodeName.add("10"); //normal node
		Map<String,String> toLineVarName = new HashMap<>();
		toLineVarName.put("7","r0");
		toLineVarName.put("9","r1");
		toLineVarName.put("10","r2");
		lineVarName.put("10", toLineVarName);
		
		toLineVarName = new HashMap<>();
		testNodeName.add("13"); //normal node
		toLineVarName.put("7","r0");
		toLineVarName.put("9","r1");
		toLineVarName.put("10","r2");
		toLineVarName.put("11","r3");
		toLineVarName.put("12","r4");
		toLineVarName.put("13","i0");
		lineVarName.put("13", toLineVarName);
		
		toLineVarName = new HashMap<>();
		testNodeName.add("16"); //loop node
		toLineVarName.put("7","r0");
		toLineVarName.put("9","r1");	
		toLineVarName.put("10","r2");
		toLineVarName.put("11","r3");
		toLineVarName.put("12","r4");	
		toLineVarName.put("16","$r5");
		toLineVarName.put("13","i0");
		toLineVarName.put("17","$r6");	
		toLineVarName.put("19","$r7");
		toLineVarName.put("20","r2");
		toLineVarName.put("21","i0");	
		lineVarName.put("16", toLineVarName);
		
		/*
		testNodeName.add("35"); //nested loop node
		toLineVarName.put("","");
		toLineVarName.put("","");
		toLineVarName.put("","");
		lineVarName.put("35", toLineVarName);
		*/
	
		
		for(NodeInterface n:cfg.getAllNodes())
		{
			if(testNodeName.contains(n.getOffset()))
					testNodes.add(n);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		LayerRegion lll = new LayerRegion(null);
		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));

		rd.computeReachingDefinition();
		for(NodeInterface n: testNodes)
		{
			System.out.println();
			assertTrue(compareTwoMap(rd.getOutSet(n),lineVarName.get(n.getOffset())));
		}
	}
	
	private boolean compareTwoMap(Map<String,String> result,Map<String,String> oracle)
	{
		boolean equal = true;
		//System.out.println(result.keySet().size()+ " "+oracle.keySet().size());
	     if(!(result.keySet().containsAll(oracle.keySet())&&oracle.keySet().containsAll(result.keySet())))
			equal = false;
			if(!(result.values().containsAll(oracle.values())&&oracle.values().containsAll(result.values())))
			equal = false;
		for(String r:result.keySet())
		{
			System.out.println(result.get(r)+" "+oracle.get(r));
			if(!result.get(r).equals(oracle.get(r)))
				equal = false;
		}
		return equal;
	}

}
