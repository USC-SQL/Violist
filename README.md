# Violist
A String analysis framework for Java and Android apps

# Current Version
This version contains a precise interpreter. The unknown values are marked with placeholders (@Uknown@METHOD,etc). They can be treated as any string, i.e., .*

# Run
Command line: java -jar Violist.jar example/config.txt

Code:
JavaAndroid ja = new JavaAndroid(androidJar,apkFolder,classlist,apkName,target,loopItr);

ja.getInterpretedValues();

Note: The parameter target is a Map<String,List\<Integer\>> mapping the target method signiture to the target parameter indices.
  The output of ja.getInterpretedValues() is a Map<String,List\<String\>> mapping a string with format *method_signature@bytecode_offset@parameter_index* to a set of strings. The key uniquely represents a target string variable, which is passed to line *bytecode_offset* of the method *method_signature* as the *parameter_index_th* parameter. The value represents the possible values of the string.
  
# Build
We prefer to use eclipse to build the project (import an existing Maven project).

The current supported Java version is 1.8. If encounters problems, please try to downgrade to Java 1.7 (soot-trunk.jar needs to be updated accordingly).

Add graphs.jar and soot-trunk.jar to build path.

# Generate the classlist.txt
Step1: java -jar soot-trunk.jar -android-jars path-to-parentFolderOfandroid--1 -src-prec apk -f J -process-dir path-to-XXX.apk -allow-phantom-refs

Step2: read the names of all the generated files in sootOutput and write them to classlist.txt

# Configuration
Main method arguments: path-to-config.txt

Structure of config.txt:

path-to-parent-folder-of-android--1      (android--1 contains an android.jar)

path-to-parent-folder-of-apk

apkName.apk

classlist.txt                            (a list of the names of all the classes)

loopUnraveledTime                        (how many times loops are unraveled)

method-signature-of-hotspot1@parameter-index1@parameter-index2      (target method and target parameter index, index starts with 1)

method-signature-of-hotspot2@parameter-index1
