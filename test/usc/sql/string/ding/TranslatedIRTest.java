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

public class TranslatedIRTest {
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
	public void test() {
		ArrayList<Variable> vlist = new ArrayList<>();
		
		List<Variable> op1 = new ArrayList<>();
		op1.add(new ConstantString("b"));
		InternalVar iv = new InternalVar("r2", 1, true, 1);
		op1.add(iv);
		List<Variable> op2 = new ArrayList<>();
		op2.add(new ConstantString("e"));
		List<List<Variable>> operandList1 = new ArrayList<>();
		operandList1.add(op1);
		operandList1.add(op2);		
		Expression e1 = new Expression(operandList1, new Operation("append"));
		
		List<Variable> op3 = new ArrayList<>();
		op3.add(e1);
		List<Variable> op4 = new ArrayList<>();
		op4.add(new ConstantString("f"));
		List<List<Variable>> operandList2 = new ArrayList<>();
		operandList2.add(op3);
		operandList2.add(op4);	
		Expression e2 = new Expression(operandList2,new Operation("append"));
		
		T t = new T(e2, "r2",1 ,0 );
		
		vlist.add(t);
		TranslatedIRChecker("./target/classes","usc/sql/string/testcase/IRTestCase1.class","<usc.sql.string.testcase.IRTestCase1: void main(java.lang.String[])>","14",vlist );
		
	}
*/
}
