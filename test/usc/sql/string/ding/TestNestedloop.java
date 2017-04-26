package usc.sql.string.ding;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import usc.sql.ir.*;
import usc.sql.string.LayerRegion;
import usc.sql.string.ReachingDefinition;
import usc.sql.string.Translator;
import edu.usc.sql.graphs.cfg.BuildCFGs;
import edu.usc.sql.graphs.cfg.CFGInterface;

public class TestNestedloop {

	/*
	private void TranslatedIRChecker(String p1,String p2, String p3,String line,ArrayList<Variable> v)
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
		Translator t = new Translator(rd, lr,p3);
		assertTrue(checkEquality(t.getTranslatedIR(line),v));
		
	}

	private boolean checkEquality(ArrayList<Variable> translatedIR, ArrayList<Variable> vList) {
		Collections.sort(translatedIR, new Comparator<Variable>() {
			public int compare(Variable v1, Variable v2) {
				return v1.toString().compareTo(v2.toString()); 
			}
		});
		Collections.sort(vList, new Comparator<Variable>() {
			public int compare(Variable v1, Variable v2) {
				return v1.toString().compareTo(v2.toString()); 
			}
		});
		
		if(translatedIR.size()!=vList.size())
			return false;
		else
		{
			for(int i=0;i<translatedIR.size();i++)
				if(!translatedIR.get(i).toString().equals(vList.get(i).toString()))
					return false;
		}
		return true;
	}


	@Test
	public void testR3() {
		ArrayList<Variable> vlist = new ArrayList<>();
		
		List<Variable> op1 = new ArrayList<>();
		//op1.add(new ConstantString(""));
		InternalVar iv = new InternalVar("r3", 1, true, 1);
		op1.add(iv);
		List<Variable> op2 = new ArrayList<>();
		op2.add(new ConstantString("b"));
		List<List<Variable>> operandList1 = new ArrayList<>();
		operandList1.add(op1);
		operandList1.add(op2);		
		Expression e1 = new Expression(operandList1, new Operation("append"));

		
		T t = new T(e1, "r3",1 ,0 );
		System.out.println(t.toString());
		vlist.add(t);
		//ToolKit.Display("./target/classes","usc/sql/string/testcase/NestedLoop.class","<usc.sql.string.testcase.NestedLoop: void main(java.lang.String[])>");
		TranslatedIRChecker("./target/classes","usc/sql/string/testcase/NestedLoop.class","<usc.sql.string.testcase.NestedLoop: void main(java.lang.String[])>","18",vlist );
		
		

	}
	@Test
	public void testR2() {
		ArrayList<Variable> vlist = new ArrayList<>();
		
		
		//for r3
		List<Variable> r3op1 = new ArrayList<>();
		//op1.add(new ConstantString(""));
		InternalVar r3iv = new InternalVar("r3", 1, true, 1);
		r3op1.add(r3iv);
		r3op1.add(new ConstantString(""));
		List<Variable> r3op2 = new ArrayList<>();
		r3op2.add(new ConstantString("b"));
		List<List<Variable>> r3operandList1 = new ArrayList<>();
		r3operandList1.add(r3op1);
		r3operandList1.add(r3op2);		
		Expression r3e1 = new Expression(r3operandList1, new Operation("append"));
		T r3t = new T(r3e1, "r3",1 ,0 );

		
		List<Variable> r2op1 = new ArrayList<>();
		//op1.add(new ConstantString(""));
		InternalVar r2iv = new InternalVar("r2", 1, true, 2);
		r2op1.add(r2iv);
		r2op1.add(new ConstantString(""));
		List<Variable> r2op2 = new ArrayList<>();
		r2op2.add(r3t);
		r2op2.add(new ConstantString(""));

		List<List<Variable>> r2operandList1 = new ArrayList<>();
		r2operandList1.add(r2op1);
		r2operandList1.add(r2op2);		
		Expression r2e1 = new Expression(r2operandList1, new Operation("append"));
		System.out.println(r2e1.toString());
		
		List<Variable> r22op1 = new ArrayList<>();
		//op1.add(new ConstantString(""));
		r22op1.add(r2e1);
		List<Variable> r22op2 = new ArrayList<>();
		r22op2.add(new ConstantString("a"));

		List<List<Variable>> r22operandList1 = new ArrayList<>();
		r22operandList1.add(r22op1);
		r22operandList1.add(r22op2);		
		Expression r22e1 = new Expression(r22operandList1, new Operation("append"));
		
		T t = new T(r22e1, "r2",1 ,0 );
		//System.out.println(t.toString());
		vlist.add(t);
		//ToolKit.Display("./target/classes","usc/sql/string/testcase/NestedLoop.class","<usc.sql.string.testcase.NestedLoop: void main(java.lang.String[])>");
		//TranslatedIRChecker("./target/classes","usc/sql/string/testcase/NestedLoop.class","<usc.sql.string.testcase.NestedLoop: void main(java.lang.String[])>","11",vlist );
		
		

	}
*/
}
