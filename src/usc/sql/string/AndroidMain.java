package usc.sql.string;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidMain {

	public static void main(String[] args) {
		
		//JavaAndroid ja = new JavaAndroid(args[0],args[1],args[2],args[3],args[4],Integer.parseInt(args[5]),Integer.parseInt(args[6]));
		try {
			String content = new String(Files.readAllBytes(Paths.get(args[0])));
			String[] configs = content.split("\n");
			
			String androidJar = configs[0].split("=")[1];
			String apkFolder = configs[1].split("=")[1];
			String apkName = "/"+configs[2].split("=")[1];
			String classlist = "/"+configs[3].split("=")[1];
			int loopItr = Integer.parseInt(configs[4].split("=")[1]);
			
			Map<String,List<Integer>> target = new HashMap<>();
			for(int i = 5; i < configs.length; i++)
			{
				String[] targets = configs[5].split("@");
				String hotspot = targets[0];
				List<Integer> paraSet = new ArrayList<>();	
				for(int j = 1; j < targets.length; j++)
					paraSet.add(Integer.parseInt(targets[j]));
				target.put(hotspot,paraSet);
				//target.put("<LoggerLib.Logger: void reportString(java.lang.String,java.lang.String)>",paraSet);
			}

			
			
			JavaAndroid ja = new JavaAndroid(androidJar,apkFolder,classlist,apkName,target,loopItr);


			//args[0]:parent folder of android--1	args[1]:parent folder of apk
			//args[2]:/xxx.apk						args[3]:/classlist.txt
			//args[4]:target signature and target parameter  	args[5]:How many times a loop is unraveled
			//JavaAndroid ja = new JavaAndroid(args[0],args[1],args[2],args[3],target,1);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}
	
	
	
}
