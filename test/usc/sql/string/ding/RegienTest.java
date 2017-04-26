package usc.sql.string.ding;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

import usc.sql.string.LayerRegion;
import usc.sql.string.RegionNode;
import edu.usc.sql.graphs.cfg.BuildCFGs;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class RegienTest {
	private Set<RegionNode> GetAllRegionNodes(String p1,String p2, String p3)
	{
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						p1,
						p2);
		CFGInterface cfg = result
				.get(p3);
		LayerRegion lr=new LayerRegion(cfg);
		RegionNode root=lr.getRoot();
		Queue<RegionNode> q=new LinkedList<RegionNode>();
		Set<RegionNode> visited=new HashSet<RegionNode>();
		q.add(root);
		int cnt=0;
		while(!q.isEmpty()){
			RegionNode top=q.poll();
			cnt++;
			List<RegionNode> children=top.getChildren();
			visited.add(top);
			for(int i=0;i<children.size();i++)
			{
				if(!visited.contains(children.get(i)))
					q.add(children.get(i));
			}
			
			
			
		}
		return visited;
	}
	@Test
	public void testNumberofRegienNodes1() {
		int num=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>").size();
		assertTrue(num==4);
	}
	@Test
	public void testNumberofRegienNodes2() {
		int num=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase2.class","<usc.sql.string.testcase.TestCase2: void main(java.lang.String[])>").size();
		assertTrue(num==6);
	}
	@Test
	public void testNumberofRegienNodes3() {
		int num=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/Switch.class","<usc.sql.string.testcase.Switch: void main(java.lang.String[])>").size();
		assertTrue(num==1);
	}
	@Test
	public void testNumberofRegienNodes4() {
		int num=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase3.class","<usc.sql.string.testcase.TestCase3: void main(java.lang.String[])>").size();
		assertTrue(num==9);
	}


	private void RegienNodesTraverseChecker(String p1,String p2, String p3)
	{
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						p1,
						p2);
		CFGInterface cfg = result
				.get(p3);
		LayerRegion lr=new LayerRegion(cfg);
		RegionNode root=lr.getRoot();
		Queue<RegionNode> q=new LinkedList<RegionNode>();
		Set<RegionNode> visited=new HashSet<RegionNode>();
		q.add(root);
		int cnt=0;
		while(!q.isEmpty()){
			RegionNode top=q.poll();
			cnt++;
			List<RegionNode> children=top.getChildren();
			visited.add(top);
			for(int i=0;i<children.size();i++)
			{
				if(!visited.contains(children.get(i)))
					q.add(children.get(i));
				else{
					Assert.fail("There should not have loop in Region tree");
				}
			}
			
			
			
		}
	}
	@Test
	public void testRegienNodesTraverse1() {
		RegienNodesTraverseChecker("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");
	}
	@Test
	public void testRegienNodesTraverse2() {
		RegienNodesTraverseChecker("./target/classes","usc/sql/string/testcase/TestCase2.class","<usc.sql.string.testcase.TestCase2: void main(java.lang.String[])>");
	}
	@Test
	public void testRegienNodesTraverse3() {
		RegienNodesTraverseChecker("./target/classes","usc/sql/string/testcase/TestCase3.class","<usc.sql.string.testcase.TestCase3: void main(java.lang.String[])>");
	}
	public void CompareTemplate(int[] template,Set<RegionNode> all)
	{
		Iterator<RegionNode> ir=all.iterator();
		ArrayList<Integer> vector=new ArrayList<Integer>();
		while(ir.hasNext())
		{
			RegionNode next=ir.next();
			vector.add(next.getNodeList().size());
		}
		Collections.sort(vector);
		for(int i=0;i<vector.size();i++)
		{
			assertTrue(vector.get(i)==template[i]);
		}
	}
	@Test
	public void testRegienNodesInstruction1() {
		Set<RegionNode> all=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");
		int[] template={7,7,17,36};
		CompareTemplate(template,all);
	}
	@Test
	public void testRegienNodesInstruction2() {
		Set<RegionNode> all=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase2.class","<usc.sql.string.testcase.TestCase2: void main(java.lang.String[])>");
		int[] template={7,7,9,28,38,57};
		CompareTemplate(template,all);
	}
	@Test
	public void testRegienNodesInstruction3() {
		Set<RegionNode> all=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/Switch.class","<usc.sql.string.testcase.Switch: void main(java.lang.String[])>");
		int[] template={37};
		CompareTemplate(template,all);

	}
	@Test
	public void testRegienNodesInstruction4() {
		Set<RegionNode> all=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase3.class","<usc.sql.string.testcase.TestCase3: void main(java.lang.String[])>");
		int[] template={7,7,7,9,9,17,71,81,100};
		CompareTemplate(template,all);

	}
	private int RegionTreeDepthChecker(String p1,String p2, String p3)
	{
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						p1,
						p2);
		CFGInterface cfg = result
				.get(p3);
		LayerRegion lr=new LayerRegion(cfg);
		RegionNode root=lr.getRoot();
		int depth=getDepth(root);
		return depth;

	}
	private int getDepth(RegionNode root)
	{
		List<RegionNode> children=root.getChildren();
		int max=0;
		for(int i=0;i<children.size();i++)
		{
			int depth=getDepth(children.get(i));
			if(depth>max)
			{
				max=depth;
			}
		}
		return max+1;

	}
	@Test
	public void testRegienTreeDepth1() {
		int depth=RegionTreeDepthChecker("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");
		assertTrue(depth==3);
	}
	@Test
	public void testRegienTreeDepth2() {
		int depth=RegionTreeDepthChecker("./target/classes","usc/sql/string/testcase/TestCase2.class","<usc.sql.string.testcase.TestCase2: void main(java.lang.String[])>");
		assertTrue(depth==4);
	}
	@Test
	public void testRegienTreeDepth3() {
		int depth=RegionTreeDepthChecker("./target/classes","usc/sql/string/testcase/Switch.class","<usc.sql.string.testcase.Switch: void main(java.lang.String[])>");
		assertTrue(depth==1);

	}
	@Test
	public void testRegienTreeDepth4() {
		int depth=RegionTreeDepthChecker("./target/classes","usc/sql/string/testcase/TestCase3.class","<usc.sql.string.testcase.TestCase3: void main(java.lang.String[])>");
		assertTrue(depth==4);

	}
	private Set<RegionNode> GetRegionTreeFirstLayer(String p1,String p2, String p3)
	{
		Map<String, CFGInterface> result = BuildCFGs
				.buildCFGs(
						p1,
						p2);
		CFGInterface cfg = result
				.get(p3);
		LayerRegion lr=new LayerRegion(cfg);
		RegionNode root=lr.getRoot();
		List<RegionNode> children=root.getChildren();
		Set<RegionNode> all=new HashSet<RegionNode>();
		all.addAll(children);

		return all;
	}
	@Test
	public void testRegienTreeFirstLayer1() {
		Set<RegionNode> all=GetRegionTreeFirstLayer("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");
		int[] template={7,17};
		CompareTemplate(template,all);
	}
	@Test
	public void testRegienTreeFirstLayer2() {
		Set<RegionNode> all=GetRegionTreeFirstLayer("./target/classes","usc/sql/string/testcase/TestCase2.class","<usc.sql.string.testcase.TestCase2: void main(java.lang.String[])>");
		int[] template={7,38};
		CompareTemplate(template,all);
	}
	@Test
	public void testRegienTreeFirstLayer3() {
		Set<RegionNode> all=GetRegionTreeFirstLayer("./target/classes","usc/sql/string/testcase/Switch.class","<usc.sql.string.testcase.Switch: void main(java.lang.String[])>");
		int[] template={};
		CompareTemplate(template,all);

	}
	@Test
	public void testRegienTreeFirstLayer4() {
		Set<RegionNode> all=GetRegionTreeFirstLayer("./target/classes","usc/sql/string/testcase/TestCase3.class","<usc.sql.string.testcase.TestCase3: void main(java.lang.String[])>");
		int[] template={7,81};
		CompareTemplate(template,all);

	}
	private void CkeckNodeTopology(RegionNode rn, int childnum,int checksum)
	{
		List<RegionNode> children=rn.getChildren();
		assertTrue(children.size()==childnum);
		int sum=0;
		for(RegionNode c:children)
		{
			sum+=c.getNodeList().size();
		}
		assertTrue(sum==checksum);
	}
	@Test
	public void testRegienTreeTopology1() {
		Set<RegionNode> all=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase1.class","<usc.sql.string.testcase.TestCase1: void main(java.lang.String[])>");
		for(RegionNode rn:all)
		{
			//System.out.println(rn.getNodeList().size());
			if(rn.getNodeList().size()==36){
				CkeckNodeTopology(rn,2,24);
			}
			else if(rn.getNodeList().size()==17){
				CkeckNodeTopology(rn,1,7);
			}
			else if(rn.getNodeList().size()==7){
				CkeckNodeTopology(rn,0,0);

			}
			else{
				Assert.fail("Unknown node");

			}
		}

	}
	@Test
	public void testRegienTreeTopology2() {
		Set<RegionNode> all=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase2.class","<usc.sql.string.testcase.TestCase2: void main(java.lang.String[])>");
		for(RegionNode rn:all)
		{
			//System.out.println(rn.getNodeList().size());
			if(rn.getNodeList().size()==57){
				CkeckNodeTopology(rn,2,45);

			}
			else if(rn.getNodeList().size()==38){
				CkeckNodeTopology(rn,1,28);

			}
			else if(rn.getNodeList().size()==28){
				CkeckNodeTopology(rn,2,16);

			}
			else if(rn.getNodeList().size()==9){
				CkeckNodeTopology(rn,0,0);


			}
			else if(rn.getNodeList().size()==7){
				CkeckNodeTopology(rn,0,0);


			}
			else{
				Assert.fail("Unknown node");

			}
		}

	}
	@Test
	public void testRegienTreeTopology3() {
		Set<RegionNode> all=GetAllRegionNodes("./target/classes","usc/sql/string/testcase/TestCase3.class","<usc.sql.string.testcase.TestCase3: void main(java.lang.String[])>");
		for(RegionNode rn:all)
		{
			//System.out.println(rn.getNodeList().size());
			if(rn.getNodeList().size()==100){
				CkeckNodeTopology(rn,2,88);
			}
			else if(rn.getNodeList().size()==81){
				CkeckNodeTopology(rn,1,71);
			}
			else if(rn.getNodeList().size()==71){
				CkeckNodeTopology(rn,5,49);
			}
			else if(rn.getNodeList().size()==7){
				CkeckNodeTopology(rn,0,0);
			}
			else if(rn.getNodeList().size()==9){
				CkeckNodeTopology(rn,0,0);
			}
			else if(rn.getNodeList().size()==17){
				CkeckNodeTopology(rn,0,0);
			}
			else{
				Assert.fail("Unknown node");

			}
		}

	}

}
