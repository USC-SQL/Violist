package usc.sql.violist.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViolistConfiguration {
    public static final String JSA_APP_FOLDER = "/home/yingjun/Documents/StringAnalysis/JSATesting";
    public static final String JSA_APP_GROUNDTRUTH_FOLDER = "/home/yingjun/Documents/eclipse/workspace/StringTestCases/JSAcase/groundtruth/";
    private static final String METHOD_SUMMARY_OUTPUT_FOLDER_NAME = "MethodSummary";
    private static final String METHOD_OUTPUT_FOLDER_NAME = "Output";
    private static final String INTERMEDIATE_OUTPUT_BASE_FOLDER_NAME = "target";
    public static String METHOD_SUMMARY_OUTPUT_DIR = null;
    public static char PROPERTY_FILE_LIST_DELIMETER = ';';
    public static String PROPERTY_FILE_TARGET_DELIMETER = "@";

    private static final String AndroidJarPath = "androidJarPath";
    private static final String ParentFolderOfAPK = "parentFolderOfApk";
    private static final String ApkName = "apkName";
    private static final String LoopUnraveledTime = "loopUnraveledTime";
    private static final String Targets = "targets";
    private static final String RtJar = "rtJar";

    private String androidJar;
    private String apkFolder;
    private String apkName;
    private int loopItr;
    private String rtJar;
    private Map<String, List<Integer>> targets = new HashMap<>();

    static {
        METHOD_SUMMARY_OUTPUT_DIR = INTERMEDIATE_OUTPUT_BASE_FOLDER_NAME + File.separator + METHOD_SUMMARY_OUTPUT_FOLDER_NAME + File.separator;
    }

    static public String getAppMethodSummaryDir(String appFolder) {
        return appFolder + File.separator + METHOD_SUMMARY_OUTPUT_FOLDER_NAME + File.separator;
    }

    static public String getAppOutputDir(String appFolder) {
        return appFolder + File.separator + METHOD_OUTPUT_FOLDER_NAME + File.separator;
    }

    public ViolistConfiguration(String configFilename) {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName(configFilename)
                                .setListDelimiterHandler(new DefaultListDelimiterHandler(ViolistConfiguration.PROPERTY_FILE_LIST_DELIMETER)));
        try
        {
            Configuration config = builder.getConfiguration();
            androidJar = config.getString(ViolistConfiguration.AndroidJarPath);
            apkFolder = config.getString(ViolistConfiguration.ParentFolderOfAPK);
            apkName = "/" + config.getString(ViolistConfiguration.ApkName);
            loopItr = config.getInt(ViolistConfiguration.LoopUnraveledTime);
            rtJar = config.getString(ViolistConfiguration.RtJar);

            String[] targetList = config.getStringArray(ViolistConfiguration.Targets);
            for(String targetEntry:targetList)
            {
                String[] entry = targetEntry.split(ViolistConfiguration.PROPERTY_FILE_TARGET_DELIMETER);
                String hotspot = entry[0];
                List<Integer> paraSet = new ArrayList<>();
                for(int j = 1; j < entry.length; j++)
                    paraSet.add(Integer.parseInt(entry[j]));
                targets.put(hotspot, paraSet);
            }

        }
        catch(ConfigurationException cex)
        {
            cex.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("Violist Configuration: " + System.lineSeparator());
        output.append("\t" + ViolistConfiguration.AndroidJarPath + ":" +  androidJar + System.lineSeparator());
        output.append("\t" + ViolistConfiguration.ParentFolderOfAPK + ":" + apkFolder + System.lineSeparator());
        output.append("\t" + ViolistConfiguration.ApkName + ":" + apkName + System.lineSeparator());
        output.append("\t" + ViolistConfiguration.LoopUnraveledTime + ":" + loopItr + System.lineSeparator());
        output.append("\t" + ViolistConfiguration.Targets + ":" + targets + System.lineSeparator());
        output.append("\t" + ViolistConfiguration.RtJar + ":" + rtJar + System.lineSeparator());
        return output.toString();

    }

    public String getAndroidJar() {
        return androidJar;
    }

    public String getApkFolder() {
        return apkFolder;
    }

    public String getApkName() {
        return apkName;
    }

    public int getLoopItr() {
        return loopItr;
    }

    public String getRtJar() {return rtJar;}

    public Map<String, List<Integer>> getTargets() {
        return targets;
    }
}
