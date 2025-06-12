package usc.sql.string.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import usc.sql.string.LayerRegion;
import edu.usc.sql.graphs.Edge;
import edu.usc.sql.graphs.EdgeInterface;
import edu.usc.sql.graphs.Graph;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;


public class BackEdgeTest1 {
	private static Graph testGraph;
	private static List<Edge> backedgeOracle = new ArrayList<>();
	private static List<Edge> backedgeResult = new ArrayList<>();
	private static Node entry;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testGraph = new Graph();

		Node A = new Node("A");
		Node B = new Node("B");
		Node C = new Node("C");
		Node D = new Node("D");
		Node E = new Node("E");
		Node F = new Node("F");
		Node G = new Node("G");
		Node R = new Node("R");
		Node Entry = new Node("entry");
		Node Exit = new Node("exit");
		
		entry = Entry;

		Edge E1 = new Edge(Entry,A);
		Edge E2 = new Edge(A,B);
		Edge E3 = new Edge(B,C);
		Edge E4 = new Edge(C,D);
		Edge E5 = new Edge(D,E);
		Edge E6 = new Edge(D,F);
		Edge E7 = new Edge(E,Exit);
//		Edge E8 = new Edge(E,G);
		Edge E8 = new Edge(E,B);
		
		Edge E9 = new Edge(F,G);
		Edge E10 = new Edge(G,Exit);
		
		
		Entry.addOutEdge(E1);
		A.addInEdge(E1);
		A.addOutEdge(E2);
		B.addInEdge(E2);
		
		B.addInEdge(E8);
		E.addOutEdge(E8);
		backedgeOracle.add(E8);
		
		B.addOutEdge(E3);
		C.addInEdge(E3);
		C.addOutEdge(E4);
		D.addInEdge(E4);
		D.addOutEdge(E5);
		D.addOutEdge(E6);
		E.addInEdge(E5);
		E.addOutEdge(E7);
//		E.addOutEdge(E8);
		F.addInEdge(E6);
		F.addOutEdge(E9);
//		G.addInEdge(E8);
		G.addInEdge(E9);
		G.addOutEdge(E10);
		Exit.addInEdge(E10);

		testGraph.addNode(A);
		testGraph.addNode(B);
		testGraph.addNode(C);
		testGraph.addNode(D);
		testGraph.addNode(E);
		testGraph.addNode(F);
		testGraph.addNode(G);
		testGraph.addNode(Entry);
		testGraph.addNode(Exit);
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

	private boolean compare(List<Edge> backedgeOracle, List<Edge> backedgeResult)
	{
		return(backedgeOracle.containsAll(backedgeResult)&&backedgeResult.containsAll(backedgeOracle));
	}
	
	//Test if else branch would affect the result
	@Test
	public void test() {
		LayerRegion lr = new LayerRegion(null);
		for(EdgeInterface bg: lr.identifyBackEdges(testGraph.getAllNodes(), testGraph.getAllEdges(), entry))
		{
			backedgeResult.add((Edge) bg);
		}
		assertTrue(compare(backedgeOracle,backedgeResult));
	}

}
