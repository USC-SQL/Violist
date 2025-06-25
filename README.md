# Violist
A String analysis framework for Java and Android apps. Details of the Violist algorithm and approach can be found in our paper:

```
String analysis for Java and Android applications
Ding Li, Yingjun Lyu, Mian Wan, and William G. J. Halfond
In Proceedings of the 2015 10th Joint Meeting on Foundations of Software Engineering (ESEC/FSE 2015). ACM, New York, NY, USA, 661-672.
http://www-bcf.usc.edu/~halfond/papers/li15fse.pdf
```


# Current Version
The current version integrates the improvements and additional features described as part of the SAND analysis framework.


```	
SAND: a static analysis approach for detecting SQL antipatterns.
Yingjun Lyu, Sasha Volokh, William G.J. Halfond and Omer Tripp.
In Proceedings of the International Symposium on Software Testing and Analysis (ISSTA). July 2021.
Distinguished Paper Award.
https://dl.acm.org/doi/10.1145/3460319.3464818
```
  
# Build

Violist is structured as a Maven project. To build the project, run ```mvn package```  This will install the required "graphs.jar" into your local maven repo and create a runnable jar with all dependencies included in it as well as a regular project-only jar that can then be installed in your local maven repo and used by your projects.  The runnable jar can be executed as shown below.

# Configuration

The configuration file uses the [Apache Commons-IO Configuration format](https://commons.apache.org/proper/commons-configuration/).  The options are described below. See [an example in the repo](example/config.properties)  Developers can add new options to the configuration file to provide additional properties needed by their custom interpreters.  Interactions with the configuration file are encapsulated in the [ViolistConfiguration](src/main/java/usc/sq/violist/util/ViolistConfiguration.java) class.

androidJarPath: path to location of an Android jar
parentFolderOfApk: path to parent folder of the Android APK to analyze
apkName: name of the APK file, including the apk suffix
loopUnraveledTime: an integer specifying the number of times loops will be unravelled by the interpreter
targets: a list of method signatures and parameter indexes that will be analyzed by Violist. A method signature is specified using Soot conventions, then the @ character is used to identify the parameter index (index numbers start with 1).  Multiple index numbers can be specified per method signature.  Multiple method signatures can be specified using the ';' character as a delimiter.  For example: "signatureA@1@2; signatureB@1"


# Run
Command line: ```java -jar .\target\violist-1.1.0-SNAPSHOT-jar-with-dependencies.jar .\example\config.properties```

Code:
JavaAndroid ja = new JavaAndroid(androidJar,apkFolder,classlist,apkName,target,loopItr);

ja.getInterpretedValues();

Note: The parameter target is a Map<String,List\<Integer\>> mapping the target method signature to the n_th string parameter. For example, the signature can be <LoggerLib.Logger: void reportString(java.lang.String, java.lang.Object, java.lang.String)>, the parameters can be <1,2>, representing the first and the second parameters that are string.

The output of ja.getInterpretedValues() is a Map<String,List\<String\>> mapping a string with format *method_signature@bytecode_offset@n_th_string_parameter* to a set of strings. The key uniquely represents a target string variable, which is passed to line *bytecode_offset* of the method *method_signature* as the *n_th_string_parameter*. The value represents the possible values of the string.