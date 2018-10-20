package usc.sql.string;

import polyglot.ast.If;
import soot.*;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.Chain;
import soot.util.HashChain;
import CallGraph.StringCallGraph;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AndroidMain {

	public static void main(String[] args) {
        try {
			String content = new String(Files.readAllBytes(Paths.get(args[0])));
			String[] configs = content.split("\n");
			String androidJar = configs[0].split("=")[1];
			String apkFolder = configs[1].split("=")[1];
			String apkName = "/"+configs[2].split("=")[1];
			int loopItr = Integer.parseInt(configs[3].split("=")[1]);
			
			Map<String,List<Integer>> target = new HashMap<>();
			for(int i = 4; i < configs.length; i++)
			{
				String[] targets = configs[4].split("@");
				String hotspot = targets[0];
				List<Integer> paraSet = new ArrayList<>();	
				for(int j = 1; j < targets.length; j++)
					paraSet.add(Integer.parseInt(targets[j]));
				target.put(hotspot,paraSet);
			}

			String apkPath = apkFolder + apkName;
			setupAndInvokeSoot(apkPath,androidJar,target,loopItr,apkFolder);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void setupAndInvokeSoot(String apkPath, String androidJarPath,
                                   final Map<String,List<Integer>> targetSignature, final int loopItr,
                                   final String outputPath) {
        String packName = "wjtp";
        String phaseName = "wjtp.string";
        String[] sootArgs = {
                "-w",
                //"-p", "cg.cha", "enabled:true",
                "-p", phaseName, "enabled:true",
                "-f", "n",
                "-keep-line-number",
                "-keep-offset",
                "-allow-phantom-refs",
                "-process-multiple-dex",
                "-process-dir", apkPath,
                "-src-prec", "apk",
                "-cp", androidJarPath
        };
		// Create the phase and add it to the pack
		Pack pack = PackManager.v().getPack(packName);
		pack.add(new Transform(phaseName, new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName,
											 Map<String, String> options) {
                JavaAndroid ja = new JavaAndroid(targetSignature,loopItr,outputPath);
                ja.getInterpretedValues();
			}
		}));
		soot.Main.main(sootArgs);
	}
	
	
}
