# Violist
A String analysis framework for Java and Android apps

# Current Version
This version contains a precise interpreter. The unknown values are marked with placeholders (@Uknown@METHOD,etc). They can be treated as any string, i.e., .*

# Run
java -jar Violist.jar example/config.txt

# Build
We prefer to use eclipse to build the project (import an existing Maven project).

The current supported Java version is 1.8. If encounters problems, please try to downgrade to Java 1.7 (soot-trunk.jar needs to be updated accordingly).

# Configuration
Main method arguments: path-to-config.txt

Structure of config.txt:

path-to-parent-folder-of-android--1      (android--1 contains an android.jar)

path-to-parent-folder-of-apk--1 

apkName.apk

classlist.txt                            (a list of the names of all the classes)

loopUnraveledTime                        (how many times loops are unraveled)

method-signature-of-hotspot1@parameter-index1@parameter-index2      (target method and target parameter index, index starts with 1)

method-signature-of-hotspot2@parameter-index1
