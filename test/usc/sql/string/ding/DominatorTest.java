package usc.sql.string.ding;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cdg.Dominator;
import edu.usc.sql.graphs.cfg.BuildCFGs;
import edu.usc.sql.graphs.cfg.CFGInterface;
public class DominatorTest {
	private void SingleParentDominatesChecker(String p1,String p2, String p3)
	{
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						p1,
						p2);
		CFGInterface cfg = result
				.get(p3);
		Dominator pd = new Dominator(cfg.getAllNodes(),cfg.getAllEdges(),cfg.getEntryNode());
		pd.computeDominator();
		for(NodeInterface node : cfg.getAllNodes())
		{
			Set<EdgeInterface> outedges = node.getOutEdges();
			for(EdgeInterface e: outedges)
			{
				NodeInterface child=e.getDestination();
				int numberinedges = child.getInEdges().size();
				if(numberinedges==1)
				{
					assertTrue(pd.isDominate(node, child));
				}
			}
		}
	}
	@Test
	public void testSingleParentDominates1() {// if one node has only one parent, its parent should dominate itself
		SingleParentDominatesChecker("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");

	}
	@Test
	public void testSingleParentDominates2() {// if one node has only one parent, its parent should dominate itself
		SingleParentDominatesChecker("./target/classes","usc/sql/string/testcase/Switch.class","<usc.sql.string.testcase.Switch: void main(java.lang.String[])>");

	}
	private void MultiParentNotDominatesChecker(String s1,String s2, String s3)
	{
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						s1,
						s2);
		CFGInterface cfg = result
				.get(s3);
		
		Dominator pd = new Dominator(cfg.getAllNodes(),cfg.getAllEdges(),cfg.getEntryNode());
		pd.computeDominator();
		for(NodeInterface node : cfg.getAllNodes())
		{
			Set<EdgeInterface> inedges = node.getInEdges();

			Set<NodeInterface> parents=new HashSet<NodeInterface>();
			Set<NodeInterface> remove=new HashSet<NodeInterface>();
			for(EdgeInterface e: inedges)
			{
				parents.add(e.getSource());
			}
			for(NodeInterface p1: parents)
			{
				boolean flag=true;
				for(NodeInterface p2: parents)
				{
					if(!pd.isDominate(p1, p2))
						flag=false;
				}
				if(flag)
					remove.add(p1);
			}
			parents.removeAll(remove);
			for(NodeInterface p1: parents)
			{
				assertTrue(!pd.isDominate(p1, node));
			}
		}
	}
	@Test
	public void testMultiParentNotDominates1() {// if one node has more than one parents, one of its parent should not dominate the child
		MultiParentNotDominatesChecker("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");

	}
	@Test
	public void testMultiParentNotDominates2() {// if one node has more than one parents, one of its parent should not dominate the child
		MultiParentNotDominatesChecker("./target/classes","usc/sql/string/testcase/Switch.class","<usc.sql.string.testcase.Switch: void main(java.lang.String[])>");

	}
	public void EntryDominatesAllChecker(String s1,String s2, String s3)
	{
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						s1,
						s2);
		CFGInterface cfg = result
				.get(s3);
		
		Dominator pd = new Dominator(cfg.getAllNodes(),cfg.getAllEdges(),cfg.getEntryNode());
		pd.computeDominator();
		NodeInterface entry=cfg.getEntryNode();
		for(NodeInterface node : cfg.getAllNodes())
		{
			assertTrue(pd.isDominate(entry, node));

		}
	}
	@Test
	public void testEntryDominatesAll1() {// entry dominates all nodes
		EntryDominatesAllChecker("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");
	}
	@Test
	public void testEntryDominatesAll2() {// entry dominates all nodes
		EntryDominatesAllChecker("./target/classes","usc/sql/string/testcase/Switch.class","<usc.sql.string.testcase.Switch: void main(java.lang.String[])>");
	}
	@Test

	public void testSpecifc1() {// entry dominates all nodes
		String nodesig1="r1 = virtualinvoke r1.<java.lang.String: java.lang.String replaceAll(java.lang.String,java.lang.String)>(r2, r3)";
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						"./target/classes",
						"usc/sql/string/testcase/TestCase1.class");
		CFGInterface cfg = result
				.get("<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");
		
		Dominator pd = new Dominator(cfg.getAllNodes(),cfg.getAllEdges(),cfg.getEntryNode());
		pd.computeDominator();
		NodeInterface node1;
		List<NodeInterface> nodelist=new ArrayList<NodeInterface>();
		for(NodeInterface node : cfg.getAllNodes())
		{
			nodelist.add(node);
		}
		Collections.sort(nodelist, new Comparator<NodeInterface>(){
			public int compare(NodeInterface n1, NodeInterface n2){
				String s1=n1.getNodeContent();
				String s2=n2.getNodeContent();
				return s1.compareTo(s2);
				
			}
		});
		assertTrue(pd.isDominate(nodelist.get(25), nodelist.get(3)));
		assertTrue(pd.isDominate(nodelist.get(25), nodelist.get(0)));
		assertTrue(!pd.isDominate(nodelist.get(6), nodelist.get(25)));
		assertTrue(!pd.isDominate(nodelist.get(15), nodelist.get(3)));
		assertTrue(!pd.isDominate(nodelist.get(17), nodelist.get(3)));
		assertTrue(!pd.isDominate(nodelist.get(19), nodelist.get(3)));



	}
	@Test

	public void testSpecifc2() {// entry dominates all nodes
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						"./target/classes",
						"usc/sql/string/testcase/Switch.class");
		CFGInterface cfg = result
				.get("<usc.sql.string.testcase.Switch: void main(java.lang.String[])>");
		
		Dominator pd = new Dominator(cfg.getAllNodes(),cfg.getAllEdges(),cfg.getEntryNode());
		pd.computeDominator();
		NodeInterface node1;
		List<NodeInterface> nodelist=new ArrayList<NodeInterface>();
		for(NodeInterface node : cfg.getAllNodes())
		{
			nodelist.add(node);
		}
		Collections.sort(nodelist, new Comparator<NodeInterface>(){
			public int compare(NodeInterface n1, NodeInterface n2){
				String s1=n1.getNodeContent();
				String s2=n2.getNodeContent();
				return s1.compareTo(s2);
				
			}
		});
		for(int i=0; i< nodelist.size();i++)
		{
			System.out.println(i+" "+nodelist.get(i).getNodeContent());
		}
		assertTrue(!pd.isDominate(nodelist.get(12), nodelist.get(3)));
		assertTrue(!pd.isDominate(nodelist.get(0), nodelist.get(5)));
		assertTrue(pd.isDominate(nodelist.get(22), nodelist.get(5)));






	}

}
