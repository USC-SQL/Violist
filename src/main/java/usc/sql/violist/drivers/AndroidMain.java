package usc.sql.violist.drivers;


import soot.*;
import usc.sql.violist.string.JavaAndroid;
import usc.sql.violist.util.ViolistConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AndroidMain {

	public static void main(String[] args) {
		ViolistConfiguration vc = new ViolistConfiguration(Paths.get(args[0]).toString());
		System.out.println(vc);
		String androidJar = vc.getAndroidJar();
		String apkFolder = vc.getApkFolder();
		String apkName = vc.getApkName();
		int loopItr = vc.getLoopItr();
		Map<String,List<Integer>> target = vc.getTargets();

		String apkPath = apkFolder + apkName;
		setupAndInvokeSoot(apkPath,androidJar,target,loopItr,apkFolder);
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
                "-force-android-jar", androidJarPath
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
