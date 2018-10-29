# Violist
A String analysis framework for Java and Android apps. More algorithmic details of Violist can be found in our paper:

```
String analysis for Java and Android applications
Ding Li, Yingjun Lyu, Mian Wan, and William G. J. Halfond
In Proceedings of the 2015 10th Joint Meeting on Foundations of Software Engineering (ESEC/FSE 2015). ACM, New York, NY, USA, 661-672.
http://www-bcf.usc.edu/~halfond/papers/li15fse.pdf
```

# Current Version
This version contains a precise interpreter. The unknown values are marked with placeholders (@Uknown@METHOD,etc). They can be treated as any string, i.e., .*

# Run
Command line: java -jar Violist.jar example/config.txt

Code:
JavaAndroid ja = new JavaAndroid(androidJar,apkFolder,classlist,apkName,target,loopItr);

ja.getInterpretedValues();

Note: The parameter target is a Map<String,List\<Integer\>> mapping the target method signiture to the n_th string parameter. For example, the signature can be <LoggerLib.Logger: void reportString(java.lang.String, java.lang.Object, java.lang.String)>, the parameters can be <1,2>, representing the first and the second parameters that are string.

  The output of ja.getInterpretedValues() is a Map<String,List\<String\>> mapping a string with format *method_signature@bytecode_offset@n_th_string_parameter* to a set of strings. The key uniquely represents a target string variable, which is passed to line *bytecode_offset* of the method *method_signature* as the *n_th_string_parameter*. The value represents the possible values of the string.
  
# Build
We prefer to use eclipse or intelliJ to build the project (import an existing Maven project).

Add graphs.jar to build path.

# Configuration
Main method arguments: path-to-config.txt

Structure of config.txt:

path-to-parent-folder-of-android-8      (android-8 contains an android.jar)

path-to-parent-folder-of-apk

apkName.apk

loopUnraveledTime                        (how many times loops are unraveled)

method-signature-of-hotspot1@parameter-index1@parameter-index2      (target method and target parameter index, index starts with 1)

method-signature-of-hotspot2@parameter-index1
