package usc.sql.violist.util;

import java.io.File;

public class ViolistConfiguration {
    public static final String RT_JAR = "C:\\Program Files\\Eclipse Adoptium\\jdk-8.0.452.9-hotspot\\jre\\lib\\rt.jar";
    public static final String JSA_APP_FOLDER = "/home/yingjun/Documents/StringAnalysis/JSATesting";
    public static final String JSA_APP_GROUNDTRUTH_FOLDER = "/home/yingjun/Documents/eclipse/workspace/StringTestCases/JSAcase/groundtruth/";
    private static final String METHOD_SUMMARY_OUTPUT_FOLDER_NAME = "MethodSummary";
    private static final String METHOD_OUTPUT_FOLDER_NAME = "Output";
    private static final String INTERMEDIATE_OUTPUT_BASE_FOLDER_NAME = "target";
    public static String METHOD_SUMMARY_OUTPUT_DIR = null;

    static {
        METHOD_SUMMARY_OUTPUT_DIR = INTERMEDIATE_OUTPUT_BASE_FOLDER_NAME + File.separator + METHOD_SUMMARY_OUTPUT_FOLDER_NAME + File.separator;
    }

    static public String getAppMethodSummaryDir(String appFolder) {
        return appFolder + File.separator + METHOD_SUMMARY_OUTPUT_FOLDER_NAME + File.separator;
    }

    static public String getAppOutputDir(String appFolder) {
        return appFolder + File.separator + METHOD_OUTPUT_FOLDER_NAME + File.separator;
    }
}
