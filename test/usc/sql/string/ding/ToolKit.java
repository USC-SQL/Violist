package usc.sql.string.ding;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.util.Chain;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import edu.usc.sql.graphs.cfg.SootCFG;

public class ToolKit {
	public static void Display(String rootDir, String component,String methodsig) {

		String classPath = rootDir;
		String sootClassPath = Scene.v().getSootClassPath() + File.pathSeparator + classPath;
		Scene.v().setSootClassPath(sootClassPath);

		Options.v().set_keep_line_number(true);

		String className = component.replace(File.separator, ".");
		className = className.substring(0, className.length()-6);

		SootClass sootClass = Scene.v().loadClassAndSupport(className);
		sootClass.setApplicationClass();
		List<SootMethod> methodList = sootClass.getMethods();

		Map<String, CFGInterface> cfgMap = new HashMap<String, CFGInterface>();
		for (SootMethod sm : methodList) {
			if(sm.toString().equals(methodsig))
			{
				Chain units = sm.retrieveActiveBody().getUnits();
				// get a snapshot iterator of the unit since we are going to
				// mutate the chain when iterating over it.
				//
				Iterator stmtIt = units.snapshotIterator();
				int offset=0;
				while(stmtIt.hasNext())
				{
					System.out.println(offset+" "+stmtIt.next());
					offset++;
				}
				//System.out.println(sm.retrieveActiveBody());
				//System.out.println(sm.retrieveActiveBody().getUnits().size());
			}

		}
	}
}
